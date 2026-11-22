# macOS port and acceptance

Target: macOS 14.2+, Apple Silicon arm64 and Intel x86_64, separate packages.
EasyKnack remains 0.4.0-SNAPSHOT; this compatible platform feature is covered by
the existing next minor version. No document, Formula ABI or host protocol bump.
GLSwing remains an external Maven dependency; no Metal renderer is introduced.

## Platform boundary

`platform/MacPlatform.mm` is the only Objective-C++ translation unit. It implements
Core Audio device discovery, microphone authorization, private Core Audio process
taps, volume and CoreMIDI, selected through `MacHost.hpp` / `HostPlatform.hpp`.
`RoutedAudio` owns channel selection, graph execution, bounded rings, single-DSP
ownership and secondary clocks. Its `DeviceDriver` boundary has no Apple types.
`RoutedVolume` follows desktop volume changes, not global key interception.

Core Audio hardware exposes consecutive channel pairs. Default and named aliases
share one stream; a final mono channel is duplicated on input and downmixed on
output. Each device uses float PCM at 48 kHz; miniaudio handles conversion to the
device format/rate. The graph remains 256 frames. Independent devices use adaptive
rings and may have different latency. Device, permission and open failures are
reported; missing inputs can be retried after refreshing the list.

System/application taps require macOS 14.2 and Audio Capture permission. They are
private aggregate devices, destroyed after stopping capture. System taps exclude
the host PID. Application selection groups Core Audio process objects by bundle
ID, or executable path for processes without a bundle. Copy does not mute the
application; exclusive rerouting remains external. Microphone requests return a
pending-permission error immediately rather than blocking the host command loop.

CoreMIDI hardware/virtual source identities use upstream unique IDs. MIDI 1 input
supports running status and interleaved realtime bytes; Learn and keyboard input
use the shared `MidiPortBank`. Reconnect/stop sends panic. Core Audio/MIDI setup
notifications are processed on the host control loop; default-device changes
rebuild affected routes. Explicit refresh also retries unavailable sources.
This is MIDI 1, not a new MIDI 2/UMP public contract.

The replaceable LAME dylib is located by `EASYKNACK_NATIVE_LIBDIR`, set by the Java
host launcher. This also supports locally built Bake/plugin hosts outside the app
and avoids relying on DYLD variables stripped by hardened-runtime launches.

## Build on macOS

Install Xcode or Apple's Command Line Tools, Maven, CMake and Python 3.12+. Do not
redistribute Apple's SDK. Native Formula/Bake/source-plugin builds on a user's Mac
also require the Command Line Tools; the packaged wrapper reports this requirement.
The normal Formula interpreter remains usable without native optimization.

```sh
python3 scripts/prepare-macos-inputs.py --output target/macos-inputs
export JAVA_HOME="$(cat target/macos-inputs/java-home.txt)"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f target/macos-inputs/core/pom.xml -pl glswing-core -am -Dmaven.test.skip=true install
sh scripts/build-posix.sh
mvn -Dmaven.test.skip=true package
python3 scripts/package-macos.py --java-build target --native target/native-macos --inputs target/macos-inputs --output dist/macos-new
```

The prerequisite step verifies public input hashes and obtains the matching full
JDK, OpenJDK sources, Temurin build scripts/configuration/SBOM and frozen external
Core build inputs. The package contains the full JDK (javac included), current
project/Core sources, native dependency notices and all sixteen examples. The
compiler wrapper invokes the installed Apple toolchain; no compiler/SDK is copied.
Review the actual Mac distribution and its hashes before public publication;
the earlier Linux-specific audit is not a Mac licensing certificate.

## App, signing and distribution

`package-macos.py` builds `EasyKnack.app` and an architecture-labelled DMG. Finder
file associations feed the existing atomic launch inbox; native Quit uses the
window's close request and unsaved-document flow. The Info.plist provides Retina,
minimum-system and Microphone/Audio Capture descriptions. No App Store sandbox is
enabled. Entitlements permit HotSpot JIT, user-built JNI/Formula modules and audio
input. These choices need real signed-package acceptance, not just a build pass.
Common Command editing/file shortcuts map to the existing Control bindings only
inside the GL canvas; native dialogs and Command-Q retain native handling.

Default packages are ad-hoc signed test builds. For Developer ID use
`--sign-identity 'Developer ID Application: ...'`; add `--notary-profile NAME` only
after setting up a local notarytool keychain profile. The script signs inside-out,
verifies signatures and optionally notarizes/staples the app and DMG. Credentials
are not stored in the repository or passed as raw secrets. Re-signing the bundled
JDK changes signature metadata; preserve upstream legal texts and corresponding
sources and do not claim the signed runtime is byte-identical to the download.

`.github/workflows/macos.yml` builds/tests both architectures on real macOS runners
and uploads experimental DMGs plus diagnostics. It has contents-read permission
only and never publishes releases, notarizes or uses signing secrets. A push to
`codex/macos-port` starts it; pushing requires user scope under AGENTS.md.

## Required acceptance

Local Windows checks cover `RoutedAudioTest`, MIDI byte decoding, Java platform/
compiler policy and document launch forwarding. They do not compile Apple APIs.
The workflow must compile the actual Objective-C++ adapter and run native DSP,
null-host/protocol, real Formula/Bake/plugin/cache and JOGL/AWT context checks.
`MacMidiTest` uses a real temporary CoreMIDI source; unavailable CoreMIDI on a runner
is an explicit skip (77), not hardware success. `Check-Mac.command` records tests
from the packaged app layout under `~/Library/Logs/EasyKnack`.

Physical Mac acceptance is still required: interface channel mapping, multiple
devices/clock drift, latency, disconnect/reconnect, physical MIDI/Learn, both
permission grant/deny paths, system/app capture samples and exclusion of own audio,
MP3 round trips, baked playback, Retina/theme/editor rendering, Finder open and
Quit with unsaved documents. Check unsigned/ad-hoc and Developer ID notarized
downloads separately. No macOS execution or hardware pass is claimed from Windows.
