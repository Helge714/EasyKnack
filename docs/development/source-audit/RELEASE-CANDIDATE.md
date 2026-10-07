# Release candidate audit — 2026-10-07

Scope: Windows x64, external JDK 25, GPL-3.0-only EasyKnack, source plugins and
bundled LLVM-MinGW. This records evidence for a candidate, not a warranty or a
claim that an installer was tested on a clean machine.

## GLSwing source correspondence

The external Maven JAR remains unchanged; EasyKnack has no source/project
reference to GLSwing. A clean, isolated build of the captured GLSwing working
tree completed 117 tests (one skipped, no failures/errors). All 927 class files
and every other archive entry except the following match the shipped JAR byte
for byte:

- `META-INF/glswing/build.properties`: generated time/local build identity and
  Git metadata. The isolated copy is inside the EasyKnack checkout, so its Git
  identity is not the original GLSwing identity.
- `META-INF/maven/com.vingame/glswing/pom.properties`: Eclipse adds local project
  path/name and comments; Maven's fresh build retains only artifact coordinates.

The whole JAR is therefore **not** claimed to be byte-reproducible. Exact runtime
classes/resources are matched, not inferred from timestamps. See
`glswing-comparison.json`. The captured source baseline is GLSwing commit
`6605584c8786289a87751b0cff314e0ce90cf535` plus the recorded working-tree changes.
The build-input ZIP includes all 907 captured files, parent/module POMs, resources,
tests and build scripts, with per-file SHA-256 in `source-snapshot.json`.
The ZIP and the original sources JAR are pinned in `scripts/sources.lock.json`.

Rebuild from that ZIP with JDK 25 and Maven:
`mvn -f GLSwingNext/pom.xml -pl glswing-core -am package`.
Compare the resulting JAR with `scripts/compare-jar-content.py`; only the two
metadata entries listed above are permitted to differ. Build identity differences
must never be used to excuse changed classes or resources.

## Distribution inventory

- EasyKnack original Java/C++ and scripts: GPL-3.0-only. Existing manual changes
  are captured in the candidate's project-source archive; HEAD alone is not its
  source. The first release remains 0.0.1; no release tag is created here.
- Icon and splash: author confirmed original work and GPL-3.0 permission on
  2026-10-07. Original images are included under `src/main/resources/branding`.
- Test MP3 is an original synthesized sine fixture, not a third-party recording.
  No additional fonts/audio recordings are distributed in EasyKnack resources.
- GLSwing: Apache-2.0, exact LICENSE in the JAR and extracted package notices.
  Its matching resources include its SVGs; system fonts are not copied from Windows.
- JogAmp 2.6.0: exact pinned JARs; BSD and component notices retained, including
  Ubuntu font, Apache and SGI notices from the upstream license index. The shipped
  Windows natives are gluegen_rt, jogl_desktop, jogl_mobile, nativewindow_awt,
  nativewindow_win32 and newt_head. Optional Oculus artifacts are not packaged.
- ANTLR 2.7.7: original public-domain notice retained from the pinned source.
- ASIO headers: GPLv3 alternative explicitly selected; original SDK LICENSE.txt
  and COPYING retained. No proprietary ASIO redistribution permission is claimed.
- miniaudio: MIT-0 option selected; original full header and license retained.
- LLVM-MinGW 20250305: prior file-by-file official-archive comparison and pinned
  release recipes retained. Complete LLVM, MinGW, Make, Python, libffi and LLDB-MI
  sources and original notices included. This is source provenance, not an
  independent rebuild of every upstream toolchain binary.
- WiX 3.14.1: newly accounted for because Burn/WixStdBA are embedded in the EXE.
  MS-RL notice retained in the package and Help notice index. Full release-tag
  source archive (wix3141rtm) is pinned and included. Source LICENSE.TXT matches
  the installed tool notice after CRLF/LF normalization. Installer code retains
  MS-RL; the application has its separate GPL license. Build tools themselves
  are not installed with EasyKnack.
- The project source archive includes virtual-driver bridge/reference manifests,
  but no SysVAD driver binary or downloaded driver source tree is installed.
- No FFmpeg, JavaCPP, Temurin runtime or separate Microsoft VC/UCRT DLLs are
  bundled in this candidate. Windows system DLLs and .NET Framework are external
  prerequisites; the Microsoft OpenJDK download is initiated only with user consent.

The SDK and newly exported source plugins now carry LICENSE and LICENSING.md.
The notice describes copied SDK/template material without claiming ownership of
user-authored graphs and without promising a proprietary-plugin exception.

## Remaining release operations

The candidate source review is separate from distribution validation in
REVIEW.json (`sourceReviewComplete` versus `releaseReady`). The following are
not marked complete merely because the archive builds:

- Clean Windows installation without Java; actual provider download, cancellation,
  offline handling and possible UAC. Windows Sandbox is unavailable on this host.
- Installer upgrade/uninstall, shortcuts and file association in a clean test user.
- Opening and closing: existing startup-document dirty state can show a save prompt;
  do not mistake that prompt for a launcher process failure.
- Publish the **matching** source-material archive beside the binary download.
  No files have been uploaded, no release tag has been created.

New artifact versions/hashes require a fresh review; this assessment applies only
to the pinned inventory. Bundled Temurin remains a future option with its own review.

## License references

- GNU GPLv3: https://www.gnu.org/licenses/gpl-3.0.html (local full text: LICENSE).
- Apache-2.0: https://www.apache.org/licenses/LICENSE-2.0
- WiX 3 license: https://docs.firegiant.com/wix3/main/license/
- WiX 3.14.1 source: https://github.com/wixtoolset/wix3/tree/wix3141rtm
- Native component details and original notices: NATIVE-RUNTIMES.md and the
  checksum-locked archives; the old FFmpeg/Temurin sections are historical only.
