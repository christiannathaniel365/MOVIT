"""Put the real video file names back into dataset_index.csv (and dataset_windows.csv).

Why this is possible: the cleaning pipeline already knows the file of every id. It keeps it in
  Log/meta_pushup_mv.csv    (Pushup + Multi-View: column `path`)
  Log/qevd_terpilih.csv     (QEVD, the 1500 chosen videos: column `path`)
but the step that builds dataset_index.csv selects only some columns and drops `path`.
This script merges it back, WITHOUT re-running any extraction.

The `id` column is kept (the keypoint files, the .npz and the logs all use it). New columns:
  file      real video file name            e.g. subject_005_push_up_good_side.mp4
  folder    the folder it sits in           e.g. side
  src_path  the full original path

Usage:
  python add_video_names.py --index dataset_index.csv --meta meta_pushup_mv.csv --qevd qevd_terpilih.csv \
                            [--windows dataset_windows.csv] [--out-dir .]
Writes dataset_index_named.csv (and dataset_windows_named.csv).
"""
import argparse
import sys
from pathlib import Path

import pandas as pd

CHECK_COLS = ["sumber", "kelas", "kualitas", "view", "subjek"]


def clean(s):
    """comparable text: NaN and '' are the same, 5.0 and 5 are the same"""
    s = s.copy().astype(object).where(s.notna(), "")
    return s.map(lambda v: str(int(v)) if isinstance(v, float) and v == int(v) else str(v))


def split_path(p):
    parts = str(p).replace("\\", "/").split("/")
    return parts[-1], (parts[-2] if len(parts) > 1 else "")


def load_paths(files):
    parts = []
    for f in files:
        d = pd.read_csv(f)
        missing = {"id", "path"} - set(d.columns)
        if missing:
            sys.exit(f"{f}: missing column(s) {sorted(missing)}")
        parts.append(d)
    m = pd.concat(parts, ignore_index=True)
    if m.id.duplicated().any():
        sys.exit(f"duplicate ids in the path tables: {m.id[m.id.duplicated()].head().tolist()}")
    return m


def add_names(idx, m):
    # safety check: ids come from the sorted order of the files, so if the video list changed since the
    # index was built, ids would point to different videos. Compare the attributes both tables share.
    j = idx.merge(m, on="id", how="left", suffixes=("", "_meta"))
    for c in CHECK_COLS:
        if c in idx.columns and f"{c}_meta" in j.columns:
            # only compare where the path table actually has that attribute (the QEVD table has no `kualitas`)
            bad = (clean(j[c]) != clean(j[f"{c}_meta"])) & j[f"{c}_meta"].notna() & j["path"].notna()
            if bad.any():
                sys.exit(f"STOP: column '{c}' differs between the index and the path table for {int(bad.sum())} ids "
                         f"(e.g. {j.id[bad].head(3).tolist()}). The ids no longer point to the same videos, "
                         f"so names would be wrong. Use the path tables from the same run as the index.")
    names = j["path"].map(lambda p: split_path(p) if pd.notna(p) else (pd.NA, pd.NA))
    out = idx.copy()
    out["file"] = names.map(lambda t: t[0]).values
    out["folder"] = names.map(lambda t: t[1]).values
    out["src_path"] = j["path"].values
    cols = ["id", "file"] + [c for c in out.columns if c not in ("id", "file")]
    return out[cols]


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--index", required=True)
    ap.add_argument("--meta", help="Log/meta_pushup_mv.csv")
    ap.add_argument("--qevd", help="Log/qevd_terpilih.csv (or qevd_subset.csv)")
    ap.add_argument("--windows", help="dataset_windows.csv (optional)")
    ap.add_argument("--out-dir")
    a = ap.parse_args()
    if not (a.meta or a.qevd):
        sys.exit("give --meta and/or --qevd")

    idx = pd.read_csv(a.index)
    m = load_paths([f for f in (a.meta, a.qevd) if f])
    named = add_names(idx, m)

    out_dir = Path(a.out_dir) if a.out_dir else Path(a.index).parent
    out_dir.mkdir(parents=True, exist_ok=True)
    p1 = out_dir / "dataset_index_named.csv"
    named.to_csv(p1, index=False)

    no_file = named.file.isna()
    print(f"index rows: {len(named)} | with a real file name: {int((~no_file).sum())} | without: {int(no_file.sum())}")
    if no_file.any():
        print("  ids without a path:", named.id[no_file].head(8).tolist(), "...")
        print("  (a path table is missing: pass both --meta and --qevd)")
    dup = named[named.file.notna()].groupby(["sumber", "file"]).size()
    dup = dup[dup > 1]
    if len(dup):
        print(f"  note: {len(dup)} file names are used by more than one id (same name in different folders):")
        print(dup.head(5).to_string())
    print(named.groupby("sumber").file.agg(lambda s: s.dropna().iloc[0] if s.notna().any() else "").to_string())
    print("written:", p1)

    if a.windows:
        w = pd.read_csv(a.windows)
        key = "video_id" if "video_id" in w.columns else "id"
        w = w.merge(named[["id", "file", "folder"]].rename(columns={"id": key}), on=key, how="left")
        p2 = out_dir / "dataset_windows_named.csv"
        w.to_csv(p2, index=False)
        print(f"windows: {len(w)} | without file name: {int(w.file.isna().sum())} | written: {p2}")


if __name__ == "__main__":
    main()
