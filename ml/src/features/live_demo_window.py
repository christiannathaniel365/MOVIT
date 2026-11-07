"""Live webcam demo using a rolling ~2 s window (matches the window model from the notebook).

Usage:  python live_demo_window.py pushup      (or: squat)
Needs:  features.py (written by the notebook, section 2)
        outputs/<exercise>_window_model.pkl (notebook, section 9b)
Keys:   q = quit
"""
import sys
import time
from collections import deque, Counter

import cv2
import joblib
import numpy as np
import pandas as pd
import mediapipe as mp

from features import mp_pose, pick_side, EXTRACTORS

SAMPLE_EVERY = 0.1    # seconds between samples (training used every 3rd frame at ~30 fps = 0.1 s)
SUFFIXES = ("_min", "_max", "_mean", "_std", "_range")


def base_features(cols):
    """feature names the window model was trained on, e.g. elbow_angle_mean -> elbow_angle"""
    base = []
    for c in cols:
        for s in SUFFIXES:
            if c.endswith(s):
                b = c[: -len(s)]
                if b not in base:
                    base.append(b)
                break
    return base


def window_row(buf, base, cols):
    """list of per-sample feature dicts -> one row of window features (same maths as the notebook)"""
    df = pd.DataFrame(list(buf))
    row = {}
    for c in base:
        v = df[c].to_numpy()
        row[f"{c}_min"], row[f"{c}_max"] = v.min(), v.max()
        row[f"{c}_mean"], row[f"{c}_std"] = v.mean(), v.std()
        row[f"{c}_range"] = v.max() - v.min()
    return pd.DataFrame([row])[cols]


def main():
    exercise = sys.argv[1] if len(sys.argv) > 1 else "pushup"
    bundle = joblib.load(f"outputs/{exercise}_window_model.pkl")
    model, cols, window = bundle["model"], bundle["cols"], bundle["window"]
    base = base_features(cols)
    good_idx = list(model.classes_).index(1)
    extract = EXTRACTORS[exercise]
    draw = mp.solutions.drawing_utils

    buf = deque(maxlen=window)        # rolling window of per-sample features
    recent = deque(maxlen=3)          # smoothing: average of the last 3 predictions
    side_votes, side = [], None       # push-up: lock the side from the first 30 detections
    last_sample, last_seen = 0.0, time.time()

    cap = cv2.VideoCapture(0)
    with mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
        while cap.isOpened():
            ok, frame = cap.read()
            if not ok:
                break
            now = time.time()
            h, w = frame.shape[:2]
            res = pose.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
            status, color, p_good = "no pose", (150, 150, 150), None

            if res.pose_landmarks:
                last_seen = now
                lm = res.pose_landmarks.landmark
                if exercise == "pushup" and side is None:
                    side_votes.append(pick_side(lm))
                    if len(side_votes) >= 30:
                        side = Counter(side_votes).most_common(1)[0][0]

                if now - last_sample >= SAMPLE_EVERY:
                    feats = extract(lm, w, h, side or pick_side(lm))
                    if feats is not None:
                        buf.append(feats)
                        last_sample = now

                if len(buf) < window:
                    status = f"warming up {len(buf)}/{window}"
                else:
                    p_good = float(model.predict_proba(window_row(buf, base, cols))[0][good_idx])
                    recent.append(p_good)
                    good = np.mean(recent) > 0.5
                    status, color = ("GOOD" if good else "BAD"), ((0, 200, 0) if good else (0, 0, 255))
                draw.draw_landmarks(frame, res.pose_landmarks, mp_pose.POSE_CONNECTIONS)
            elif now - last_seen > 1.0:
                buf.clear()           # person left the frame: start a fresh window
                recent.clear()

            cv2.putText(frame, status, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.3, color, 3)
            if p_good is not None:
                cv2.putText(frame, f"p(good) = {p_good:.2f}", (20, 90),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.8, color, 2)
            cv2.imshow("form check (window)", frame)
            if cv2.waitKey(1) & 0xFF == ord("q"):
                break
    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
