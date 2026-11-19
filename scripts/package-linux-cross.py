#!/usr/bin/env python3
"""Create a Linux x86_64 transfer package, preserving Unix modes and JRE links.

Uses already-built Java and Linux artifacts; never executes Linux binaries on
Windows. No installer or public release is created.
"""
import argparse
import copy
import hashlib
import io
import json
import os
from pathlib import Path, PurePosixPath
import posixpath
import re
import shutil
import struct
import subprocess
import sys
import tarfile
import tempfile
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parent.parent


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def require_elf(data, name):
    if data[:6] != b"\x7fELF\x02\x01" or len(data) < 20 or struct.unpack_from("<H", data, 18)[0] != 62:
        raise ValueError("Expected Linux x86_64 ELF: " + str(name))


def sources():
    output = subprocess.check_output(["git", "-C", str(ROOT), "ls-files", "-z", "--cached", "--others", "--exclude-standard"])
    result = {}
    for name in sorted(set(output.decode("utf-8").split("\0"))):
        if not re.match(r"^(src/|native/|scripts/|docs/|examples/|\.github/|[^/]+\.(md|xml|bat)$|LICENSE$|\.gitignore$|\.gitattributes$|\.editorconfig$|\.clang-format$)", name):
            continue
        if re.search(r"(^|/)(target|build|\.git|\.aws|\.codex|\.settings|__pycache__)/|\.(exe|dll|class|jar|zip|pdb|obj|key|pfx|pem|pyc)$", name):
            continue
        path = ROOT / name
        if path.is_symlink():
            raise ValueError("Source symlink requires review: " + name)
        if path.is_file():
            result[name] = digest(path)
    return result


def package(args):
    if args.output.exists():
        raise ValueError("Choose a fresh output directory; packages are never overwritten")
    runtime = json.loads(args.runtime_metadata.read_text(encoding="utf-8-sig"))
    if digest(args.runtime) != runtime["binary"]["package"]["checksum"]:
        raise ValueError("Linux runtime checksum mismatch")
    binary = runtime["binary"]
    if (binary["os"], binary["architecture"], binary["image_type"]) != ("linux", "x64", "jdk"):
        raise ValueError("Runtime is not a Linux x64 JDK (javac is required)")
    if digest(args.runtime_sources) != runtime["source"]["checksum"]:
        raise ValueError("Runtime source checksum mismatch")
    compiler=json.loads(args.compiler_metadata.read_text(encoding="utf-8-sig"))
    if "zig-x86_64-linux-0.15.2.tar.xz" not in compiler["tarball"] or digest(args.compiler)!=compiler["shasum"]:
        raise ValueError("Linux C++ compiler identity/checksum mismatch")
    namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
    version = ET.parse(ROOT / "pom.xml").findtext("m:version", namespaces=namespace)
    jar_name = "easy-knack-" + version + ".jar"
    jar = args.java_build / jar_name
    snapshot = sources()
    commit = subprocess.check_output(["git", "-C", str(ROOT), "rev-parse", "HEAD"], text=True).strip()
    args.output.mkdir(parents=True)
    work_root = ROOT / "target/linux-package-work"
    work_root.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=work_root) as temporary:
        stage = Path(temporary) / "EasyKnack"
        stage.mkdir()
        def put(source, target):
            destination = stage / target
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)
        put(jar, jar_name)
        with zipfile.ZipFile(jar) as archive:
            manifest = archive.read("META-INF/MANIFEST.MF").decode().replace("\r\n ", "").replace("\n ", "")
            dependencies = re.search(r"(?m)^Class-Path: (.+)$", manifest).group(1).strip().split()
            identity = archive.read("META-INF/easyknack/build.properties").decode()
            with zipfile.ZipFile(io.BytesIO(archive.read("easyknack/plugin-sdk.zip"))) as sdk:
                for name in sdk.namelist():
                    source = ROOT / "native/audio-host" / name
                    if name.startswith(("src/", "third_party/")) and source.is_file() and sdk.read(name) != source.read_bytes():
                        raise ValueError("Embedded plugin SDK is stale; rerun Maven process-classes/package: " + name)
        selected = []
        for relative in dependencies:
            if not re.fullmatch(r"lib/[^/\\]+\.jar", relative):
                raise ValueError("Unsafe dependency path: " + relative)
            if "-natives-" in relative and "-natives-linux-amd64" not in relative:
                continue
            path = args.java_build / relative
            put(path, relative)
            selected.append({"file": relative, "sha256": digest(path)})
            with zipfile.ZipFile(path) as archive:
                for entry in archive.infolist():
                    if entry.filename.endswith(".so"):
                        require_elf(archive.read(entry), relative + ":" + entry.filename)
                    if re.search(r"(?i)(license|copying|notice)", entry.filename) and not entry.is_dir():
                        target = stage / "licenses" / path.stem / re.sub(r"[^a-zA-Z0-9._-]", "_", entry.filename)
                        target.parent.mkdir(parents=True, exist_ok=True)
                        target.write_bytes(archive.read(entry))
        for name in ("easyknack-audio-host", "libmp3lame.so.0", "tests/CommandReaderTest", "tests/AudioGraphTest",
                     "tests/PortablePlatformTest", "tests/MidiPortBankTest", "tests/LameRecordingTest",
                     "tests/LinuxAudioTest", "tests/LinuxDesktopTest", "tests/libplatform-fixture.so"):
            path = args.native / name
            require_elf(path.read_bytes()[:64], name)
            put(path, "native/"+name if not name.startswith("tests/") else name)
        put(args.native / "cross-build.json", "cross-build.json")
        put(ROOT / "native/audio-host/tests/HostProtocolTest.py", "tests/HostProtocolTest.py")
        put(ROOT / "native/audio-host/tests/PlatformLibraryFixture.cpp", "tests/PlatformLibraryFixture.cpp")
        put(ROOT / "scripts/linux/NativeCompilationCheck.java", "tests/NativeCompilationCheck.java")
        put(ROOT / "scripts/linux/clang++", "tools/zig/bin/clang++")
        for source, name in [("start-easyknack.sh", "Start-EasyKnack.sh"), ("check-linux.sh", "Check-Linux.sh")]:
            put(ROOT / "scripts/linux" / source, name)
        (stage / "application-jar.txt").write_text(jar_name + "\n", encoding="utf-8", newline="\n")
        (stage / "installation.properties").write_text("format=1\nplatform=linux-x86_64\njava=bundled\n", encoding="ascii")
        put(ROOT / "LICENSE", "LICENSE")
        put(ROOT / "THIRD-PARTY.md", "THIRD-PARTY.md")
        put(ROOT / "scripts/linux/README-test.md", "README.md")
        shutil.copytree(ROOT / "src/main/resources/easyknack/notices", stage / "licenses/upstream")
        for library in ("miniaudio", "dr_mp3"):
            source = (ROOT / "native/audio-host/third_party" / (library + ".h")).read_text(encoding="utf-8")
            marker = source.rfind("This software is available as a choice of the following licenses.")
            if marker < 0:
                raise ValueError("Missing " + library + " license")
            folder = stage / "licenses" / library
            folder.mkdir(parents=True)
            (folder / "LICENSE.txt").write_text(source[marker:].replace("*/", "").strip() + "\n", encoding="utf-8")
        for relative in ("LICENSE", "lib/libcxx/LICENSE.TXT", "lib/libcxxabi/LICENSE.TXT", "lib/libunwind/LICENSE.TXT"):
            put(args.zig_directory / relative, "licenses/zig-runtime/" + relative.replace("/", "_"))
        for name in ("COPYING", "AUTHORS", "PROVENANCE.md", "OOURA-LICENSE.txt"):
            put(ROOT / "native/audio-host/third_party/loris" / name, "licenses/loris/" + name)
        for name in ("alsa","pulse","lame"):
            for notice in ("COPYING","LICENSE","LGPL","GPL"):
                path=ROOT / "native/audio-host/third_party" / name / notice
                if path.is_file():put(path,"licenses/"+name+"/"+notice)
            put(ROOT / "native/audio-host/third_party" / name / "PROVENANCE.json", "licenses/"+name+"/PROVENANCE.json")
        java = args.java.resolve()
        classpath = str(jar.resolve()) + os.pathsep + str((args.java_build / "lib/*").resolve())
        for arguments in ([str(stage / "Examples")], ["--verify", str(stage / "Examples")]):
            subprocess.run([str(java), "-Deasyknack.language=en", "-cp", classpath,
                            "com.vin.easyknack.ui.ExampleLibrary", *arguments], check=True)
        # Include the actual EasyKnack sources and the external Core source JAR.
        source_zip = stage / "sources/EasyKnack-project-sources.zip"
        source_zip.parent.mkdir()
        with zipfile.ZipFile(source_zip, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for name in snapshot:
                archive.write(ROOT / name, "easy-knack/" + name)
            archive.writestr("source-snapshot.json", json.dumps({"commit": commit, "files": snapshot}, indent=2))
        put(args.glswing_sources, "sources/" + args.glswing_sources.name)
        put(args.runtime_sources, "sources/" + runtime["source"]["name"])
        metadata = {"version": version, "sourceCommit": commit, "buildIdentity": identity,
                    "platform": "linux-x86_64", "linuxExecution": "not performed; run Check-Linux.sh on target",
                    "runtime": runtime, "compiler":compiler, "libraries": selected,
                    "glswingSourceSha256": digest(args.glswing_sources)}
        (stage / "package-build.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
        if snapshot != sources():
            raise RuntimeError("Sources changed while packaging; build again from a stable source state")
        archive_path = args.output / ("EasyKnack-" + version + "-linux-x86_64-test.tar.gz")
        written=set()
        def clean(info):
            info.uid = info.gid = 0
            info.uname = info.gname = ""
            relative = info.name.removeprefix("EasyKnack/")
            info.mode = 0o755 if info.isdir() or relative.endswith(".sh") or relative in ("native/easyknack-audio-host","tools/zig/bin/clang++") or relative.startswith("tests/") and "." not in relative.rsplit("/",1)[-1] else 0o644
            written.add(info.name.rstrip("/"))
            return info
        with tarfile.open(archive_path, "w:gz") as output:
            output.add(stage, arcname="EasyKnack", filter=clean)
            for source_archive,prefix,elfs in [(args.runtime,"EasyKnack/runtime",{"bin/java","bin/javac"}),
                                               (args.compiler,"EasyKnack/tools/zig",{"zig"})]:
              with tarfile.open(source_archive, "r:*") as source:
                for original in source:
                    parts = PurePosixPath(original.name).parts
                    if not parts or PurePosixPath(original.name).is_absolute() or ".." in parts:
                        raise ValueError("Unsafe runtime archive path")
                    entry = copy.copy(original)
                    relative="/".join(parts[1:])
                    entry.name = prefix + ("/" + relative if relative else "")
                    if entry.name.rstrip("/") in written:
                        if entry.isdir():continue
                        raise ValueError("Duplicate dependency archive entry: "+entry.name)
                    written.add(entry.name.rstrip("/"))
                    entry.uid = entry.gid = 0
                    entry.uname = entry.gname = ""
                    if entry.issym():
                        target = posixpath.normpath(posixpath.join(posixpath.dirname(entry.name), entry.linkname))
                        if not target.startswith(prefix+"/"):
                            raise ValueError("Dependency symlink escapes its archive root")
                    elif entry.islnk():
                        entry.linkname = prefix+"/" + "/".join(PurePosixPath(entry.linkname).parts[1:])
                    elif not (entry.isfile() or entry.isdir()):
                        raise ValueError("Unsupported runtime archive entry")
                    data = source.extractfile(original) if entry.isfile() else None
                    if relative in elfs:
                        require_elf(data.read(64), entry.name)
                        data.seek(0)
                    output.addfile(entry, data)
        checksum = digest(archive_path)
        archive_path.with_suffix(archive_path.suffix + ".sha256").write_text(checksum + "  " + archive_path.name + "\n", encoding="ascii")
        subprocess.run([sys.executable, str(ROOT / "scripts/verify-linux-package.py"), str(archive_path)], check=True)
        print("Linux test package: " + str(archive_path.resolve()))
        print("SHA256: " + checksum)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java-build", "native", "runtime", "runtime-sources", "runtime-metadata", "compiler", "compiler-metadata", "zig-directory", "java", "glswing-sources", "output"):
        parser.add_argument("--" + name, type=Path, required=True)
    package(parser.parse_args())
