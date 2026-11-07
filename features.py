import numpy as np
import mediapipe as mp

mp_pose = mp.solutions.pose
L = mp_pose.PoseLandmark
MIN_VIS = 0.5   # landmarks below this visibility are treated as unreliable


def angle(a, b, c):
    """angle at point b formed by a-b-c, in degrees"""
    a, b, c = np.array(a), np.array(b), np.array(c)
    ba, bc = a - b, c - b
    cos = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc) + 1e-6)
    return float(np.degrees(np.arccos(np.clip(cos, -1, 1))))


def pt(lm, idx, w, h):
    return (lm[idx].x * w, lm[idx].y * h)   # scale by w,h so angles aren't distorted


def pick_side(lm):
    """which side of the body faces the camera (higher visibility)"""
    def vis(s):
        return np.mean([lm[getattr(L, f"{s}_{n}")].visibility
                        for n in ("SHOULDER", "HIP", "ANKLE")])
    return "LEFT" if vis("LEFT") >= vis("RIGHT") else "RIGHT"


def track_stats(lm):
    """body centre + torso size (normalised coords), used to detect tracking jumps"""
    sx = (lm[L.LEFT_SHOULDER].x + lm[L.RIGHT_SHOULDER].x) / 2
    sy = (lm[L.LEFT_SHOULDER].y + lm[L.RIGHT_SHOULDER].y) / 2
    hx = (lm[L.LEFT_HIP].x + lm[L.RIGHT_HIP].x) / 2
    hy = (lm[L.LEFT_HIP].y + lm[L.RIGHT_HIP].y) / 2
    return (sx + hx) / 2, (sy + hy) / 2, float(np.hypot(sx - hx, sy - hy))


def pushup_features(lm, w, h, side):
    names = ("SHOULDER", "ELBOW", "WRIST", "HIP", "KNEE", "ANKLE")
    if min(lm[getattr(L, f"{side}_{n}")].visibility for n in names) < MIN_VIS:
        return None
    g = lambda n: pt(lm, getattr(L, f"{side}_{n}"), w, h)
    sh, el, wr = g("SHOULDER"), g("ELBOW"), g("WRIST")
    hip, kn, an = g("HIP"), g("KNEE"), g("ANKLE")
    return {
        "elbow_angle": angle(sh, el, wr),
        "body_line": angle(sh, hip, an),   # ~180 = straight, low = sagging/piking
        "hip_angle": angle(sh, hip, kn),
        "knee_angle": angle(hip, kn, an),  # bent knees = cheating
    }


def squat_features(lm, w, h, side=None):
    names = ("LEFT_SHOULDER", "RIGHT_SHOULDER", "LEFT_HIP", "RIGHT_HIP",
             "LEFT_KNEE", "RIGHT_KNEE", "LEFT_ANKLE", "RIGHT_ANKLE")
    if min(lm[getattr(L, n)].visibility for n in names) < MIN_VIS:
        return None
    g = lambda n: pt(lm, getattr(L, n), w, h)
    lsh, rsh = g("LEFT_SHOULDER"), g("RIGHT_SHOULDER")
    lh, rh = g("LEFT_HIP"), g("RIGHT_HIP")
    lk, rk = g("LEFT_KNEE"), g("RIGHT_KNEE")
    la, ra = g("LEFT_ANKLE"), g("RIGHT_ANKLE")

    torso = np.hypot((lsh[0] + rsh[0] - lh[0] - rh[0]) / 2,
                     (lsh[1] + rsh[1] - lh[1] - rh[1]) / 2) + 1e-6
    ankle_w = max(abs(la[0] - ra[0]), 0.02 * w)   # guard against feet-together blow-up
    knee_y, hip_y = (lk[1] + rk[1]) / 2, (lh[1] + rh[1]) / 2
    return {
        "knee_angle_l": angle(lh, lk, la),
        "knee_angle_r": angle(rh, rk, ra),
        "knee_ankle_ratio": abs(lk[0] - rk[0]) / ankle_w,   # <1 = knees caving in
        "hip_ankle_ratio": abs(lh[0] - rh[0]) / ankle_w,
        "hip_depth": (knee_y - hip_y) / torso,              # ~0 at parallel, >0 = hips above knees
        "shoulder_tilt": abs(lsh[1] - rsh[1]) / torso,      # leaning sideways
        "hip_tilt": abs(lh[1] - rh[1]) / torso,
    }


EXTRACTORS = {"pushup": pushup_features, "squat": squat_features}
