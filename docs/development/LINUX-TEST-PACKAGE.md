# Linux test package from Windows

This path creates Linux x86_64 ELF executables and a transfer archive without WSL,
a Linux VM, a remote runner or a push to GitHub. It is a cross-build, not execution
on Linux. Keep it labelled as a test package until device/GUI acceptance succeeds.

## Tools and inputs

- Existing Windows Java/Maven setup and the external GLSwing Maven dependency.
- Python 3.11+ (standard library only).
- Official [Zig 0.15.2 Windows x86_64 archive](https://ziglang.org/download/0.15.2/zig-x86_64-windows-0.15.2.zip),
  SHA256 `3a0ed1e8799a2f8ce2a6e6290a9ff22e6906f8227865911fb7ddedc3cc14cb0c`.
  Extract into ignored `.local/tools`. Its C++ frontend/library/linker target
  `x86_64-linux-gnu.2.28`; no host CPU-specific optimisation is requested.
- An official Linux x64 Temurin JDK 25 `.tar.gz` and the corresponding asset JSON
  from `https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=linux&vendor=eclipse`.
  Save one asset object as UTF-8 JSON, not the enclosing response array. The packager
  checks its platform and SHA256, preserves the runtime archive's executable modes
  and symlinks, and retains the exact asset metadata in the finished package.
- The source JAR matching the external GLSwing binary used by Maven.
- The matching Temurin OpenJDK source archive and its SHA256. Add the release's
  `source` object (including `name`, `link` and verified `checksum`) to the saved
  runtime asset JSON; obtain it from the official feature-release API and source
  asset checksum file. The packager includes and checks the source archive too.

Do not extract the Linux JDK on Windows: permission bits and symlinks can be lost.
Do not substitute a musl/static host: miniaudio and Formula need dynamic loading.

The matching Linux Zig archive from the official download index is also required
for the transfer package. Save its URL/SHA256/size metadata as UTF-8 JSON and pass
`--compiler` / `--compiler-metadata`; its unmodified contents and notices are retained.

## Build

1. Run Maven `package` with an isolated `easyknack.buildDirectory` such as
   `target/linux-java`. Run relevant Java tests before packaging, or retain their
   recorded evidence; `-Dmaven.test.skip=true` is only a packaging shortcut.
2. Run the cross compiler (replace paths with the actual tools):

   ```powershell
   python scripts/build-linux-cross.py --zig .local/tools/zig-linux-cross/zig-x86_64-windows-0.15.2/zig.exe --contracts target/linux-java/native/generated
   ```

   Output defaults to `target/native-linux-cross`. Host, LAME and seven native test
   executables plus a shared-library fixture are built sequentially. Logs and
   target/compiler metadata remain beside them. Tests are not executed on Windows.
3. Create a fresh transfer archive:

   ```powershell
   python scripts/package-linux-cross.py --java-build target/linux-java --native target/native-linux-cross --runtime target/linux-cross/runtime.tar.gz --runtime-sources target/linux-cross/runtime-sources.tar.gz --runtime-metadata target/linux-cross/runtime-download.json --compiler target/linux-parity/zig-linux.tar.xz --compiler-metadata target/linux-parity/zig-linux-download.json --zig-directory .local/tools/zig-linux-cross/zig-x86_64-windows-0.15.2 --java C:/path/to/jdk/bin/java.exe --glswing-sources C:/path/to/glswing-0.2.0-SNAPSHOT-sources.jar --output dist/linux-test-new
   ```

The product version comes from Maven. A new destination is required; existing
packages are never overwritten. Only manifest-declared dependencies and Linux
amd64 JogAmp natives are included. Sixteen examples are generated and verified;
Linux JDK/compiler, licenses, EasyKnack working-tree sources, external Core sources and build identity
are included. `.tar.gz` retains the Unix permissions needed for direct startup.
The packager checks the archive's ELF architecture, modes, runtime links, selected
libraries, examples, sources and SHA256. Rerun independently with
`python scripts/verify-linux-package.py <archive>`.

## Test on the other computer

Transfer the `.tar.gz` and checksum. Extract the complete archive into a writable
local folder. Run `./Check-Linux.sh`, then `./Start-EasyKnack.sh` in the `EasyKnack`
folder. Return `linux-diagnostics.txt`, startup output if needed, and the listening
results. Python 3 is optional on the target, but needed for the protocol/Unicode
audio-import check. GUI and physical audio acceptance remain manual.

This package assumes a glibc-based Linux x86_64 desktop, X11/XWayland and OpenGL 3.
It is not for ARM, Alpine/musl or a headless GUI session. The included JDK and Zig C++ wrapper support
Formula, Bake and source plugins without separate compiler installation. Desktop
audio/capture requires PulseAudio or PipeWire-Pulse; hardware MIDI requires ALSA.
`Check-Linux.sh` includes real desktop integration and compilation/cache tests.
Its first compiler run can take several minutes and prints live progress. Existing platform limitations are in [PORTABILITY.md](PORTABILITY.md).

## Distribution license review

An experimental label does not waive redistribution terms. Before publishing a new
archive, collect the exact JDK build metadata and Temurin build-source revision,
matching Core build inputs and any updated notices. Run `scripts/audit-linux-licenses.py`
against the immutable binary archive, exact upstream JDK/Zig archives, Core inputs
and isolated rebuilt Core JAR; use `--help` for its required paths. Publish the
resulting evidence and source/build materials beside the binary download. The
OpenJDK source archive already inside the binary archive must remain available too.
See [test 1's component ledger and remediation](source-audit/LINUX-TEST-1-2026-11-19.md).
