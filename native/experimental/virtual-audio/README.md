# Windows virtual audio driver development

**Deferred:** EasyKnack now prioritizes existing external routing tools through
WASAPI and ASIO. This experimental driver is not required by the audio host and
is not installed by the application. See `docs/development/features/external-audio-routing.md`.

The driver toolchain is separate from the portable C++ audio host and from Maven.
There are two builds: the unchanged Microsoft SysVAD reference and the EasyKnack
driver derived from that pinned source. The EasyKnack package builds and passes
WDK checks, but has not yet been installed or validated in a running kernel.

## Prerequisites

- Visual Studio Build Tools 2026: C++ tools, x64/x86 Spectre libraries, ATL/MFC
  Spectre libraries, and `Component.Microsoft.Windows.DriverKit.BuildTools`.
- Windows SDK and WDK with matching build number 28000.

Verified on this development machine on 2026-09-16: Visual Studio Build Tools
2026 18.10.1 (MSVC 14.51), SDK and WDK 10.0.28000.2526. The x64 Debug reference
build passed, including INF and Universal API validation. The Build Tools
installer returned 3010 (restart pending); SDK and WDK returned 0. Compilation
and validation succeeded before restarting.

Official setup documentation: https://learn.microsoft.com/windows-hardware/drivers/download-the-wdk

## Reference build

From the project root, run:

```powershell
./scripts/build-driver-reference.ps1
./scripts/build-driver-reference.ps1 -Configuration Release
```

The script uses 64-bit MSBuild (required by the current WDK's INF validator),
downloads the pinned Microsoft sources into
`target/driver-research/Windows-driver-samples`, verifies every Git blob hash,
and builds `EndpointsCommon` followed by `TabletAudioSample` for x64.
`-PrepareOnly` downloads/verifies without compiling. The manifest preserves the
upstream `LICENSE`; the source revision is recorded in that manifest.

This builds only the kernel sample, not the sample APOs or an installable product
package. Signing is disabled for this reference check. It does not install a
driver, create a test certificate, enable test signing, or change boot settings.
Do not install the upstream sample as an EasyKnack audio device: its generated
capture tone and render-file behavior still need replacement by the actual
EasyKnack adapter.

The experimental user-mode transport in `native/audio-host/src/audio/transport`
is not mapped into this driver. Kernel transport currently uses buffered IOCTLs
and driver-owned nonpaged PCM rings instead.

## EasyKnack driver build

```powershell
./scripts/build-virtual-audio.ps1
```

The script verifies upstream sources, creates a separate generated source tree in
`target/virtual-audio/source`, applies checked integration edits, and builds x64.
Keep edits in `native/virtual-audio` and the build script, not the generated tree.
The resulting **unsigned** package is `target/virtual-audio/Debug/package`:
`EasyKnackAudio.sys`, `.inf`, `.cat`, and the Microsoft source license.
INF validation, Universal API validation and Inf2Cat validation for Windows 11
22H2/24H2 run during the build. No test certificate or boot setting is changed.

The driver exposes only:

- **EasyKnack Input**: Windows playback endpoint, feeding the host.
- **EasyKnack Output**: Windows capture endpoint, receiving host audio.

Both use 48 kHz stereo PCM16. Windows handles application mixing/resampling.
Only one system stream per direction is supported; offload and loopback stream
instances are disabled. Bluetooth/USB sideband discovery and sample audio modules
are disabled. Test-tone generation and audio file writes are removed from stream
processing. DMA allocations are capped at 64 KiB. Protected render data is blocked
and any queued data discarded when protection is enabled.

## Kernel/host protocol

`BridgeProtocol.hpp` defines a fixed, versioned 2080-byte packet. The IOCTL uses
METHOD_BUFFERED and requires both read and write access. It carries at most 256
stereo PCM16 frames in each direction. Sizes, version, reserved fields and frame
counts are validated before ring access. No user pointers or writable shared
cursors reach the kernel. There is no floating-point processing in this bridge.

The control device `\\.\EasyKnackAudioBridge` permits SYSTEM and elevated
administrators only and accepts one open client. This is a development policy;
the normal unprivileged GUI does not open it. A later broker/service must provide
the production access boundary instead of broadening the device ACL.

Each direction has 2048 frames of fixed storage protected by a spin lock; an audio
transfer holds it for at most 256 frames at a time. Capture underruns become zeros;
render overflow drops new frames. Open/cleanup, stream release and run/pause
transitions clear the appropriate queues. A two-second host lease expires
terminally: a late client must close and reopen. Capture without a host is silent.

`WindowsDriverBridge.hpp` is the user-mode client. Its synchronous IOCTL belongs
on an IO worker, never in the DSP callback. The native build also produces
`target/native/driver-bridge-probe.exe`, which opens the device and checks ABI and
stream status without sending audio. It requires the installed test driver and
an elevated terminal. A missing driver is reported as an error, not simulated.

## Verification and remaining work

Automated: x64 Debug build and WDK package checks; protocol rejection and randomized
PCM ring tests against an independent FIFO model (30,000 operations, wraparound,
full/empty, bounds guards, stereo integrity, reset/silence); full Maven/native/GL
suite. These tests do **not** establish kernel runtime or acoustic correctness.

Before connecting this to normal playback, use a Windows test VM or test machine
to validate installation/signing, endpoint activation, duplex audio, pause/resume,
host crash/reconnect, device removal, power transitions and Driver Verifier.
The normal AudioBackend still does not advertise virtual endpoints or ROUTE.
A broker/IO worker, Float32/PCM conversion, clock handling, application reassignment
and restoration remain to be integrated. This is not a release-signed driver.

Test environment decision (2026-09-16): the user chose this development machine
after a later restart. Installation is deferred. Test-signing/boot changes still
require a concrete review of the machine state and explicit approval.

## Test signing after the toolchain restart

Run `./scripts/sign-virtual-audio-test.ps1` to create a separate package in
`target/virtual-audio/Debug/test-signed`. The script creates/reuses a non-exportable
development key in CurrentUser/My, embeds a SHA256 signature in the SYS, rebuilds
the catalog over that signed image, and signs the CAT. The exported CER contains
only the public certificate. It does not import trust, install a device or change
boot settings. This development certificate expires after six months.

Machine check on 2026-09-16 after restart: Secure Boot is enabled according to
UEFISecureBootEnabled=1; memory integrity is configured (Enabled=1). Direct BCD/UEFI
queries require an elevated Windows token, which the current shell lacks.
SYS and CAT signing succeeded. Authenticode/SignTool trust validation reports an
untrusted self-signed root, as expected before trust import.

Pending user decision: the conventional test-driver path here requires disabling
Secure Boot in firmware, enabling TESTSIGNING, and restarting. This relaxes driver
signature enforcement system-wide. Memory integrity need not be disabled merely
to use an embedded test signature. Check BitLocker/device-encryption recovery
access before changing firmware security settings; never save recovery secrets here.
After explicit approval, record original boot state, import only this test
certificate as needed, enable test mode and install/test the root device.
After testing, remove the EasyKnack device/package and test trust entries,
disable test mode, restore Secure Boot and verify the resulting state.

Microsoft reference: https://learn.microsoft.com/windows-hardware/drivers/install/the-testsigning-boot-configuration-option
