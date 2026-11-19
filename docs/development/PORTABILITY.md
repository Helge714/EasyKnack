# Native platform boundary

Linux is the first additional target. macOS follows the same boundary; neither
platform is a released or hardware-validated distribution yet.

## Ownership

1. Java / external GLSwing owns the interface, documents and graph compilation.
   `NativePlatform` selects artifact names, architecture/cache identity and compiler
   policy; GUI widgets do not invoke platform audio APIs.
2. The C++ host owns graph execution, DSP and the authenticated loopback protocol.
   DSP borrows `AudioIO` and `MidiSource`; host control code uses `HostServices`.
3. Platform services own device callbacks, MIDI discovery, endpoint volume, control
   sockets, native module loading and worker thread policy. Selection happens in
   `native/audio-host/src/platform/HostPlatform.hpp`.

This is a service boundary inside the existing host, not another process or a
replacement rendering toolkit. GLSwing still uses OpenGL. A future Metal renderer
belongs in GLSwing and is independent of the audio device backend.

`WindowsHostServices` adapts the existing WASAPI/ASIO routing, Windows MIDI and
volume implementations. `DefaultAudio` uses miniaudio's platform backend selection
(e.g. Linux audio services or Core Audio). `KeyboardMidi` provides portable computer
keyboard events. `ControlSocket`, `SharedLibrary` and `ThreadPriority` isolate the
other host OS calls. Existing guarded recording/codec implementations remain;
MP3 recording still depends on Windows Media Foundation.

## Current capability boundary

| Capability | Windows backend | Initial portable backend |
| --- | --- | --- |
| Audio playback/capture | Existing endpoint routing | Default stereo output and optional default stereo input |
| Sample rate / graph block | 48 kHz / 256 frames | Same; miniaudio converts device formats/rates |
| MIDI | Windows devices and computer keyboard | Computer keyboard only |
| Process/system capture, ASIO | Existing support | Not advertised; unsupported routes are rejected |
| Endpoint volume / media keys | Existing support | Unavailable; Windows volume controls hidden |
| Formula native modules | DLL | Linux SO / macOS dylib; ABI checked before use |
| Worker priority | Existing Windows policy | Default OS policy; no implicit realtime permission changes |
| Device refresh | Existing support | No automatic device reselection/hotplug recovery yet |

The portable device callback renders the graph directly; Windows render-ahead is
unchanged. Real-device latency, CPU load and capture/playback synchronisation need
Linux acceptance measurements. No multiple-device clock bridging is implemented
in this initial backend. WAV recording and existing portable decoders remain;
portable MP3 encoding is a separate task.

Host protocol 13, Formula ABI 6, document format and plugin contracts are unchanged.
Native caches are platform/architecture-specific and must not be copied between
operating systems. Old Windows endpoint/MIDI selections can be unavailable on
Linux; select the default devices/computer keyboard explicitly.

## Build and checks

On Linux install a JDK compatible with the Maven project (Java 17+), Maven, Python
3, CMake 3.20+ and a C++17 compiler. Provide the external GLSwing Maven dependency
as for the Windows development build. No sibling GLSwing source checkout is used.

Run `sh scripts/build-posix.sh` from the checkout. It generates contracts with
Maven, configures/builds CMake, runs CTest and places the host beside the development
Java classes at `target/native/easyknack-audio-host`. `MVN`, `CXX` and `BUILD_JOBS`
can select tools/parallelism. Native runtime audio libraries/services must be
available on the machine. This script builds/checks the host; it does not produce
a Linux installer, bundle a JRE or validate the GUI/display dependencies.

CTest includes the existing DSP and command reader suites plus portable default
device/MIDI lifecycle, native library loading and a real protocol handshake/graph
exchange through `--null`. The Python registry generator replaces PowerShell in
CMake builds. `python3 scripts/test-native-generator.py` tests malformed and
duplicate registrations and deterministic output.

On Windows the regular `scripts/build-audio-host.ps1` path remains. CMake option
`EASYKNACK_PORTABLE_HOST=ON` exercises the portable audio/MIDI backend on Windows;
it does not test POSIX socket/loader code. The null backend needs no audio hardware.

Local validation on 2026-11-19 uses Windows; Linux/WSL is not installed on the
development machine. Both host variants pass the null-device protocol check;
the existing Windows WASAPI, ASIO, DSP, shared transport and driver bridge suites
pass. CMake configuration/build and portable-platform/command-reader CTest checks
pass on Windows. Native Formula compilation/execution and platform policy tests
also pass. These checks do not execute POSIX branches or physical audio devices.
Java SDK packaging, host-client/service and routing checks pass as well; four
device/broader audio cases remain disabled in the focused host-client run.
A real Linux build, GUI launch and audio/MIDI device
acceptance are still required before advertising Linux support. macOS additionally
needs a real SDK build on Intel/Apple Silicon, GUI/OpenGL validation, device
acceptance and distribution signing/notarization. CMake/framework and dylib policy
are prepared, not evidence that those checks have passed.
