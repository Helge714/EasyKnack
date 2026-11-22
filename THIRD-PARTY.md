# Licensing and third-party components

EasyKnack-authored code and documentation are licensed under GNU GPL version 3
only (SPDX: GPL-3.0-only), unless a file states otherwise. See LICENSE.
There is no additional plugin/linking exception. Existing third-party notices
and licenses are retained and are not replaced by EasyKnack's license.

GLSwing is a separately distributed Apache-2.0 library. Its license is unchanged.
This inventory identifies components for distribution review; it is not a claim
that all corresponding-source obligations have already been fulfilled.

The Linux experimental test 1 archive and its matching source/license supplement
are reviewed separately in [the Linux distribution report](docs/development/source-audit/LINUX-TEST-1-2026-11-19.md).
That report records exact artifact hashes, preserved notices, JDK build materials
and the isolated reconstruction of the current GLSwing runtime. It is a technical
distribution review, not a legal clearance certificate.

Linux test 2 supersedes the test 1 release page. Its source/license supplement
retains the complete original test 1 supplement under `legacy/` and supplies the
replacement GLSwing sources/build inputs and updated diagnostic scripts. The full
test 2 program archive includes this supplement as well. See the test 2 release
for the current downloads; test 1's historical Git tag remains available.

| Component | License / distribution notes |
|---|---|
| EasyKnack Java application, native host and original SDK code | GPL-3.0-only; provide corresponding source and build scripts for the distributed version. |
| GLSwing | Apache-2.0; retain its license and applicable notices from the external artifact. |
| Steinberg ASIO SDK | The vendored SDK offers GPLv3 or a separate proprietary agreement. EasyKnack uses the GPLv3 option. Preserve the SDK notices and COPYING. |
| miniaudio 0.11.21 | MIT-0 option, copyright David Reid; license text is in the vendored header and packaged notices. |
| Loris (Kelly Fitz and Lippold Haken) | GPL-2.0-or-later, used under GPLv3. Pinned upstream sources implement partial analysis; COPYING, AUTHORS, provenance and the bundled Ooura FFT permission notice are retained in the SDK and Windows package. No FFTW dependency. |
| dr_mp3 0.7.4 | MIT-0 option, copyright David Reid; includes minimp3 under CC0. Exact revision and checksum are in the native third-party README; complete upstream license statements are retained and packaged. |
| JogAmp JOGL / GlueGen and transitive JARs | Preserve the notices/licenses from each exact artifact; review bundled native components too. |
| External Windows JDK | Windows packages do not distribute Java. Users install a Windows x64 JDK 25 separately under its provider terms; the launcher can assist via winget. |
| Linux test-package JDK | The Linux test archive bundles an unmodified Eclipse Temurin JDK 25, including javac. Preserve its complete `runtime/legal` directory; exact asset metadata/checksums and matching OpenJDK sources are included. OpenJDK uses GPL-2.0 with the Classpath Exception plus its retained third-party terms. |
| macOS test-package JDK and launcher | Temurin JDK 25 and the OpenJDK jpackage launcher retain their GPL-2.0/ClassPath Exception, Assembly Exception and third-party notices. Full OpenJDK sources and exact Temurin build inputs accompany the package. macOS re-signing changes signature metadata; do not describe signed binaries as byte-identical to the upstream download. Apple's compiler/SDK remains an external prerequisite. |
| Linux C++ toolchain/runtime | Zig 0.15.2 is bundled for Formula/Bake/source plugins. Its MIT license and supplied library notices remain in `tools/zig`; LLVM libc++, libc++abi and libunwind use Apache-2.0 with LLVM Exceptions, also retained under `licenses/zig-runtime`. Native host/tests link the LLVM C++ runtime statically. Linux glibc and audio-service libraries come from the target system. |
| LAME 3.100 | LGPL-2.0-or-later. Linux MP3 encoding uses a separately replaceable `libmp3lame.so.0`, built from the retained upstream portable encoder sources. The original sources, custom build configuration, provenance checksum and COPYING are distributed; the decoder and assembler paths are not built. Windows retains Media Foundation. |
| ALSA 1.2.14 public headers | LGPL-2.1-or-later. Original header notices, COPYING and provenance remain under `native/audio-host/third_party/alsa`; Linux loads the system `libasound.so.2` dynamically for MIDI. |
| PulseAudio 17.0 public headers | LGPL-2.1-or-later; preserve upstream header notices and LICENSE/LGPL/GPL files. The version template is instantiated locally as documented in provenance. Linux dynamically loads the system `libpulse.so.0`; a PulseAudio or PipeWire-Pulse user service is required for desktop capture/device features. |
| LLVM-MinGW toolchain | LLVM Apache-2.0 with LLVM exceptions plus separately licensed toolchain components; retain all supplied notices, not only the top-level LICENSE.TXT. |
| GNU Make 4.4.1 and MinGW gendef (older full compiler bundles) | GPL-3.0-or-later; omitted from the current reduced x64 profile. Their matching sources/recipes/notices remain in the provenance materials. |
| Python, libffi and LLDB-MI (older full compiler bundles) | The Python/LLDB-MI programs and separate libffi runtime are omitted from the current reduced x64 profile; their pinned sources and notices remain in the provenance materials. |
| Microsoft VC/UCRT DLLs included in older Temurin-bundled packages | Microsoft redistribution terms apply; these files are not relicensed as OpenJDK code. MSVC provides the three VC runtime DLLs; the Windows SDK provides UCRT/API-set DLLs. See the manual Microsoft runtime review before publication. |

## Where notices are installed

Windows packages place LICENSE and this file in the application's app directory.
The licenses directory contains extracted runtime-JAR notices, native component
notices. Its upstream subdirectory supplements missing JAR
notices with the JogAmp and ANTLR release license documents. The selected compiler profile retains the applicable upstream notices. Application JARs also contain the project
license and this overview under META-INF.

## Source availability before public distribution

For each published binary, assemble a corresponding-source download from its exact
source revision, including local modifications, build scripts and required materials.
Provide exact required source/build materials for bundled copyleft components too,
including the compiler toolchain and any bundled Java runtime. Preserve upstream notices for permissively licensed components.

Project repository: https://github.com/Helge97/EasyKnack

These reference links are not an already fulfilled source offer or a complete
source archive matching any particular installer. The release checklist in
`docs/development/LICENSING.md` records what remains to prepare.
New installer builds include project and upstream source-material archives with
checksums and a REVIEW.json report. Remaining provenance checks are documented in
`docs/development/SOURCE-PACKAGES.md`; review them before public distribution.
The detailed native findings are in `docs/development/source-audit/NATIVE-RUNTIMES.md`.

The macOS package path includes matching OpenJDK/Temurin and Core build materials
and native notices. Apple frameworks and the SDK remain system/developer-tool
prerequisites, not redistributed files. Developer ID/ad-hoc re-signing changes
runtime signature metadata; the upstream source and legal texts are retained.
Review each actual Mac artifact before publication; the Linux-specific audit does
not certify a different platform package. See `docs/development/MACOS.md`.
The first Mac test distribution's package/source checks are recorded in
[the Mac distribution report](docs/development/source-audit/MACOS-EXPERIMENTAL-1-2026-11-22.md).
Its per-architecture license/source supplements accompany the DMG downloads.

For download distribution, give binary recipients equivalent access to the matching
project and dependency sources. An inaccessible private repository link does not
provide that access. Recipients need only the installer to run EasyKnack, but
source availability remains a separate distribution requirement. A shared folder
containing the installer and both matching source archives is one practical route.

## Plugins and generated code

Exported plugins can contain or depend on EasyKnack SDK/native code. Do not assume
that generated output or native plugins are license-independent merely because
part of their code was generated. Check the included code and dependencies before
distribution. No blanket permission for proprietary plugins is granted here.

The Microsoft component origin, exact SDK license and conditional redistribution
routes are recorded in `docs/development/source-audit/MICROSOFT-RUNTIME.md`.
Those component terms do not replace the licenses of EasyKnack or OpenJDK.

## Installer and artwork

The EXE installer embeds WiX 3.14.1 Burn/WixStdBA under the Microsoft Reciprocal
License (MS-RL). Its original notice is under licenses/upstream and its complete
release-tag sources are included in the matching source-material download. This
installer code retains its own license, separate from the EasyKnack application.
The adapted RTF setup theme and localization in `scripts/windows/InstallerTheme*`
also retain MS-RL; their notice is `scripts/windows/InstallerTheme-LICENSE.txt`.
EKSplash and EKIcon are original author-provided assets distributed under
GPL-3.0-only with the author's permission.
