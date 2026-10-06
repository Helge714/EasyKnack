# Licensing and third-party components

EasyKnack-authored code and documentation are licensed under GNU GPL version 3
only (SPDX: GPL-3.0-only), unless a file states otherwise. See LICENSE.
There is no additional plugin/linking exception. Existing third-party notices
and licenses are retained and are not replaced by EasyKnack's license.

GLSwing is a separately distributed Apache-2.0 library. Its license is unchanged.
This inventory identifies components for distribution review; it is not a claim
that all corresponding-source obligations have already been fulfilled.

| Component | License / distribution notes |
|---|---|
| EasyKnack Java application, native host and original SDK code | GPL-3.0-only; provide corresponding source and build scripts for the distributed version. |
| GLSwing | Apache-2.0; retain its license and applicable notices from the external artifact. |
| Steinberg ASIO SDK | The vendored SDK offers GPLv3 or a separate proprietary agreement. EasyKnack uses the GPLv3 option. Preserve the SDK notices and COPYING. |
| miniaudio 0.11.21 | MIT-0 option, copyright David Reid; license text is in the vendored header and packaged notices. |
| FFmpeg 7.1 / Bytedeco 1.5.11 | The inspected standard Windows executable reports LGPL-3.0-or-later. Exact build dependencies and source materials still require release review. |
| JavaCPP and presets | Separate from FFmpeg; retain the licenses/notices in the exact Maven artifacts. |
| JogAmp JOGL / GlueGen and transitive JARs | Preserve the notices/licenses from each exact artifact; review bundled native components too. |
| Bundled JDK | Retain the runtime's legal directory. OpenJDK GPLv2 with Classpath Exception and additional component notices require review against the exact JDK distribution. |
| LLVM-MinGW toolchain | LLVM Apache-2.0 with LLVM exceptions plus separately licensed toolchain components; retain all supplied notices, not only the top-level LICENSE.TXT. |
| GNU Make 4.4.1 and MinGW gendef (inside the compiler bundle) | GPL-3.0-or-later; matching source trees, build recipes and license notices are collected. |
| Compiler's Python, libffi and LLDB-MI | Separate licenses; preserve their notices. Their pinned source archives are collected too. |
| Microsoft VC/UCRT DLLs included in Temurin | Microsoft redistribution terms apply; these files are not relicensed as OpenJDK code. See the native distribution review before publication. |

## Where notices are installed

Windows packages place LICENSE and this file in the application's app directory.
The licenses directory contains extracted runtime-JAR notices, native component
notices and FFmpeg information. Its upstream subdirectory supplements missing JAR
notices with the JavaCPP, JogAmp and ANTLR release license documents. The JDK legal directory and the complete compiler
distribution retain their own notices. Application JARs also contain the project
license and this overview under META-INF.

## Source availability before public distribution

For each published binary, assemble a corresponding-source download from its exact
source revision, including local modifications, build scripts and required materials.
Provide exact required source/build materials for bundled copyleft components too,
including the actual FFmpeg configuration, patches and enabled dependencies, and the
bundled JDK. Preserve upstream notices for permissively licensed components.

Project repository: https://github.com/Helge97/EasyKnack
FFmpeg legal information: https://ffmpeg.org/legal.html
Bytedeco build recipes: https://github.com/bytedeco/javacpp-presets/tree/1.5.11/ffmpeg

These reference links are not an already fulfilled source offer or a complete
source archive matching any particular installer. The release checklist in
`docs/development/LICENSING.md` records what remains to prepare.
New installer builds include project and upstream source-material archives with
checksums and a REVIEW.json report. Remaining provenance checks are documented in
`docs/development/SOURCE-PACKAGES.md`; review them before public distribution.
The detailed native findings are in `docs/development/source-audit/NATIVE-RUNTIMES.md`.

## Plugins and generated code

Exported plugins can contain or depend on EasyKnack SDK/native code. Do not assume
that generated output or native plugins are license-independent merely because
part of their code was generated. Check the included code and dependencies before
distribution. No blanket permission for proprietary plugins is granted here.
