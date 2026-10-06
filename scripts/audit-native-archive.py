"""Compare an installed native distribution against an official upstream ZIP.

Read-only; never extracts or runs archive contents. Requires Python 3.11+.
Usage: python audit-native-archive.py upstream.zip installed-directory report.json
"""
import hashlib
import json
import pathlib
import sys
import zipfile


def digest(stream):
    return hashlib.file_digest(stream, "sha256").hexdigest()


def audit(archive_path, installed):
    with archive_path.open("rb") as stream:
        archive_hash = digest(stream)
    expected = {}
    with zipfile.ZipFile(archive_path) as archive:
        for entry in archive.infolist():
            if entry.is_dir():
                continue
            parts = pathlib.PurePosixPath(entry.filename).parts
            if len(parts) < 2 or ".." in parts or entry.filename.startswith("/"):
                raise ValueError(f"Unsafe or unrooted archive entry: {entry.filename}")
            path = "/".join(parts[1:])
            if path in expected:
                raise ValueError(f"Duplicate archive entry: {path}")
            with archive.open(entry) as stream:
                expected[path] = digest(stream)
    actual = {}
    for path in sorted(installed.rglob("*")):
        if path.is_symlink():
            raise ValueError(f"Unexpected installed symlink: {path}")
        if path.is_file():
            with path.open("rb") as stream:
                actual[path.relative_to(installed).as_posix()] = digest(stream)
    missing = sorted(expected.keys() - actual.keys())
    extra = sorted(actual.keys() - expected.keys())
    changed = sorted(p for p in expected.keys() & actual.keys() if expected[p] != actual[p])
    # Use the same sorted relative-path/hash encoding in future rechecks.
    tree = hashlib.sha256("".join(f"{p}\0{actual[p]}\n" for p in sorted(actual)).encode()).hexdigest()
    return dict(archive=archive_path.name, archiveSha256=archive_hash,
                expectedFiles=len(expected), installedFiles=len(actual),
                installedTreeSha256=tree, missing=missing, extra=extra, changed=changed,
                exactMatch=not (missing or extra or changed))


if __name__ == "__main__":
    archive_path, installed, output = map(pathlib.Path, sys.argv[1:])
    report = audit(archive_path, installed)
    output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    sys.exit(0 if report["exactMatch"] else 1)
