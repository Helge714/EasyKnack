# Native platform boundary

Linux is the first additional target. Functional Linux support is implemented;
GUI/physical-device acceptance still requires Linux. macOS follows the same
boundary and remains unvalidated.

## Ownership

1. Java / external GLSwing owns interface, documents and graph compilation.
   `NativePlatform` selects artifact/toolchain names and platform cache identity.
2. The C++ host owns DSP and authenticated loopback protocol. DSP borrows `AudioIO`
   and `MidiSource`; control code uses `HostServices`.
3. Platform services own devices, MIDI, volume, transport, loading and priority.
   `src/platform/HostPlatform.hpp` selects implementations.

GLSwing still uses OpenGL; a future Metal renderer is independent of audio.
No OS audio calls belong in GUI widgets or DSP node implementations.

## Feature mapping

| Capability | Windows | Linux |
| --- | --- | --- |
| Device selection / multiple endpoints | WASAPI/ASIO | PulseAudio / PipeWire-Pulse |
| Graph rate / block | 48 kHz / 256 frames | Same; server handles device conversion |
| Bursts / secondary clocks | Render-ahead / adaptive rings | Same shared primitives |
| Hardware/virtual MIDI, keyboard, Learn | WinMM / keyboard | ALSA sequencer / keyboard; `MidiPortBank` slots |
| System/application capture | Windows process loopback | Sink monitors / individual stream monitors; own PID excluded |
| Exclusive application rerouting | External tools; Copy in node | External desktop tools; Copy in node |
| Endpoint volume / meter | Windows endpoint | Pulse/PipeWire sink |
| Linked volume keys | Global keyboard hook | Follows desktop default-volume changes, including menu changes; preserves selected proportions |
| MP3 recording | Media Foundation | Replaceable bundled LAME 3.100 library |
| Formula / Bake / source plugins | Compiler and JDK | Bundled Linux Zig C++ wrapper and JDK with javac |
| Worker priority | Existing Windows policy | OS scheduling; no implicit privilege/RTKit changes |

Multichannel sources/sinks expose consecutive channel pairs with explicit native
positions and no server remixing into other channels. A final singleton uses mono
conversion. Alias selections share a physical stream. Application monitor data uses
the parent sink's layout, as required by the PulseAudio direct-monitor protocol.

ASIO vendor drivers and global hooks are OS-specific. Linux supplies corresponding
workflows through native services; latency, scheduling and MP3 bitstreams need not
be identical. Linked Linux keys observe desktop volume rather than intercepting
arbitrary Wayland global shortcuts. Windows overlays retain existing behavior.

`LinuxPulse` loads system `libpulse.so.0`; `LinuxAudio` owns topology/callbacks and
`LinuxVolume` owns volume policy. Only control operations wait (bounded to three
seconds); callbacks use prepared buffers/rings. Handover stops the DSP helper before
releasing streams. Application streams are grouped by binary and unavailable sources
are retried on refresh. Copy never suppresses original playback.

`LinuxMidi` loads `libasound.so.2` and uses a nonblocking ALSA sequencer reader.
Disconnect/overrun produces panic. Learn subscriptions refresh outside DSP. The
kernel sequencer must be accessible; missing ALSA never disables the keyboard.

Without the desktop audio service, startup reports the reason and falls back to
miniaudio default-device audio. That fallback has no device choice, loopback or
volume service and is not full desktop acceptance. Null-device tests deliberately
use it without hardware access.

MP3 uses 128 kbit/s mono / 192 kbit/s stereo, clipped input and a finalized gapless
tag. Shared upstream tables are serialized on recording workers; DSP only queues
packets. Original sources/config/notices accompany the separate library.
`GaplessMp3Decoder` supports UTF-8 and Windows wide paths.

Host protocol 13, Formula ABI 6, document and plugin contracts are unchanged.
Caches stay platform-specific. Reselect Windows device IDs on Linux. Formula builds
allow five minutes for initial portable-toolchain bootstrap, matching plugin builds.

## Build and validation

On Linux use Java 17+ JDK, Maven, Python 3, CMake 3.20+, C/C++17 and the external
GLSwing Maven dependency. `sh scripts/build-posix.sh` builds/checks host and LAME,
then copies them to `target/native`. Use `LD_LIBRARY_PATH=target/native` to launch
a development JAR if LAME is not installed system-wide. Package scripts set it.
Windows retains `scripts/build-audio-host.ps1`. The [cross-build/package path](LINUX-TEST-PACKAGE.md)
creates Linux ELF artifacts and a permission-preserving JDK/compiler/examples/source
archive without a VM or GitHub push.

- `MidiPortBankTest`: hardware-slot queues, keyboard, Learn and panic.
- `LinuxAudioTest` / `FakePulse`: actual adapter with deterministic server API;
  separate outputs, captured samples, app selection, aliases, volume and teardown.
- `LameRecordingTest`: real mono/stereo encoding, gapless decode/import, Unicode
  paths and collision protection, executable on Windows and Linux.
- `LinuxDesktopTest`: real server, temporary null sinks, independent producer,
  monitor/application capture and volume. Also tests an ALSA virtual subscription
  when available. CTest skips with 77 without a desktop server; package diagnostics
  report missing desktop acceptance instead of claiming a complete pass.
- `NativeCompilationCheck.java`: target Formula build/load/result/cache and real
  javac/source-plugin/Bake pipeline, including host handshake and build cache.
- Existing DSP, command, host, routing and SDK regressions remain.

`Check-Linux.sh` prints live progress and records diagnostics. This Windows machine
has no Linux runtime: Linux binaries are cross-built, not executed here. Pure
adapter/codec tests run on Windows. Desktop, physical MIDI/audio, latency, GUI/OpenGL
and long-running stability require target acceptance. macOS still needs its own
device/MIDI adapters, SDK builds, GUI validation and distribution work.
