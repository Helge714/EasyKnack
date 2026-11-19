#!/usr/bin/env python3
"""Check Linux transfer archive contents without executing target binaries."""
import argparse
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import posixpath
import tarfile
import zipfile


def verify(path):
    with tarfile.open(path, "r:gz") as archive:
        members = archive.getmembers()
        entries = {member.name.rstrip("/"): member for member in members}
        if len(entries) != len(members):
            raise ValueError("Duplicate archive entries")
        for entry in members:
            parts = PurePosixPath(entry.name).parts
            if not parts or parts[0] != "EasyKnack" or ".." in parts or entry.name.startswith("/"):
                raise ValueError("Unsafe package entry: " + entry.name)
            if entry.issym():
                target = posixpath.normpath(posixpath.join(posixpath.dirname(entry.name), entry.linkname))
                if not target.startswith(("EasyKnack/runtime/","EasyKnack/tools/zig/")) or target not in entries:
                    raise ValueError("Broken/escaping runtime symlink: " + entry.name)
        def data(name,limit=-1):
            return archive.extractfile("EasyKnack/" + name).read(limit)
        for name in ("Start-EasyKnack.sh", "Check-Linux.sh", "native/easyknack-audio-host",
                     "runtime/bin/java", "runtime/bin/javac", "tools/zig/zig", "tools/zig/bin/clang++",
                     "tests/CommandReaderTest", "tests/AudioGraphTest", "tests/PortablePlatformTest",
                     "tests/MidiPortBankTest", "tests/LameRecordingTest", "tests/LinuxAudioTest", "tests/LinuxDesktopTest"):
            if not entries["EasyKnack/" + name].mode & 0o111:
                raise ValueError("Missing executable permissions: " + name)
            if name.endswith(".sh") or name.endswith("/clang++"):
                if b"\r" in data(name):
                    raise ValueError("Windows line endings in shell script: " + name)
            else:
                content = data(name,64)
                if content[:6] != b"\x7fELF\x02\x01" or content[18:20] != b"\x3e\x00":
                    raise ValueError("Not x86_64 Linux ELF: " + name)
        jars = [name for name in entries if name.startswith("EasyKnack/lib/") and name.endswith(".jar")]
        native_jars = [name for name in jars if "-natives-" in name]
        if len(native_jars) != 2 or any("-natives-linux-amd64" not in name for name in native_jars):
            raise ValueError("Expected exactly the two Linux amd64 JogAmp native JARs")
        jar = data("application-jar.txt").decode().strip()
        if b"\r" in data("application-jar.txt"):
            raise ValueError("Windows line endings in the shell launcher JAR marker")
        with zipfile.ZipFile(io.BytesIO(data(jar))) as java:
            if "META-INF/easyknack/build.properties" not in java.namelist():
                raise ValueError("Missing build identity")
        examples = [name for name in entries if name.startswith("EasyKnack/Examples/") and name.endswith(".knack")]
        if len(examples) != 16:
            raise ValueError("Expected the sixteen bundled examples")
        for name in ("LICENSE", "THIRD-PARTY.md", "README.md", "sources/EasyKnack-project-sources.zip",
                     "Examples/README-de.md", "Examples/README-en.md", "tests/HostProtocolTest.py",
                     "licenses/zig-runtime/lib_libcxx_LICENSE.TXT", "runtime/legal/java.base/LICENSE"):
            if not data(name):
                raise ValueError("Empty required package file: " + name)
        if not any(name.startswith("EasyKnack/sources/glswing-") and name.endswith("-sources.jar") for name in entries):
            raise ValueError("Missing external GLSwing sources")
        info = json.loads(data("package-build.json"))
        if info["runtime"]["binary"]["image_type"]!="jdk" or "x86_64-linux-0.15.2" not in info["compiler"]["tarball"]:
            raise ValueError("Missing target JDK/compiler metadata")
        if data("native/libmp3lame.so.0",6)!=b"\x7fELF\x02\x01":
            raise ValueError("Missing Linux MP3 encoder")
        runtime_source = info["runtime"]["source"]
        if hashlib.sha256(data("sources/" + runtime_source["name"])).hexdigest() != runtime_source["checksum"]:
            raise ValueError("Missing or mismatched Java sources")
        if info["platform"] != "linux-x86_64" or "not performed" not in info["linuxExecution"]:
            raise ValueError("Incorrect platform/validation metadata")
    with path.open("rb") as stream:
        checksum = hashlib.file_digest(stream, "sha256").hexdigest()
    declared = path.with_suffix(path.suffix + ".sha256").read_text().split()[0]
    if checksum != declared:
        raise ValueError("Package checksum mismatch")
    print("Linux archive verified: ELF target, executable modes, runtime links, libraries, examples, sources and checksum.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    verify(parser.parse_args().archive)
