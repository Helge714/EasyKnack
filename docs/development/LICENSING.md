# Distribution licensing checklist

Project choice: **GPL-3.0-only**, with the unmodified GPLv3 text in the root LICENSE.
This applies to EasyKnack-authored material unless separately marked. It does not
relicense third-party code. GLSwing remains Apache-2.0. The vendored ASIO SDK is
used under its GPLv3 alternative, not the proprietary agreement.

## Implemented

- Root license, README declaration and Maven license metadata.
- Project license and third-party overview included in application JARs.
- Windows package includes project license, overview, native notices, in addition to JAR, runtime and toolchain notices.
- Missing JogAmp and ANTLR notices are supplied from the pinned upstream
  source releases, including JogAmp's referenced license documents.
- Packaging captures the actual working-tree sources, detects edits during the
  build and adds a deterministic sources archive plus collected upstream materials.
- Exact JAR hashes and Temurin source revisions are
  recorded. See [source package instructions](SOURCE-PACKAGES.md).
- Native distribution review now includes official archive comparisons, Temurin
  SBOM/configuration, full compiler-component sources (including GPL gendef/Make),
  native-library notices with source-member checksums.
  See [verified findings and remaining boundaries](source-audit/NATIVE-RUNTIMES.md).

## Before publishing any binary

- [ ] Check rights and existing notices for all project and vendored files,
  including fonts/resources, reference driver code and generated SDK/export material.
- [ ] Audit the exact resolved Maven artifacts and native libraries actually shipped.
  Record component versions, hashes, license choices and source provenance.
- [x] Collect the identified OpenJDK build sources/configuration and LLVM-MinGW
  component sources/notices, including GNU Make and gendef.
- [x] Trace Temurin Microsoft DLLs to MSVC/SDK Redist origins; inspect the exact
  SDK installer license, VS product terms and matching REDIST entries.
- [ ] Establish downstream Microsoft authorization and how recipient agreement
  requirements are satisfied for installer and portable packages. See the
  [manual review](source-audit/MICROSOFT-RUNTIME.md); the narrowed
  [upstream question](source-audit/UPSTREAM-QUESTIONS.md) remains unsent.
- FFmpeg/JavaCPP were removed on 2026-10-07. Their historical build-provenance
  question no longer applies to new distributions; older images remain unchanged.
- [ ] Build from an identified release revision and archive corresponding EasyKnack
  sources and build scripts. Include uncommitted changes if distributing a local build;
  an archive of HEAD alone does not describe a dirty binary.
- [ ] Publish required source downloads with the binary distribution and preserve
  their availability as required by the applicable licenses. Upstream homepage links
  alone are not the release source package.
- [ ] Check plugin SDK/export licensing and generated notices; there is no special
  linking exception. Review combinations before promising proprietary-plugin support.
- [ ] Verify notices and source links in the final installed package and download page.

The local packaging script downloads checksum-locked source materials, but matching
all upstream build inputs remains under review. `REVIEW.json` records these gaps;
the package is not a public-release compliance certification.
No release tags, public uploads or source-offer commitments are created by this change.

References: [third-party overview](../../THIRD-PARTY.md),
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).
