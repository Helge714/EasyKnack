#!/usr/bin/env python3
"""Build actual Linux ELF artifacts on Windows, without WSL or a VM.

Requires Zig 0.15.2 and Maven-generated parameter contracts. Tests are built for
the target and must be run on Linux; a successful cross-build is not a test run.
"""
import argparse
import json
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent
TARGET = "x86_64-linux-gnu.2.28"


def build(zig, destination, contracts):
    zig = zig.resolve()
    version = subprocess.check_output([str(zig), "version"], text=True).strip()
    if version != "0.15.2":
        raise ValueError("This cross-build path is validated with Zig 0.15.2")
    if not (contracts / "ParameterContracts.hpp").is_file():
        raise ValueError("Run Maven process-classes to generate native contracts first")
    destination.mkdir(parents=True, exist_ok=True)
    generated = destination / "generated"
    subprocess.run([sys.executable, str(ROOT / "scripts/generate-native-nodes.py"),
                    "--nodes", str(ROOT / "native/audio-host/src/nodes"),
                    "--output", str(generated)], check=True)
    native = ROOT / "native/audio-host"
    policy = dict(line.split("=", 1) for line in
                  (native / "build.properties").read_text().splitlines()
                  if line.strip() and not line.startswith("#"))
    command = [str(zig), "c++", "-target", TARGET, "-std=c++" + policy["cpp.standard"],
               *policy["linux.host.compile.flags"].split(), "-fno-sanitize=undefined",
               "-I", str(native / "third_party"), "-I", str(native / "src"),
               "-I", str(generated), "-I", str(contracts)]
    libraries = ["-l" + library for library in policy["linux.libraries"].split()]
    # The MP3 codec stays a replaceable LGPL library; it is not linked into DSP
    # kernels or Formula modules. Build the portable upstream C encoder only.
    lame=native / "third_party/lame"
    lame_objects=[]
    for source in sorted((lame / "libmp3lame").glob("*.c")):
        output=destination / ("lame-"+source.stem+".o")
        flags=[str(zig),"cc","-target",TARGET,"-std=gnu99","-O2","-fPIC","-DHAVE_CONFIG_H",
               "-I",str(lame/"easyknack"),"-I",str(lame/"include"),"-I",str(lame/"libmp3lame")]
        subprocess.run([*flags,"-c",str(source),"-o",str(output)],check=True)
        lame_objects.append(str(output))
    subprocess.run([str(zig),"cc","-target",TARGET,"-shared","-Wl,-soname,libmp3lame.so.0",
                    *lame_objects,"-lm","-o",str(destination/"libmp3lame.so.0")],check=True)
    def run(arguments, name):
        print("Building Linux " + name, flush=True)
        with (destination / (name + ".log")).open("w", encoding="utf-8") as log:
            process = subprocess.run(command + arguments, stdout=log, stderr=subprocess.STDOUT)
        if process.returncode:
            print((destination / (name + ".log")).read_text(encoding="utf-8", errors="replace"))
            raise RuntimeError("Cross compilation failed: " + name)
    objects = []
    for index, source in enumerate(sorted((native / "src/nodes").rglob("*.cpp"))):
        output = destination / ("node-" + str(index) + ".o")
        run(["-c", str(source), "-o", str(output)], source.stem)
        objects.append(str(output))
    run([str(native / "src/main.cpp"), *objects, "-o",
         str(destination / "easyknack-audio-host"), *libraries], "audio-host")
    tests = destination / "tests"
    tests.mkdir(exist_ok=True)
    for name, graph in [("PortablePlatformTest", True), ("AudioGraphTest", True),
                        ("CommandReaderTest", False), ("MidiPortBankTest", False), ("LameRecordingTest", False),
                        ("LinuxAudioTest", True), ("LinuxDesktopTest", True)]:
        run([str(native / "tests" / (name + ".cpp")), *(objects if graph else []),
             "-o", str(tests / name), *libraries], name)
    run(["-shared", "-fPIC", str(native / "tests/PlatformLibraryFixture.cpp"),
         "-o", str(tests / "libplatform-fixture.so"), *libraries], "platform-library")
    (destination / "cross-build.json").write_text(json.dumps({
        "target": TARGET, "compiler": "Zig " + version,
        "tests": "built for Linux; not executed on the Windows build host",
    }, indent=2) + "\n", encoding="utf-8")
    print("Linux ELF artifacts built in " + str(destination), flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--zig", type=Path, required=True)
    parser.add_argument("--output", type=Path, default=ROOT / "target/native-linux-cross")
    parser.add_argument("--contracts", type=Path, default=ROOT / "target/native/generated")
    args = parser.parse_args()
    build(args.zig, args.output.resolve(), args.contracts.resolve())
