
import sys, json
from pathlib import Path
import numpy as np

def main():
    job = json.load(open(sys.argv[1], encoding="utf-8"))
    kp_dir = Path(job["kp_dir"])
    T = float(job["target_fps"])
    import cv2, mediapipe as mp
    for it in job["items"]:
        out = kp_dir / (it["id"] + ".npy")
        if out.exists():
            continue
        (kp_dir / job["progress"]).write_text(it["id"], encoding="utf-8")   # penanda: video yang sedang diproses
        frames, i = [], 0
        cap = cv2.VideoCapture(it["path"])
        fps = it["fps"] or cap.get(cv2.CAP_PROP_FPS) or 30.0
        with mp.solutions.pose.Pose(min_detection_confidence=0.5) as pose:
            while cap.isOpened():
                ok, frame = cap.read()
                if not ok:
                    break
                i += 1
                if int(i * T / fps) == int((i - 1) * T / fps):     # ambil frame supaya hasilnya ~target fps
                    continue
                res = pose.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
                if res.pose_landmarks:
                    frames.append([[l.x, l.y] for l in res.pose_landmarks.landmark])
                else:
                    frames.append(np.full((33, 2), np.nan))
        cap.release()
        np.save(out, np.array(frames, dtype=np.float32).reshape(-1, 33, 2))
        print("OK", it["id"], len(frames), flush=True)

main()
