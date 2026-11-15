"""
Real-time workout detector (kamera).

Alur:
  1. MENUNGGU   : tunjukkan 5 jari (telapak terbuka) ke kamera dan tahan ~1 detik.
  2. BERSIAP    : hitung mundur supaya kamu sempat pindah ke posisi workout.
  3. BERJALAN   : sistem mengenali gerakan (kelas_model.pkl). Kalau gerakannya Pushup,
                  sistem juga memperkirakan apakah form-nya tampak baik (kualitas_model.pkl).

Tombol: r = kembali menunggu 5 jari, q / Esc = keluar.

Ini SARAN form, bukan penilaian pasti dan bukan pengganti pelatih.

Contoh:
  python realtime_workout.py
  python realtime_workout.py --gerakan Pushup
  python realtime_workout.py --model-dir "C:\\...\\Dataset\\outputs" --kamera 1

Butuh: pip install opencv-python mediapipe numpy pandas scikit-learn joblib
(pakai versi mediapipe yang sama dengan waktu ekstraksi keypoint)
"""
import argparse
import math
import sys
import time
from collections import deque
from pathlib import Path

import numpy as np

# ---------------------------------------------------------------- pengaturan
KELAS = ["Elbow_Plank", "High_Plank", "Pushup", "Pushup_on_Knees", "Side_Plank"]
TARGET_FPS = 15          # data latih QEVD / multiview efektifnya 15 fps
MAX_MISSING = 0.2        # window dilewati kalau >20% frame tanpa pose (sama dengan training)
HOLD_SEC = 1.0           # lama 5 jari harus ditahan
HOLD_GRACE = 0.3         # toleransi deteksi tangan sempat hilang sebentar
SMOOTH_N = 5             # rata-rata dari N prediksi terakhir supaya tidak berkedip
KELAS_MIN_CONF = 0.5     # di bawah ini tampil "belum yakin"
FORM_BAIK = 0.6          # P(good) >= ini  -> "tampak baik"
FORM_BURUK = 0.4         # P(good) <= ini  -> "perlu diperbaiki"; di antaranya "belum yakin"
POSE_LOST_SEC = 2.0      # pose hilang selama ini -> minta user mundur / perbaiki posisi

DEFAULT_MODEL_DIRS = [
    Path(r"C:\Users\hanse\OneDrive\Documents\MOVIT\Dataset\outputs"),
]


# ------------------------------------------------- fitur (sama dengan notebook)
def angles(X, sudut_def):
    """(N, T, 33, 2) -> (N, T, 8) sudut dalam derajat"""
    out = []
    for _, a, b, c in sudut_def:
        ba, bc = X[:, :, a] - X[:, :, b], X[:, :, c] - X[:, :, b]
        cos = (ba * bc).sum(-1) / (np.linalg.norm(ba, axis=-1) * np.linalg.norm(bc, axis=-1) + 1e-6)
        out.append(np.degrees(np.arccos(np.clip(cos, -1, 1))))
    return np.stack(out, -1).astype(np.float32)


def summarize(A):
    """(N, T, 8) -> (N, 40): min, max, mean, std, range tiap sudut"""
    return np.concatenate([A.min(1), A.max(1), A.mean(1), A.std(1), A.max(1) - A.min(1)], axis=1)


def prep_window(buf):
    """list of (33,2) -> (T,33,2) siap pakai, atau None kalau terlalu banyak frame tanpa pose"""
    w = np.stack(buf).astype(np.float32)
    miss = np.isnan(w).any(axis=(1, 2))
    if miss.all() or miss.mean() > MAX_MISSING:
        return None
    if miss.any():                      # isi frame kosong dengan interpolasi (sama dengan training)
        import pandas as pd
        w = (pd.DataFrame(w.reshape(len(w), -1)).interpolate(limit_direction="both")
             .to_numpy().reshape(w.shape)).astype(np.float32)
    return w


def proba_dict(model, F):
    """predict_proba -> vektor ukuran jumlah kelas penuh (urutan model.classes_)"""
    return model.classes_, model.predict_proba(F)[0]


# ---------------------------------------------------------- deteksi 5 jari
def _d(a, b):
    return math.hypot(a[0] - b[0], a[1] - b[1])


def tangan_terbuka(lm):
    """lm: 21 titik (x, y) MediaPipe Hands. True kalau kelima jari terentang.
    Jari = ujung lebih jauh dari pergelangan daripada sendi PIP (tidak bergantung arah tangan)."""
    w = lm[0]
    for tip, pip in ((8, 6), (12, 10), (16, 14), (20, 18)):
        if _d(lm[tip], w) < _d(lm[pip], w) * 1.1:
            return False
    # jempol: ujung (4) harus lebih jauh dari pangkal kelingking (17) daripada sendi IP (3)
    return _d(lm[4], lm[17]) > _d(lm[3], lm[17]) * 1.1


class Mesin:
    """MENUNGGU -> BERSIAP -> BERJALAN. step() dipanggil tiap frame."""

    def __init__(self, countdown=5.0, hold=HOLD_SEC, grace=HOLD_GRACE, tanpa_gestur=False):
        self.countdown, self.hold, self.grace = countdown, hold, grace
        self.tanpa_gestur = tanpa_gestur
        self.reset()

    def reset(self):
        self.state = "MENUNGGU"
        self.hold_start = None
        self.last_open = None
        self.count_start = None

    def step(self, buka, now):
        """buka: apakah 5 jari terlihat. Return (state, progres_tahan 0..1, sisa_hitung_mundur detik)."""
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


# ------------------------------------------------------------ model & analisis
def cari_model_dir(arg):
    kandidat = ([Path(arg)] if arg else []) + [
        Path(__file__).resolve().parent, Path(__file__).resolve().parent / "outputs",
        Path("outputs"), *DEFAULT_MODEL_DIRS]
    for d in kandidat:
        if (d / "kelas_model.pkl").exists():
            return d
    sys.exit("kelas_model.pkl tidak ketemu. Taruh kelas_model.pkl (dan kualitas_model.pkl) di folder yang sama "
             "dengan skrip ini, atau pakai --model-dir <folder>.")


class Analisis:
    """Buffer 30 frame -> prediksi gerakan (+ form untuk Pushup), dihaluskan beberapa prediksi terakhir."""

    def __init__(self, kel, kq):
        self.kel, self.kq = kel, kq
        self.window = int(kel.get("window", 30))
        self.stride = int(kel.get("stride", 10))
        self.sudut = kel["sudut"]
        self.names = kel.get("kelas", KELAS)
        self.buf = deque(maxlen=self.window)
        self.baru = 0
        self.p_kelas = deque(maxlen=SMOOTH_N)
        self.p_good = deque(maxlen=SMOOTH_N)
        self.valid = True

    def reset(self):
        self.buf.clear(); self.baru = 0; self.p_kelas.clear(); self.p_good.clear()

    def tambah(self, kp):
        """kp: (33,2) atau None (pose tidak terdeteksi). Prediksi dijalankan tiap `stride` sampel."""
        self.buf.append(np.full((33, 2), np.nan, np.float32) if kp is None else kp)
        self.baru += 1
        if len(self.buf) < self.window or self.baru < self.stride:
            return
        self.baru = 0
        w = prep_window(self.buf)
        self.valid = w is not None
        if w is None:
            return
        F = summarize(angles(w[None], self.sudut))
        cls, p = proba_dict(self.kel["model"], F)
        full = np.zeros(len(self.names), np.float32)
        full[np.asarray(cls, dtype=int)] = p
        self.p_kelas.append(full)
        if self.kq is not None and self.names[int(np.argmax(full))] == "Pushup":
            cq, pq = proba_dict(self.kq["model"], F)
            self.p_good.append(float(pq[list(cq).index(1)]) if 1 in list(cq) else 0.0)

    def kelas(self):
        """(nama, keyakinan) atau (None, 0) kalau belum ada prediksi / belum yakin"""
        if not self.p_kelas:
            return None, 0.0
        p = np.mean(self.p_kelas, axis=0)
        i = int(np.argmax(p))
        return (self.names[i], float(p[i])) if p[i] >= KELAS_MIN_CONF else (None, float(p[i]))

    def form(self):
        """'baik' / 'perbaiki' / 'ragu' / None (belum ada / bukan pushup)"""
        if not self.p_good:
            return None, 0.0
        g = float(np.mean(self.p_good))
        return ("baik" if g >= FORM_BAIK else "perbaiki" if g <= FORM_BURUK else "ragu"), g


# ------------------------------------------------------------------ tampilan
def teks(cv2, img, s, y, warna=(255, 255, 255), skala=0.8, tebal=2):
    cv2.putText(img, s, (20, y), cv2.FONT_HERSHEY_SIMPLEX, skala, (0, 0, 0), tebal + 3, cv2.LINE_AA)
    cv2.putText(img, s, (20, y), cv2.FONT_HERSHEY_SIMPLEX, skala, warna, tebal, cv2.LINE_AA)


def main():
    ap = argparse.ArgumentParser(description="Deteksi workout real-time dengan kamera")
    ap.add_argument("--model-dir", help="folder berisi kelas_model.pkl dan kualitas_model.pkl")
    ap.add_argument("--gerakan", choices=KELAS, help="gerakan yang dipilih user (opsional)")
    ap.add_argument("--kamera", type=int, default=0, help="nomor kamera (default 0)")
    ap.add_argument("--countdown", type=float, default=5.0, help="detik bersiap setelah 5 jari (default 5)")
    ap.add_argument("--tanpa-gestur", action="store_true", help="lewati 5 jari, langsung hitung mundur")
    a = ap.parse_args()

    import cv2, joblib, mediapipe as mp
    d = cari_model_dir(a.model_dir)
    kel = joblib.load(d / "kelas_model.pkl")
    kq = joblib.load(d / "kualitas_model.pkl") if (d / "kualitas_model.pkl").exists() else None
    print("model dari:", d, "| model kualitas:", "ada" if kq else "tidak ada")

    cap = cv2.VideoCapture(a.kamera)
    if not cap.isOpened():
        sys.exit(f"kamera {a.kamera} tidak bisa dibuka (coba --kamera 1, dan tutup aplikasi lain yang memakai kamera)")

    mesin = Mesin(countdown=a.countdown, tanpa_gestur=a.tanpa_gestur)
    ana = Analisis(kel, kq)
    mp_hands, mp_pose, mp_draw = mp.solutions.hands, mp.solutions.pose, mp.solutions.drawing_utils
    last_sample, last_pose, ada_pose = 0.0, time.monotonic(), False

    with mp_hands.Hands(max_num_hands=1, min_detection_confidence=0.6, min_tracking_confidence=0.5) as hands, \
            mp_pose.Pose(min_detection_confidence=0.5) as pose:
        while cap.isOpened():
            ok, frame = cap.read()
            if not ok:
                break
            now = time.monotonic()
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
                teks(cv2, frame, "Tunjukkan 5 jari untuk mulai", 40)
                if a.gerakan:
                    teks(cv2, frame, f"Gerakan: {a.gerakan}", 75, (0, 255, 255), 0.7)
                cv2.rectangle(frame, (20, 95), (20 + int(300 * prog), 115), (0, 220, 0), -1)
                cv2.rectangle(frame, (20, 95), (320, 115), (255, 255, 255), 2)

            elif state == "BERSIAP":
                teks(cv2, frame, "Pindah ke posisi workout. Seluruh tubuh harus terlihat.", 40, (0, 255, 255), 0.7)
                teks(cv2, frame, str(int(math.ceil(sisa))), H // 2 + 40, (0, 255, 255), 5, 8)
                ana.reset(); last_pose = now

            else:   # BERJALAN
                if now - last_sample >= 1.0 / TARGET_FPS:
                    last_sample = now
                    res = pose.process(rgb)
                    ada_pose = res.pose_landmarks is not None
                    if ada_pose:
                        last_pose = now
                        mp_draw.draw_landmarks(frame, res.pose_landmarks, mp_pose.POSE_CONNECTIONS)
                        kp = np.array([[l.x, l.y] for l in res.pose_landmarks.landmark], np.float32)
                    else:
                        kp = None
                    ana.tambah(kp)

                nama, conf = ana.kelas()
                if len(ana.buf) < ana.window and not ana.p_kelas:
                    teks(cv2, frame, f"Mengumpulkan gerakan... {len(ana.buf)}/{ana.window}", 40, (200, 200, 200), 0.7)
                elif now - last_pose > POSE_LOST_SEC:
                    teks(cv2, frame, "Tubuh tidak terlihat. Mundur / sesuaikan kamera.", 40, (0, 165, 255), 0.7)
                elif not ana.valid:
                    teks(cv2, frame, "Video kurang jelas, coba sesuaikan posisi.", 40, (0, 165, 255), 0.7)
                elif nama is None:
                    teks(cv2, frame, "Gerakan: belum yakin", 40, (0, 165, 255))
                else:
                    teks(cv2, frame, f"Gerakan: {nama} ({conf:.0%})", 40, (0, 255, 0))
                    if a.gerakan and nama != a.gerakan:
                        teks(cv2, frame, f"Belum terlihat seperti {a.gerakan}", 75, (0, 165, 255), 0.7)
                    elif nama == "Pushup" and kq is not None:
                        f, g = ana.form()
                        if f == "baik":
                            teks(cv2, frame, "Form tampak sudah baik", 75, (0, 255, 0), 0.75)
                        elif f == "perbaiki":
                            teks(cv2, frame, "Form tampak perlu diperbaiki", 75, (0, 140, 255), 0.75)
                        elif f == "ragu":
                            teks(cv2, frame, "Form belum bisa dipastikan", 75, (0, 255, 255), 0.75)
                    else:
                        teks(cv2, frame, "Penilaian form untuk gerakan ini belum tersedia", 75, (200, 200, 200), 0.6)

            teks(cv2, frame, "r: ulang | q: keluar | Ini saran form, bukan penilaian pasti", H - 15, (220, 220, 220), 0.5, 1)
            cv2.imshow("Workout Detector", frame)
            k = cv2.waitKey(1) & 0xFF
            if k in (ord("q"), 27):
                break
            if k == ord("r"):
                mesin.reset(); ana.reset()

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
