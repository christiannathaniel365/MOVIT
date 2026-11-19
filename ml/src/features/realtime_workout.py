"""
Real-time workout detector v6 (MOVIT): output GOOD / BAD + alasan.

Alur:
  1. MENUNGGU : tunjukkan 5 jari (telapak terbuka) ke kamera dan tahan ~1 detik.
  2. BERSIAP  : hitung mundur supaya kamu sempat pindah ke posisi workout.
  3. BERJALAN : sistem mengenali gerakan (5 kelas) lalu menilai form:
                  GOOD = tidak ada kesalahan yang terdeteksi
                  BAD  = ada kesalahan; alasan + cara memperbaikinya tampil di layar
                Push-up & push-up lutut: kedalaman dan lengan lurus dinilai per repetisi.

Tombol: r = ulang dari awal, q / Esc = keluar.
Ini SARAN form, bukan pengganti pelatih.

Contoh:
  python realtime_workout.py
  python realtime_workout.py --gerakan Pushup
  python realtime_workout.py --model-dir "C:\\...\\Dataset\\outputs" --kamera 1
  python realtime_workout.py --video rekaman.mp4          (uji pakai file video, tanpa gestur)

Butuh : pip install opencv-python mediapipe numpy pandas scikit-learn joblib
Folder: movit_model_v6.pkl ada di folder yang sama dengan movit_fitur.py (mis. MOVIT/ml/src/features);
        skrip ini boleh di folder itu juga atau di folder tetangganya (mis. MOVIT/ml/src).
"""
import argparse
import math
import sys
import time
from collections import Counter, deque
from pathlib import Path

import numpy as np

_SINI = Path(__file__).resolve().parent
# movit_fitur.py dicari di folder skrip ini dan pola umum (mis. MOVIT/ml/src/features)
CARI_FITUR = [_SINI, _SINI / "features"] + [b / s for b in (_SINI.parent, _SINI.parent.parent, _SINI.parent.parent.parent)
                                            for s in ("features", "src/features", "ml/src/features", "src")]
DIR_FITUR = next((d for d in CARI_FITUR if (d / "movit_fitur.py").exists()), None)
if DIR_FITUR is not None:
    sys.path.insert(0, str(DIR_FITUR))
try:
    from movit_fitur import (FITUR_VERSI, KELAS, KELUARGA_PUSHUP, MAX_MISSING, NAMA_TAMPIL, TARGET_FPS,
                             bangun_fitur, cek_aturan, metrik_satu, metrik_window, pesan, sudut_siku_kp)
except ImportError:
    sys.exit("movit_fitur.py tidak ketemu. Sudah dicari di:\n  " + "\n  ".join(map(str, CARI_FITUR)))

# ---------------------------------------------------------------- pengaturan
NAMA_PAKET = "movit_model_v6.pkl"
DEFAULT_MODEL_DIRS = [Path(r"C:\Users\hanse\OneDrive\Documents\MOVIT\ml\src\features")]

HOLD_SEC = 1.0          # lama 5 jari harus ditahan
HOLD_GRACE = 0.3        # toleransi deteksi tangan sempat hilang sebentar
SMOOTH_N = 5            # rata-rata N prediksi kelas terakhir supaya tidak berkedip
KELAS_MIN_YAKIN = 0.5   # (internal) di bawah ini gerakan dianggap belum dikenali
MIRIP_MIN = 0.35        # (internal) dengan --gerakan: di bawah ini "belum terlihat seperti ..."
POSE_LOST_SEC = 2.0     # pose hilang selama ini -> minta user sesuaikan kamera
STABIL_N, STABIL_MIN = 3, 2   # kesalahan baru ditampilkan kalau muncul di >= 2 dari 3 penilaian terakhir

# penghitung repetisi push-up (sudut siku, derajat / detik)
REP_TURUN = 30          # turun minimal segini dari posisi atas = mulai repetisi
REP_NAIK = 30           # naik minimal segini dari posisi bawah = repetisi selesai
REP_BERLAKU = 6.0       # hasil repetisi terakhir dipakai selama ini
REP_TUNGGU = 4.0        # tidak ada repetisi selama ini ...
REP_DIAM = 12           # ... tapi siku bergerak > ini  ->  "turun kurang dalam"; kalau tidak -> "mulai push-up"
ATURAN_REP = {"KURANG_DALAM", "KURANG_LURUS"}
# dengan --gerakan: prediksi kelas yang masih dianggap "posisi yang benar". Posisi awal push-up
# (lengan lurus, belum turun) memang mirip high plank, jadi itu tidak boleh dianggap salah gerakan.
KOMPATIBEL = {
    "Pushup": {"Pushup", "Pushup_on_Knees", "High_Plank"},
    "Pushup_on_Knees": {"Pushup", "Pushup_on_Knees", "High_Plank"},
}

HIJAU, MERAH, KUNING, ORANYE, ABU, PUTIH = (0, 200, 0), (40, 40, 230), (0, 220, 255), (0, 150, 255), (200, 200, 200), (255, 255, 255)


# ---------------------------------------------------------------- window
def prep_window(buf):
    """list of (33,5) -> (T,33,5) siap pakai, atau None kalau terlalu banyak frame tanpa pose."""
    w = np.stack(buf).astype(np.float32)
    miss = np.isnan(w).any(axis=(1, 2))
    if miss.all() or miss.mean() > MAX_MISSING:
        return None
    if miss.any():
        import pandas as pd
        w = (pd.DataFrame(w.reshape(len(w), -1)).interpolate(limit_direction="both")
             .to_numpy().reshape(w.shape)).astype(np.float32)
    return w


# ---------------------------------------------------------------- repetisi push-up
class PenghitungRep:
    """Satu repetisi = siku turun >= REP_TURUN dari posisi atas lalu naik >= REP_NAIK dari posisi bawah.
    Disimpan: sudut atas (sebelum turun) dan sudut bawah (paling dalam)."""

    def __init__(self):
        self.reset()

    def reset(self):
        self.fase, self.ema, self.puncak, self.lembah = "atas", None, None, None
        self.jumlah, self.terakhir, self.waktu = 0, None, None

    def tambah(self, sudut, now):
        if not np.isfinite(sudut):
            return None
        self.ema = sudut if self.ema is None else 0.5 * sudut + 0.5 * self.ema
        a = self.ema
        if self.fase == "atas":
            self.puncak = a if self.puncak is None else max(self.puncak, a)
            if a < self.puncak - REP_TURUN:
                self.fase, self.lembah = "turun", a
        else:
            self.lembah = min(self.lembah, a)
            if a > self.lembah + REP_NAIK:
                self.terakhir = {"atas": float(self.puncak), "bawah": float(self.lembah)}
                self.jumlah += 1
                self.waktu = now
                self.fase, self.puncak = "atas", a
                return self.terakhir
        return None


# ---------------------------------------------------------------- analisis
class Analisis:
    """Buffer 30 frame -> kelas gerakan -> aturan biomekanik (+ ML kualitas bila ada) -> GOOD/BAD."""

    def __init__(self, paket, target=None):
        self.p = paket
        self.names = list(paket["kelas"])
        self.window, self.stride = int(paket["window"]), int(paket["stride"])
        self.aturan = paket["aturan"]
        self.model_q = paket.get("model_kualitas") or {}
        self.target = target
        self.reset()

    def reset(self):
        self.buf = deque(maxlen=self.window)
        self.baru = 0
        self.p_kelas = deque(maxlen=SMOOTH_N)
        self.riwayat = deque(maxlen=STABIL_N)       # tiap penilaian: {id: pesan}
        self.riwayat_ml = deque(maxlen=STABIL_N)
        self.rep = PenghitungRep()
        self.kelas_aktif, self.mirip, self.valid, self.menunggu_rep = None, True, True, False
        self.t_mulai = None

    # -- dipanggil tiap frame sampel (~15 fps)
    def tambah(self, kp, now):
        if self.t_mulai is None:
            self.t_mulai = now
        self.buf.append(np.full((33, 5), np.nan, np.float32) if kp is None else kp)
        if kp is not None:
            self.rep.tambah(sudut_siku_kp(kp), now)
        self.baru += 1
        if len(self.buf) < self.window or self.baru < self.stride:
            return
        self.baru = 0
        w = prep_window(self.buf)
        self.valid = w is not None
        if w is None:
            return
        X = w[None]
        F = bangun_fitur(X)
        model = self.p["model_kelas"]
        full = np.zeros(len(self.names), np.float32)
        full[np.asarray(model.classes_, dtype=int)] = model.predict_proba(F)[0]
        self.p_kelas.append(full)

        kelas = self._tentukan_kelas()
        if kelas != self.kelas_aktif:
            self.kelas_aktif = kelas
            self.riwayat.clear()
            self.riwayat_ml.clear()
        if kelas is None:
            return

        m = metrik_satu(metrik_window(X))
        pushup = kelas in KELUARGA_PUSHUP
        masalah = cek_aturan(m, kelas, self.aturan, lewati=ATURAN_REP if pushup else ())
        self.menunggu_rep = False
        if pushup:
            masalah += self._masalah_rep(kelas, m, now)
        self.riwayat.append(dict(masalah))

        mq = self.model_q.get(kelas)
        if mq is not None and 1 in mq.classes_:
            p_good = float(mq.predict_proba(F)[0][list(mq.classes_).index(1)])
            self.riwayat_ml.append(p_good < 0.5)

    def _tentukan_kelas(self):
        p = np.mean(self.p_kelas, axis=0)
        i = int(np.argmax(p))
        if self.target:
            boleh = KOMPATIBEL.get(self.target, {self.target})
            skor = sum(p[self.names.index(k)] for k in boleh if k in self.names)
            self.mirip = self.names[i] in boleh or skor >= MIRIP_MIN
            return self.target
        self.mirip = True
        return self.names[i] if p[i] >= KELAS_MIN_YAKIN else None

    def _masalah_rep(self, kelas, m, now):
        rep = self.rep.terakhir
        if rep is not None and now - self.rep.waktu <= REP_BERLAKU:
            return cek_aturan({"siku_bawah": rep["bawah"], "siku_atas": rep["atas"]}, kelas, self.aturan, hanya=ATURAN_REP)
        sejak = now - (self.rep.waktu if self.rep.waktu is not None else self.t_mulai)
        aktif = any(r["id"] == "KURANG_DALAM" and r.get("aktif", True) for r in self.aturan.get(kelas, []))
        if sejak > REP_TUNGGU and m["siku_rentang"] > REP_DIAM and aktif:
            return [("KURANG_DALAM", pesan(kelas, "KURANG_DALAM"))]   # bergerak tapi terlalu dangkal
        self.menunggu_rep = True
        return []

    def status(self):
        k = self.kelas_aktif
        if k is None:
            return {"keadaan": "DETEKSI"}
        if not self.mirip:
            return {"keadaan": "TIDAK_MIRIP", "kelas": k}
        rep = self.rep.jumlah if k in KELUARGA_PUSHUP else None
        if not self.riwayat:
            return {"keadaan": "MENILAI", "kelas": k, "rep": rep}
        if k in KELUARGA_PUSHUP and self.menunggu_rep:
            return {"keadaan": "MULAI", "kelas": k, "rep": rep}
        butuh = min(STABIL_MIN, len(self.riwayat))
        hit = Counter(i for d in self.riwayat for i in d)
        teks_id = {}
        for d in self.riwayat:
            teks_id.update(d)
        urutan = [r["id"] for r in self.aturan.get(k, [])] + [i for i in teks_id if i not in {r["id"] for r in self.aturan.get(k, [])}]
        alasan = []
        for i in urutan:
            if hit[i] >= butuh and teks_id[i] not in alasan:
                alasan.append(teks_id[i])
        if not alasan and self.riwayat_ml and sum(self.riwayat_ml) >= min(STABIL_MIN, len(self.riwayat_ml)):
            alasan = [pesan(k, "ML_BURUK")]
        return {"keadaan": "NILAI", "kelas": k, "form": "BAD" if alasan else "GOOD", "alasan": alasan, "rep": rep}


# ---------------------------------------------------------------- deteksi 5 jari
def _d(a, b):
    return math.hypot(a[0] - b[0], a[1] - b[1])


def tangan_terbuka(lm):
    """lm: 21 titik (x, y) MediaPipe Hands. True kalau kelima jari terentang."""
    w = lm[0]
    for tip, pip in ((8, 6), (12, 10), (16, 14), (20, 18)):
        if _d(lm[tip], w) < _d(lm[pip], w) * 1.1:
            return False
    return _d(lm[4], lm[17]) > _d(lm[3], lm[17]) * 1.1


class Mesin:
    """MENUNGGU -> BERSIAP -> BERJALAN. step() dipanggil tiap frame."""

    def __init__(self, countdown=5.0, hold=HOLD_SEC, grace=HOLD_GRACE, tanpa_gestur=False):
        self.countdown, self.hold, self.grace = countdown, hold, grace
        self.tanpa_gestur = tanpa_gestur
        self.reset()

    def reset(self):
        self.state, self.hold_start, self.last_open, self.count_start = "MENUNGGU", None, None, None

    def step(self, buka, now):
        if self.state == "MENUNGGU":
            if self.tanpa_gestur:
                self.state, self.count_start = "BERSIAP", now
            elif buka:
                if self.hold_start is None:
                    self.hold_start = now
                self.last_open = now
                if now - self.hold_start >= self.hold:
                    self.state, self.count_start = "BERSIAP", now
            elif self.hold_start is not None and now - self.last_open > self.grace:
                self.hold_start = None
            prog = 0.0 if self.hold_start is None else min(1.0, (now - self.hold_start) / self.hold)
            return self.state, prog, 0.0
        if self.state == "BERSIAP":
            sisa = self.countdown - (now - self.count_start)
            if sisa <= 0:
                self.state = "BERJALAN"
                return self.state, 1.0, 0.0
            return self.state, 1.0, sisa
        return self.state, 1.0, 0.0


# ---------------------------------------------------------------- model
def cari_paket(arg):
    here = Path(__file__).resolve().parent
    kandidat = ([Path(arg)] if arg else []) + ([DIR_FITUR] if DIR_FITUR else []) + [
        here, here / "outputs", Path("outputs"), *DEFAULT_MODEL_DIRS]      # utama: folder movit_fitur.py
    for d in kandidat:
        f = d if d.suffix == ".pkl" else d / NAMA_PAKET
        if f.exists():
            return f
    sys.exit(f"{NAMA_PAKET} tidak ketemu. Jalankan notebook v6 sampai bagian 10 (model tersimpan di folder "
             f"movit_fitur.py), atau pakai --model-dir <folder>. Dicari di:\n  " + "\n  ".join(map(str, kandidat)))


# ---------------------------------------------------------------- tampilan
FONT = None


def teks(cv2, img, s, x, y, warna=PUTIH, skala=0.7, tebal=2, garis=True):
    if garis:
        cv2.putText(img, s, (x, y), FONT, skala, (0, 0, 0), tebal + 3, cv2.LINE_AA)
    cv2.putText(img, s, (x, y), FONT, skala, warna, tebal, cv2.LINE_AA)


def footer(cv2, img):
    H, W = img.shape[:2]
    panel(cv2, img, 0, H - 32, W, H, 0.55)
    teks(cv2, img, "r: ulang | q: keluar | Ini saran form, bukan pengganti pelatih", 20, H - 11, ABU, 0.5, 1, garis=False)


def teks_bungkus(cv2, img, s, x, y, maks, warna=PUTIH, skala=0.6, tebal=2, jarak=26):
    """tulis teks dengan pindah baris otomatis; return y berikutnya."""
    baris = ""
    for kata in s.split():
        uji = (baris + " " + kata).strip()
        if cv2.getTextSize(uji, FONT, skala, tebal)[0][0] > maks and baris:
            teks(cv2, img, baris, x, y, warna, skala, tebal)
            y, baris = y + jarak, kata
        else:
            baris = uji
    if baris:
        teks(cv2, img, baris, x, y, warna, skala, tebal)
        y += jarak
    return y


def panel(cv2, img, x1, y1, x2, y2, alpha=0.45):
    roi = img[y1:y2, x1:x2]
    if roi.size:
        img[y1:y2, x1:x2] = cv2.addWeighted(roi, 1 - alpha, np.zeros_like(roi), alpha, 0)


def gambar_status(cv2, img, st, ana, target):
    H, W = img.shape[:2]
    maks = W - 50
    panel(cv2, img, 0, 0, W, min(H, 230))
    k = st.get("kelas")
    if k:
        judul = f"Gerakan: {NAMA_TAMPIL.get(k, k)}"
        if st.get("rep") is not None:
            judul += f"   |   Repetisi: {st['rep']}"
        teks(cv2, img, judul, 20, 35, PUTIH, 0.75)
    kd = st["keadaan"]
    if kd == "DETEKSI":
        teks(cv2, img, "Mendeteksi gerakan...", 20, 35, ABU, 0.75)
    elif kd == "TIDAK_MIRIP":
        teks_bungkus(cv2, img, f"Belum terlihat seperti {NAMA_TAMPIL.get(target, target)}. Ambil posisi yang benar, "
                               "seluruh tubuh terlihat dari samping.", 20, 75, maks, ORANYE, 0.65)
    elif kd == "MENILAI":
        teks(cv2, img, "Menilai form...", 20, 80, ABU, 0.8)
    elif kd == "MULAI":
        teks(cv2, img, "Mulai push-up: turunkan badan lalu dorong naik.", 20, 80, KUNING, 0.7)
    elif kd == "NILAI":
        good = st["form"] == "GOOD"
        teks(cv2, img, st["form"], 20, 95, HIJAU if good else MERAH, 1.8, 4)
        y = 135
        if good:
            teks(cv2, img, "Postur sudah benar, pertahankan!", 20, y, HIJAU, 0.65)
        for a in st["alasan"][:3]:
            y = teks_bungkus(cv2, img, "- " + a, 20, y, maks, KUNING, 0.6)


# ---------------------------------------------------------------- main
def main():
    global FONT
    ap = argparse.ArgumentParser(description="Deteksi workout real-time v6 (GOOD/BAD + alasan)")
    ap.add_argument("--model-dir", help=f"folder berisi {NAMA_PAKET} (atau path file .pkl-nya)")
    ap.add_argument("--gerakan", choices=KELAS, help="gerakan yang dipilih user (opsional)")
    ap.add_argument("--kamera", type=int, default=0, help="nomor kamera (default 0)")
    ap.add_argument("--video", help="uji pakai file video, bukan kamera")
    ap.add_argument("--countdown", type=float, default=5.0, help="detik bersiap setelah 5 jari (default 5)")
    ap.add_argument("--tanpa-gestur", action="store_true", help="lewati 5 jari, langsung hitung mundur")
    a = ap.parse_args()

    import cv2
    import joblib
    import mediapipe as mp
    FONT = cv2.FONT_HERSHEY_SIMPLEX

    f = cari_paket(a.model_dir)
    paket = joblib.load(f)
    if paket.get("fitur_versi") != FITUR_VERSI:
        sys.exit(f"{f.name} dibuat dengan fitur {paket.get('fitur_versi')}, sedangkan movit_fitur.py versi {FITUR_VERSI}. "
                 "Latih ulang dengan notebook v6.")
    print("paket model:", f)
    print("mode kualitas per kelas:", paket.get("mode_kualitas"))

    dari_video = bool(a.video)
    cap = cv2.VideoCapture(a.video if dari_video else a.kamera)
    if not cap.isOpened():
        sys.exit(f"{'video ' + a.video if dari_video else f'kamera {a.kamera}'} tidak bisa dibuka "
                 "(coba --kamera 1, dan tutup aplikasi lain yang memakai kamera)")
    fps_video = cap.get(cv2.CAP_PROP_FPS) or 30.0

    mesin = Mesin(countdown=0.0 if dari_video else a.countdown, tanpa_gestur=a.tanpa_gestur or dari_video)
    ana = Analisis(paket, target=a.gerakan)
    target_fps = float(paket.get("target_fps", TARGET_FPS))
    mp_hands, mp_pose, mp_draw = mp.solutions.hands, mp.solutions.pose, mp.solutions.drawing_utils
    last_sample, last_pose, n_frame, terakhir_dicetak = -1e9, None, 0, None

    with mp_hands.Hands(max_num_hands=1, min_detection_confidence=0.6, min_tracking_confidence=0.5) as hands, \
            mp_pose.Pose(min_detection_confidence=0.5, model_complexity=1) as pose:
        while cap.isOpened():
            ok, frame = cap.read()
            if not ok:
                break
            n_frame += 1
            now = n_frame / fps_video if dari_video else time.monotonic()
            last_pose = now if last_pose is None else last_pose
            rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

            buka = False
            if mesin.state == "MENUNGGU":
                res = hands.process(rgb)
                if res.multi_hand_landmarks:
                    h = res.multi_hand_landmarks[0]
                    buka = tangan_terbuka([(p.x, p.y) for p in h.landmark])
                    mp_draw.draw_landmarks(frame, h, mp_hands.HAND_CONNECTIONS)
            state, prog, sisa = mesin.step(buka, now)
            H, W = frame.shape[:2]

            if state == "MENUNGGU":
                panel(cv2, frame, 0, 0, W, 130)
                teks(cv2, frame, "Tunjukkan 5 jari untuk mulai", 20, 40)
                if a.gerakan:
                    teks(cv2, frame, f"Target gerakan: {NAMA_TAMPIL[a.gerakan]}", 20, 75, KUNING, 0.7)
                cv2.rectangle(frame, (20, 95), (20 + int(300 * prog), 115), (0, 220, 0), -1)
                cv2.rectangle(frame, (20, 95), (320, 115), PUTIH, 2)

            elif state == "BERSIAP":
                panel(cv2, frame, 0, 0, W, 60)
                teks(cv2, frame, "Pindah ke posisi workout. Seluruh tubuh harus terlihat.", 20, 40, KUNING, 0.7)
                teks(cv2, frame, str(int(math.ceil(sisa))), W // 2 - 30, H // 2 + 40, KUNING, 5, 8)
                ana.reset()
                last_pose = now

            else:   # BERJALAN
                if now - last_sample >= 1.0 / target_fps:
                    last_sample = now
                    res = pose.process(rgb)
                    kp = None
                    if res.pose_landmarks is not None and res.pose_world_landmarks is not None:
                        last_pose = now
                        mp_draw.draw_landmarks(frame, res.pose_landmarks, mp_pose.POSE_CONNECTIONS)
                        l2, l3 = res.pose_landmarks.landmark, res.pose_world_landmarks.landmark
                        kp = np.array([[l2[j].x, l2[j].y, l3[j].x, l3[j].y, l3[j].z] for j in range(33)], np.float32)
                    ana.tambah(kp, now)

                if now - last_pose > POSE_LOST_SEC:
                    panel(cv2, frame, 0, 0, W, 60)
                    teks(cv2, frame, "Tubuh tidak terlihat. Mundur / sesuaikan kamera.", 20, 40, ORANYE, 0.7)
                elif len(ana.buf) < ana.window and not ana.p_kelas:
                    panel(cv2, frame, 0, 0, W, 60)
                    teks(cv2, frame, f"Mengumpulkan gerakan... {len(ana.buf)}/{ana.window}", 20, 40, ABU, 0.7)
                elif not ana.valid:
                    panel(cv2, frame, 0, 0, W, 60)
                    teks(cv2, frame, "Video kurang jelas, coba sesuaikan posisi.", 20, 40, ORANYE, 0.7)
                else:
                    st = ana.status()
                    gambar_status(cv2, frame, st, ana, a.gerakan)
                    kunci = (st["keadaan"], st.get("kelas"), st.get("form"), tuple(st.get("alasan", ())))
                    if kunci != terakhir_dicetak:
                        terakhir_dicetak = kunci
                        jam = f"{now:6.1f}s" if dari_video else time.strftime("%H:%M:%S")
                        info = st.get("form", st["keadaan"])
                        print(f"[{jam}] {NAMA_TAMPIL.get(st.get('kelas'), '-')}: {info}"
                              + "".join(f"\n           - {x}" for x in st.get("alasan", [])))

            footer(cv2, frame)
            cv2.imshow("MOVIT Workout Detector v6", frame)
            k = cv2.waitKey(1) & 0xFF
            if k in (ord("q"), 27):
                break
            if k == ord("r"):
                mesin.reset()
                ana.reset()

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
