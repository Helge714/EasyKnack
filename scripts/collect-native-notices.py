"""Refresh native notices from checksum-locked source archives (Python 3.11+).

Run after downloading the entries in sources.lock.json. Original text bytes are
preserved; provenance records archive/member and the copied text's SHA-256.
"""
import hashlib
import json
import pathlib
import re
import tarfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
RULES = {
    "lame-3.100": ["COPYING", "LICENSE"],
    "speex-1.2.1": ["COPYING", "AUTHORS"],
    "opus-1.3.1": ["COPYING", "AUTHORS"],
    "opencore-amr-0.1.6": ["LICENSE", "opencore/NOTICE"],
    "vo-amrwbenc-0.1.3": ["COPYING", "NOTICE", "common/NOTICE", "amrwbenc/NOTICE"],
    "zlib-1.3.1": ["LICENSE"],
    "openssl-3.3.2": ["LICENSE.txt", "AUTHORS.md"],
    "openh264-2.4.1": ["LICENSE"],
    "libvpx-1.14.1": ["LICENSE", "PATENTS", "AUTHORS"],
    "freetype-2.13.3": ["LICENSE.TXT", "docs/FTL.TXT", "docs/GPLv2.TXT"],
    "mfx-dispatch-1.35.1": ["LICENSE"],
    "libxml2-2.9.12": ["COPYING", "Copyright", "AUTHORS"],
    "srt-1.5.3": ["LICENSE"],
    "libwebp-1.4.0": ["COPYING", "PATENTS", "AUTHORS"],
    "libaom-3.9.1": ["LICENSE", "PATENTS", "AUTHORS"],
    "svt-av1-2.2.1": ["LICENSE.md", "LICENSE-BSD2.md", "PATENTS.md"],
    "zimg-3.0.5": ["COPYING"],
    "nv-codec-headers-12.2.72.0": ["include/ffnvcodec/" + name for name in
        ["dynlink_cuda.h", "dynlink_cuviddec.h", "dynlink_loader.h", "dynlink_nvcuvid.h", "nvEncodeAPI.h"]],
    "gcc-12.2.0": ["COPYING3", "COPYING.RUNTIME"],
    "make-4.4.1": ["COPYING"],
    "libffi": ["LICENSE"],
    "lldb-mi": ["LICENSE.TXT"],
    "cpython-mingw": ["LICENSE"],
    "llvm-project": [p + "/LICENSE.TXT" for p in
                     ["llvm", "clang", "clang-tools-extra", "lld", "lldb", "compiler-rt",
                      "libcxx", "libcxxabi", "libunwind", "openmp"]],
}


def collect():
    sources = {s["id"]: s for s in json.loads((ROOT / "scripts/sources.lock.json").read_text())["sources"]}
    output = ROOT / "src/main/resources/easyknack/notices"
    provenance = []
    for identity, paths in RULES.items():
        source = sources[identity]
        archive_path = ROOT / "target/source-materials" / source["file"]
        with archive_path.open("rb") as stream:
            if hashlib.file_digest(stream, "sha256").hexdigest() != source["sha256"]:
                raise ValueError(f"Source checksum mismatch: {identity}")
        pending = set(paths)
        with tarfile.open(archive_path, "r|*") as archive:
            for entry in archive:
                relative = "/".join(pathlib.PurePosixPath(entry.name).parts[1:])
                if relative not in pending or not entry.isfile():
                    continue
                data = archive.extractfile(entry).read()
                excerpt = relative.endswith(".h")
                if excerpt:
                    comment = re.match(rb"\s*/\*.*?\*/", data, re.DOTALL)
                    if comment is None or b"Copyright" not in comment.group():
                        raise ValueError(f"Missing leading copyright notice: {entry.name}")
                    data = comment.group() + b"\n"
                name = "native-" + identity + "-" + re.sub(r"[^a-zA-Z0-9._-]", "_", relative) + ".txt"
                (output / name).write_bytes(data)
                provenance.append(dict(file=name, source=source["file"], member=entry.name, leadingCommentOnly=excerpt,
                                       sha256=hashlib.sha256(data).hexdigest()))
                pending.remove(relative)
                if not pending:
                    break
        if pending:
            raise ValueError(f"Missing notices in {identity}: {sorted(pending)}")
    (output / "index.txt").write_text("\n".join(sorted(p.name for p in output.iterdir()
        if p.is_file() and p.suffix != ".pdf" and p.name != "index.txt")) + "\n", encoding="utf-8")
    report = ROOT / "docs/development/source-audit/native-notices.json"
    report.write_text(json.dumps(provenance, indent=2) + "\n", encoding="utf-8")
    print(f"Collected {len(provenance)} native notice files with exact source provenance.")


if __name__ == "__main__":
    collect()
