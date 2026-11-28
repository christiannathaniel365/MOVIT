"""Match dataset_index.csv to the real video files, so the videos can stay in 3 raw folders.

If the index has a `file` column (made by add_video_names.py), every row is matched by its real file name
(a leading "Copy of " is ignored). Otherwise the index has only an `id`, and every row is matched by what the
index does know about it. Either way every match is VERIFIED against the real file (frame count / fps):

  multiview : subject + quality + camera view, read from the file path (subject_005_push_up_good_side.mp4)
  pushup    : fingerprint = (fps, number of frames), plus good/bad if it appears in the path
  qevd      : the id number, tested with several hypotheses (file name number, position in the sorted
              folder); the hypothesis that passes the frame-count check on >= 90% of rows wins

Usage:
  python build_manifest.py --index dataset_index.csv --qevd D:/data/qevd --pushup D:/data/pushup \
                           --multiview "D:/data/7 workout"
Any folder can be left out. Writes outputs/manifest.csv (every index row + a `path` column) and prints a report.
"""
import argparse
import re
import sys
from pathlib import Path

import numpy as np
import pandas as pd

VIDEO_EXT = {".mp4", ".mov", ".avi", ".mkv", ".webm"}
FPS_TOL = 0.05        # fps must match within this
QEVD_FRAME_TOL = 2    # QEVD frames are resampled (30 -> 15 fps), allow rounding differences
ACCEPT_RATE = 0.90    # a QEVD hypothesis is accepted if this share of rows passes verification


# ---------------------------------------------------------------- file helpers
def natural_key(s):
    return [int(t) if t.isdigit() else t for t in re.split(r"(\d+)", s.lower())]


def list_videos(root):
    files = [p for p in Path(root).rglob("*") if p.is_file() and p.suffix.lower() in VIDEO_EXT]
    return sorted(files, key=lambda p: natural_key(p.as_posix()))


_INFO = {}


def video_info(path):
    """(fps, n_frames) read from the file header; nothing is decoded, so it is fast"""
    if path not in _INFO:
        import cv2
        cap = cv2.VideoCapture(str(path))
        _INFO[path] = (float(cap.get(cv2.CAP_PROP_FPS) or 0.0), int(cap.get(cv2.CAP_PROP_FRAME_COUNT) or 0))
        cap.release()
    return _INFO[path]


def tokens(path):
    return [t for t in re.split(r"[^a-z0-9]+", path.as_posix().lower()) if t]


def quality_hint(path):
    t = set(tokens(path))
    if "good" in t and "bad" not in t:
        return "good"
    if "bad" in t and "good" not in t:
        return "bad"
    return None


# ---------------------------------------------------------------- by real file name
def norm_name(name):
    n = re.sub(r"^copy of ", "", str(name).lower().strip())
    return re.sub(r"\s+", " ", n)


def match_by_file(rows, files):
    """match on the `file` column. If several files share a name, `folder` / good-bad in the path decide."""
    index = {}
    for p in files:
        index.setdefault(norm_name(p.name), []).append(p)
    mapping, ambiguous, missing = {}, 0, 0
    for i, r in rows.iterrows():
        c = index.get(norm_name(r.file), []) if pd.notna(r.file) else []
        if not c:
            missing += 1
            continue
        if len(c) > 1 and isinstance(r.get("folder"), str) and r["folder"]:
            c = [p for p in c if r["folder"].lower() in p.as_posix().lower()] or c
        if len(c) > 1 and r.get("kualitas") in ("good", "bad"):
            c = [p for p in c if quality_hint(p) == r["kualitas"]] or c
        if len(c) == 1:
            mapping[i] = (c[0], "file name")
        else:
            ambiguous += 1
    return mapping, f"by file name: {len(mapping)} matched, {missing} names not found in the folder, {ambiguous} ambiguous"


# ---------------------------------------------------------------- multiview
def match_multiview(rows, files):
    """key = (subject, quality, view); only push-up files are considered"""
    index, dup = {}, 0
    for p in files:
        s = p.as_posix().lower()
        if not re.search(r"push[_\- ]?up", s):
            continue
        m = re.search(r"subject[_\- ]?0*(\d+)", s)
        t = set(tokens(p))
        q = "good" if "good" in t else "bad" if "bad" in t else None
        v = next((x for x in ("front", "side", "diagonal") if x in t), None)
        if not (m and q and v):
            continue
        key = (int(m.group(1)), q, v)
        if key in index:
            dup += 1
        else:
            index[key] = p
    mapping = {}
    for i, r in rows.iterrows():
        p = index.get((int(r.subjek), r.kualitas, r.view))
        if p is not None:
            mapping[i] = (p, "subject+quality+view")
    return mapping, f"push-up files recognised: {len(index)}" + (f" (duplicates ignored: {dup})" if dup else "")


# ---------------------------------------------------------------- pushup (100 clips)
def fp_ok(info, row, tol):
    fps, n = info
    return abs(fps - row.fps_asli) <= FPS_TOL and abs(n - row.n_frames_video) <= tol


def match_pushup(rows, files):
    info = {p: video_info(p) for p in files}
    hint = {p: quality_hint(p) for p in files}
    best = (-1, None, {})
    for tol in (0, 1, 2):
        cand = {p: [i for i, r in rows.iterrows()
                    if fp_ok(info[p], r, tol) and (hint[p] is None or hint[p] == r.kualitas)] for p in files}
        assigned, used = {}, set()
        changed = True
        while changed:                                   # 1) files with exactly one candidate row
            changed = False
            for p in files:
                c = [i for i in cand[p] if i not in used]
                if p not in assigned and len(c) == 1:
                    assigned[p] = (c[0], "fingerprint"); used.add(c[0]); changed = True
        for p in files:                                  # 2) ties where all candidates share one quality: harmless
            c = [i for i in cand[p] if i not in used]
            if p not in assigned and c and rows.loc[c, "kualitas"].nunique() == 1:
                assigned[p] = (min(c), "fingerprint-tie"); used.add(min(c))
        if len(assigned) > best[0]:
            best = (len(assigned), tol, assigned)
        if len(assigned) >= 0.95 * len(rows):
            break
    n, tol, assigned = best
    mapping = {i: (p, m) for p, (i, m) in assigned.items()}
    unresolved = [p.name for p in files if p not in assigned]
    note = f"frame tolerance used: {tol}"
    if unresolved:
        note += f" | files not matched ({len(unresolved)}): {unresolved[:6]}{' ...' if len(unresolved) > 6 else ''}"
    return mapping, note


# ---------------------------------------------------------------- qevd (1500 clips)
def qevd_expected_frames(info, row):
    fps, n = info
    return n * row.fps_efektif / fps if fps > 0 else np.nan


def match_qevd(rows, files):
    num = rows.id.str.extract(r"(\d+)")[0].astype(int)
    by_stem, by_any = {}, {}
    for p in files:
        digits = re.findall(r"\d+", p.stem)
        if p.stem.isdigit():
            by_stem.setdefault(int(p.stem), p)
        for d in digits:
            by_any.setdefault(int(d), []).append(p)
    hypotheses = {
        "file name is the id number": lambda k: by_stem.get(k),
        "id number appears in file name": lambda k: (by_any.get(k) or [None])[0] if len(by_any.get(k, [])) == 1 else None,
        "id = position in sorted folder (from 0)": lambda k: files[k] if 0 <= k < len(files) else None,
        "id = position in sorted folder (from 1)": lambda k: files[k - 1] if 1 <= k <= len(files) else None,
    }
    results = {}
    for name, fn in hypotheses.items():
        mapping, ok = {}, 0
        for i, r in rows.iterrows():
            p = fn(int(num[i]))
            if p is None:
                continue
            mapping[i] = (p, f"qevd: {name}")
            exp = qevd_expected_frames(video_info(p), r)
            ok += bool(abs(exp - r.frames) <= QEVD_FRAME_TOL)
        results[name] = (ok / len(rows), mapping)
    lines = [f"   {rate:5.0%} verified  <- {name}" for name, (rate, _) in results.items()]
    name, (rate, mapping) = max(results.items(), key=lambda kv: kv[1][0])
    if rate < ACCEPT_RATE:
        note = ("NO hypothesis reached " + f"{ACCEPT_RATE:.0%}" + ":\n" + "\n".join(lines) +
                "\n   first files in folder: " + str([p.name for p in files[:5]]) +
                "\n   first ids in index   : " + str(rows.id.head(5).tolist()))
        return {}, note
    return mapping, f"accepted: {name}\n" + "\n".join(lines)


# ---------------------------------------------------------------- main
def verify(row, path, sumber):
    fps, n = video_info(path)
    if sumber == "qevd":
        return bool(abs(qevd_expected_frames((fps, n), row) - row.frames) <= QEVD_FRAME_TOL)
    return bool(abs(fps - row.fps_asli) <= FPS_TOL and abs(n - row.n_frames_video) <= 1)


def build(index_csv, folders, out_csv):
    idx = pd.read_csv(index_csv)
    out = idx.copy()
    out["path"], out["match_method"], out["verified"] = pd.NA, "", pd.NA
    matchers = {"multiview": match_multiview, "pushup": match_pushup, "qevd": match_qevd}
    lines = []
    for sumber, fn in matchers.items():
        folder = folders.get(sumber)
        if not folder:
            continue
        rows = idx[idx.sumber == sumber]
        files = list_videos(folder)
        if "file" in idx.columns and rows.file.notna().any():
            mapping, note = match_by_file(rows, files)
            if len(mapping) < ACCEPT_RATE * len(rows):          # names did not line up: fall back to the other strategies
                alt, alt_note = fn(rows, files)
                if len(alt) > len(mapping):
                    mapping, note = alt, note + "\n   too few -> used the id/fingerprint strategy instead:\n   " + alt_note
        else:
            mapping, note = fn(rows, files)
        used = set()
        for i, (p, method) in mapping.items():
            out.at[i, "path"] = p.resolve().as_posix()
            out.at[i, "match_method"] = method
            out.at[i, "verified"] = verify(idx.loc[i], p, sumber)
            used.add(p)
        sub = out.loc[rows.index]
        lines.append(f"\n[{sumber}] index rows: {len(rows)} | video files in folder: {len(files)}\n"
                     f"   matched: {sub.path.notna().sum()} | verified against the file: {int((sub.verified == True).sum())}"
                     f" | rows without a video: {sub.path.isna().sum()} | folder files not in index: {len(files) - len(used)}\n"
                     f"   {note}")
    Path(out_csv).parent.mkdir(parents=True, exist_ok=True)
    out.to_csv(out_csv, index=False)
    print("\n".join(lines))
    print(f"\nwritten: {out_csv}")
    return out


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--index", default="dataset_index.csv")
    ap.add_argument("--qevd")
    ap.add_argument("--pushup")
    ap.add_argument("--multiview")
    ap.add_argument("--out", default="outputs/manifest.csv")
    a = ap.parse_args()
    if not (a.qevd or a.pushup or a.multiview):
        sys.exit("give at least one of --qevd / --pushup / --multiview")
    build(a.index, {"qevd": a.qevd, "pushup": a.pushup, "multiview": a.multiview}, a.out)
