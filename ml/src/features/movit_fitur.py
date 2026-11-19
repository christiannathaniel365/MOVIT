"""
movit_fitur.py  -  modul bersama MOVIT v6 (dipakai notebook DAN realtime_workout.py).

Kenapa modul terpisah: dulu fungsi fitur ditulis dua kali (notebook + realtime) dan harus
"dijaga sama" manual. Sekarang cukup satu sumber, jadi tidak mungkin beda.

Isi:
  - fitur_frame / bangun_fitur : fitur per frame -> ringkasan per window (input model ML)
  - metrik_window              : angka-angka yang dipakai aturan biomekanik
  - ATURAN_DEFAULT + PESAN     : aturan GOOD/BAD per kelas + alasan & cara memperbaiki
  - cek_aturan                 : jalankan aturan pada satu window, hasil = daftar (id, pesan)

Format keypoint: array (..., 33, 5) = [x2d, y2d, x3d, y3d, z3d] dari MediaPipe
(pose_landmarks untuk 2D, pose_world_landmarks untuk 3D). Sama dengan cache v5.
Konvensi MediaPipe: sumbu y mengarah ke BAWAH, jadi offset y positif = lebih rendah.
"""
import copy

import numpy as np

FITUR_VERSI = "v6"

KELAS = ["Elbow_Plank", "High_Plank", "Pushup", "Pushup_on_Knees", "Side_Plank"]
NAMA_TAMPIL = {
    "Elbow_Plank": "Elbow Plank",
    "High_Plank": "High Plank",
    "Pushup": "Push-up",
    "Pushup_on_Knees": "Push-up (lutut)",
    "Side_Plank": "Side Plank",
}
KELUARGA_PUSHUP = ("Pushup", "Pushup_on_Knees")

TARGET_FPS = 15      # semua video disamakan ke ~15 fps
WINDOW = 30          # 30 frame = 2 detik
STRIDE = 10
MAX_MISSING = 0.2    # window dibuang kalau >20% frame tanpa pose

# indeks landmark MediaPipe (kiri, kanan)
TELINGA, BAHU, SIKU, TANGAN = (7, 8), (11, 12), (13, 14), (15, 16)
PINGGUL, LUTUT, KAKI = (23, 24), (25, 26), (27, 28)

# sudut sendi kiri/kanan. Disimpan sebagai (min, max) pasangan kiri-kanan, jadi fitur
# tidak peduli sisi mana yang menghadap kamera (side plank kiri = side plank kanan).
PASANGAN_SUDUT = [
    ("siku", (11, 13, 15), (12, 14, 16)),
    ("bahu", (23, 11, 13), (24, 12, 14)),
    ("pinggul", (11, 23, 25), (12, 24, 26)),
    ("lutut", (23, 25, 27), (24, 26, 28)),
]

NAMA_FRAME = [f"{n}_{s}" for n, _, _ in PASANGAN_SUDUT for s in ("min", "max")] + [
    "sudut_tubuh",          # bahu-pinggul-kaki (~180 = badan lurus)
    "sudut_pinggul_mid",    # bahu-pinggul-lutut
    "sudut_lutut_mid",      # pinggul-lutut-kaki (~175 lurus, ~90 push-up lutut)
    "dev_pinggul",          # offset bertanda pinggul dari garis bahu-kaki (+ turun, - naik)
    "dev_kepala",           # offset bertanda telinga dari garis pinggul-bahu (+ menunduk, - mendongak)
    "sudut_leher",          # pinggul-bahu-telinga
    "rasio_bahu_kaki_lutut",
    "inklinasi_torso",
    "lebar_tangan",         # jarak pergelangan / lebar bahu
    "lebar_siku",           # jarak siku / lebar bahu
    "tangan_vs_siku",       # jarak pergelangan / jarak siku (<1 = lengan bawah mengarah ke dalam)
    "jarak_bahu_tangan",    # naik-turun saat push-up, diam saat plank
    "v_pinggul",            # kecepatan 2D (per frame, dibagi panjang torso)
    "v_bahu",
    "v_tangan",
    "v_kaki",
    "gerak_kaki_vs_lutut",
]
STAT = ("min", "max", "mean", "std", "range")


# ---------------------------------------------------------------- helper geometri
def _sudut(p, q, r):
    """sudut P-Q-R (derajat) untuk array (..., D)."""
    a, b = p - q, r - q
    cos = (a * b).sum(-1) / (np.linalg.norm(a, axis=-1) * np.linalg.norm(b, axis=-1) + 1e-6)
    return np.degrees(np.arccos(np.clip(cos, -1.0, 1.0)))


def _jarak(p, q):
    return np.linalg.norm(p - q, axis=-1)


def _dev_garis(p, a, b):
    """Offset bertanda titik p dari garis a->b, dibagi panjang a-b.
    Tanda ikut sumbu y (ke bawah): + berarti p lebih rendah dari garis."""
    ab = b - a
    t = ((p - a) * ab).sum(-1) / ((ab * ab).sum(-1) + 1e-9)
    off = p - (a + t[..., None] * ab)
    return np.sign(off[..., 1]) * np.linalg.norm(off, axis=-1) / (np.linalg.norm(ab, axis=-1) + 1e-6)


def _mid(P, ij):
    return (P[:, :, ij[0]] + P[:, :, ij[1]]) / 2.0


# ---------------------------------------------------------------- fitur per frame
def fitur_frame(X):
    """X (N, T, 33, 5) -> (N, T, K) fitur per frame, urutan = NAMA_FRAME."""
    X = np.asarray(X, dtype=np.float32)
    P3, P2 = X[..., 2:5], X[..., 0:2]
    out = []

    for _, ki, ka in PASANGAN_SUDUT:
        s_ki = _sudut(P3[:, :, ki[0]], P3[:, :, ki[1]], P3[:, :, ki[2]])
        s_ka = _sudut(P3[:, :, ka[0]], P3[:, :, ka[1]], P3[:, :, ka[2]])
        out += [np.minimum(s_ki, s_ka), np.maximum(s_ki, s_ka)]

    bahu, pinggul, lutut, kaki = _mid(P3, BAHU), _mid(P3, PINGGUL), _mid(P3, LUTUT), _mid(P3, KAKI)
    telinga = _mid(P3, TELINGA)
    torso = _jarak(bahu, pinggul) + 1e-6
    lebar_bahu = np.maximum(_jarak(P3[:, :, 11], P3[:, :, 12]), 0.05)

    s_tubuh = _sudut(bahu, pinggul, kaki)
    s_pinggul = _sudut(bahu, pinggul, lutut)
    s_lutut = _sudut(pinggul, lutut, kaki)
    # garis badan: bahu->kaki kalau kaki lurus, bahu->lutut kalau bertumpu di lutut
    ujung = np.where((s_lutut > 150)[..., None], kaki, lutut)
    dev_pinggul = _dev_garis(pinggul, bahu, ujung)
    dev_kepala = _dev_garis(telinga, pinggul, bahu)
    s_leher = _sudut(pinggul, bahu, telinga)

    r_bk_bl = _jarak(bahu, kaki) / (_jarak(bahu, lutut) + 1e-6)
    v_torso = bahu - pinggul
    inklinasi = np.degrees(np.arctan2(np.abs(v_torso[..., 1]), np.linalg.norm(v_torso[..., [0, 2]], axis=-1) + 1e-6))

    lebar_tangan = _jarak(P3[:, :, 15], P3[:, :, 16]) / lebar_bahu
    lebar_siku = _jarak(P3[:, :, 13], P3[:, :, 14]) / lebar_bahu
    tangan_vs_siku = lebar_tangan / (lebar_siku + 1e-6)
    jarak_bahu_tangan = _jarak(bahu, _mid(P3, TANGAN)) / torso

    # gerak dihitung di 2D (koordinat 3D world selalu berpusat di pinggul, jadi pinggul "diam")
    torso2 = np.maximum(_jarak(_mid(P2, BAHU), _mid(P2, PINGGUL)), 0.05)

    def kecepatan(p):
        d = np.diff(p, axis=1, prepend=p[:, :1])
        return np.linalg.norm(d, axis=-1) / torso2

    v_pinggul, v_bahu = kecepatan(_mid(P2, PINGGUL)), kecepatan(_mid(P2, BAHU))
    v_tangan, v_kaki, v_lutut = kecepatan(_mid(P2, TANGAN)), kecepatan(_mid(P2, KAKI)), kecepatan(_mid(P2, LUTUT))
    r_gerak = np.clip(v_kaki / (v_lutut + 1e-3), 0.0, 10.0)

    out += [s_tubuh, s_pinggul, s_lutut, dev_pinggul, dev_kepala, s_leher, r_bk_bl, inklinasi,
            lebar_tangan, lebar_siku, tangan_vs_siku, jarak_bahu_tangan,
            v_pinggul, v_bahu, v_tangan, v_kaki, r_gerak]
    F = np.stack(out, -1).astype(np.float32)
    return np.nan_to_num(F, nan=0.0, posinf=0.0, neginf=0.0)


def summarize(A):
    """(N, T, K) -> (N, 5K): min, max, mean, std, range tiap fitur."""
    return np.concatenate([A.min(1), A.max(1), A.mean(1), A.std(1), A.max(1) - A.min(1)], axis=1)


def bangun_fitur(X):
    """X (N, T, 33, 5) -> matriks fitur model ML (N, 5 * len(NAMA_FRAME))."""
    return summarize(fitur_frame(X))


def nama_fitur():
    return [f"{n}_{s}" for s in STAT for n in NAMA_FRAME]


# ---------------------------------------------------------------- metrik untuk aturan
_IDX = {n: i for i, n in enumerate(NAMA_FRAME)}


def sudut_siku(F):
    """rata-rata siku kiri-kanan per frame dari output fitur_frame."""
    return (F[..., _IDX["siku_min"]] + F[..., _IDX["siku_max"]]) / 2.0


def metrik_window(X):
    """X (N, T, 33, 5) -> dict nama -> array (N,). Satu angka per window untuk tiap metrik."""
    F = fitur_frame(X)
    g = lambda n: F[..., _IDX[n]]
    siku = sudut_siku(F)
    return {
        "siku_bawah": np.percentile(siku, 10, axis=1),   # posisi bawah push-up
        "siku_atas": np.percentile(siku, 90, axis=1),    # posisi atas push-up
        "siku_rata": siku.mean(1),
        "siku_rentang": np.percentile(siku, 90, axis=1) - np.percentile(siku, 10, axis=1),
        "lutut": np.median(g("sudut_lutut_mid"), axis=1),
        "tubuh": np.median(g("sudut_tubuh"), axis=1),
        "dev_pinggul": np.median(g("dev_pinggul"), axis=1),
        "dev_kepala": np.median(g("dev_kepala"), axis=1),
        "lebar_tangan": np.median(g("lebar_tangan"), axis=1),
        "lebar_siku": np.median(g("lebar_siku"), axis=1),
        "tangan_vs_siku": np.median(g("tangan_vs_siku"), axis=1),
        "gerak_kaki": g("v_kaki").mean(1),
        "gerak_tangan": g("v_tangan").mean(1),
        "gerak_pinggul": g("v_pinggul").mean(1),
    }


def metrik_satu(m, i=0):
    """ambil metrik window ke-i sebagai dict angka biasa."""
    return {k: float(v[i]) for k, v in m.items()}


def sudut_siku_kp(kp):
    """sudut siku rata-rata dari SATU frame keypoint (33, 5). Dipakai penghitung repetisi."""
    kp = np.asarray(kp, dtype=np.float32)
    P = kp[:, 2:5]
    s1 = _sudut(P[11], P[13], P[15])
    s2 = _sudut(P[12], P[14], P[16])
    return float((s1 + s2) / 2.0)


# ---------------------------------------------------------------- aturan GOOD / BAD
def _r(id_, metrik, arah, ambang):
    return dict(id=id_, metrik=metrik, arah=arah, ambang=float(ambang), aktif=True, sumber="default")


# Ambang awal (perkiraan). Notebook v6 mengkalibrasi ulang dari tag QEVD lalu menyimpannya
# ke movit_model_v6.pkl, dan realtime memakai hasil kalibrasi itu.
ATURAN_DEFAULT = {
    "Pushup": [
        _r("PINGGUL_TURUN", "dev_pinggul", ">", 0.06),
        _r("PINGGUL_NAIK", "dev_pinggul", "<", -0.08),
        _r("LUTUT_TEKUK", "lutut", "<", 150),
        _r("KURANG_DALAM", "siku_bawah", ">", 110),
        _r("KURANG_LURUS", "siku_atas", "<", 150),
        _r("TANGAN_LEBAR", "lebar_tangan", ">", 2.2),
        _r("KEPALA_TURUN", "dev_kepala", ">", 0.30),
        _r("KEPALA_NAIK", "dev_kepala", "<", -0.30),
    ],
    "Pushup_on_Knees": [
        _r("PINGGUL_TURUN", "dev_pinggul", ">", 0.06),
        _r("PINGGUL_NAIK", "dev_pinggul", "<", -0.08),
        _r("KURANG_DALAM", "siku_bawah", ">", 110),
        _r("KURANG_LURUS", "siku_atas", "<", 150),
        _r("TANGAN_LEBAR", "lebar_tangan", ">", 2.2),
        _r("KEPALA_TURUN", "dev_kepala", ">", 0.30),
        _r("KEPALA_NAIK", "dev_kepala", "<", -0.30),
    ],
    "High_Plank": [
        _r("PINGGUL_TURUN", "dev_pinggul", ">", 0.05),
        _r("PINGGUL_NAIK", "dev_pinggul", "<", -0.07),
        _r("LUTUT_TEKUK", "lutut", "<", 155),
        _r("SIKU_TEKUK", "siku_rata", "<", 155),
        _r("KEPALA_TURUN", "dev_kepala", ">", 0.30),
        _r("KEPALA_NAIK", "dev_kepala", "<", -0.30),
        _r("KAKI_BERGERAK", "gerak_kaki", ">", 0.04),
        _r("TANGAN_BERGERAK", "gerak_tangan", ">", 0.04),
    ],
    "Elbow_Plank": [
        _r("PINGGUL_TURUN", "dev_pinggul", ">", 0.05),
        _r("PINGGUL_NAIK", "dev_pinggul", "<", -0.07),
        _r("LUTUT_TEKUK", "lutut", "<", 155),
        _r("SIKU_KECIL", "siku_rata", "<", 65),
        _r("SIKU_BESAR", "siku_rata", ">", 115),
        _r("LENGAN_KE_DALAM", "tangan_vs_siku", "<", 0.6),
        _r("LENGAN_LEBAR", "lebar_siku", ">", 1.6),
        _r("KEPALA_TURUN", "dev_kepala", ">", 0.30),
        _r("KEPALA_NAIK", "dev_kepala", "<", -0.30),
    ],
    "Side_Plank": [
        _r("PINGGUL_TURUN", "dev_pinggul", ">", 0.05),
        _r("PINGGUL_NAIK", "dev_pinggul", "<", -0.07),
        _r("LUTUT_TEKUK", "lutut", "<", 150),
        _r("KEPALA_TURUN", "dev_kepala", ">", 0.30),
    ],
}

# Alasan = apa yang salah + cara memperbaiki. ASCII saja (font OpenCV tidak bisa simbol derajat).
PESAN = {
    "PINGGUL_TURUN": "Pinggul turun. Kencangkan perut, jaga bahu-pinggul-kaki satu garis lurus.",
    "PINGGUL_NAIK": "Pinggul terlalu tinggi. Turunkan pinggul sampai badan lurus.",
    "LUTUT_TEKUK": "Lutut menekuk/menyentuh lantai. Luruskan kaki, tumpu di ujung jari kaki.",
    "KURANG_DALAM": "Turun kurang dalam. Tekuk siku sampai sekitar 90 derajat.",
    "KURANG_LURUS": "Lengan belum lurus saat naik. Dorong sampai siku lurus.",
    "TANGAN_LEBAR": "Tangan terlalu lebar. Letakkan tangan sedikit lebih lebar dari bahu.",
    "KEPALA_TURUN": "Kepala menunduk. Jaga leher sejajar punggung.",
    "KEPALA_NAIK": "Kepala mendongak. Pandang lantai sedikit di depan tangan.",
    "SIKU_TEKUK": "Siku menekuk. Kunci lengan lurus, tangan tepat di bawah bahu.",
    "SIKU_KECIL": "Siku tidak tepat di bawah bahu. Posisikan siku lurus di bawah bahu.",
    "SIKU_BESAR": "Siku tidak tepat di bawah bahu. Posisikan siku lurus di bawah bahu.",
    "LENGAN_KE_DALAM": "Lengan bawah mengarah ke dalam. Sejajarkan lengan bawah lurus ke depan.",
    "LENGAN_LEBAR": "Siku terlalu lebar. Letakkan siku tepat di bawah bahu.",
    "KAKI_BERGERAK": "Kaki bergeser. Tahan kaki tetap diam.",
    "TANGAN_BERGERAK": "Tangan bergeser. Tahan tangan tetap di tempat.",
    "ML_BURUK": "Gerakan belum stabil. Lakukan lebih pelan dan terkontrol.",
}
PESAN_KELAS = {
    ("Side_Plank", "LUTUT_TEKUK"): "Lutut menyentuh lantai. Luruskan kaki, tumpu di sisi telapak kaki.",
    ("Side_Plank", "KEPALA_TURUN"): "Kepala terkulai. Jaga kepala sejajar badan.",
    ("Side_Plank", "PINGGUL_TURUN"): "Pinggul turun ke lantai. Angkat pinggul sampai badan lurus.",
    ("Elbow_Plank", "LUTUT_TEKUK"): "Lutut menyentuh lantai. Luruskan kaki, tumpu di ujung jari kaki.",
    ("High_Plank", "LUTUT_TEKUK"): "Lutut menekuk/menyentuh lantai. Luruskan kaki, tumpu di ujung jari kaki.",
}


def pesan(kelas, id_):
    return PESAN_KELAS.get((kelas, id_), PESAN.get(id_, id_))


def salin_aturan(aturan=None):
    return copy.deepcopy(ATURAN_DEFAULT if aturan is None else aturan)


def cek_aturan(m, kelas, aturan=None, lewati=(), hanya=None):
    """m: dict metrik (angka) untuk satu window. Hasil: list (id, pesan) yang dilanggar,
    urut sesuai prioritas di daftar aturan. `lewati`/`hanya` = filter id aturan."""
    aturan = ATURAN_DEFAULT if aturan is None else aturan
    hasil, sudah = [], set()
    for r in aturan.get(kelas, []):
        if not r.get("aktif", True) or r["id"] in lewati or (hanya is not None and r["id"] not in hanya):
            continue
        v = m.get(r["metrik"])
        if v is None or not np.isfinite(v):
            continue
        kena = v > r["ambang"] if r["arah"] == ">" else v < r["ambang"]
        p = pesan(kelas, r["id"])
        if kena and p not in sudah:
            hasil.append((r["id"], p))
            sudah.add(p)
    return hasil
