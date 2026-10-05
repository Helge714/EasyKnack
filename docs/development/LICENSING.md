# Distribution licensing checklist

Project choice: **GPL-3.0-only**, with the unmodified GPLv3 text in the root LICENSE.
This applies to EasyKnack-authored material unless separately marked. It does not
relicense third-party code. GLSwing remains Apache-2.0. The vendored ASIO SDK is
used under its GPLv3 alternative, not the proprietary agreement.

## Implemented

- Root license, README declaration and Maven license metadata.
- Project license and third-party overview included in application JARs.
- Windows package includes project license, overview, native notices and FFmpeg
  license information, in addition to JAR, runtime and toolchain notices.

## Before publishing any binary

- [ ] Check rights and existing notices for all project and vendored files,
  including fonts/resources, reference driver code and generated SDK/export material.
- [ ] Audit the exact resolved Maven artifacts and native libraries actually shipped.
  Record component versions, hashes, license choices and source provenance.
- [ ] Collect matching source/build materials for the exact FFmpeg binary and its
  enabled libraries; verify -L and -buildconf for the artifact being distributed.
- [ ] Collect the corresponding sources/materials required for the exact bundled
  JDK and any other copyleft components. Review all LLVM-MinGW component notices.
- [ ] Build from an identified release revision and archive corresponding EasyKnack
  sources and build scripts. Include uncommitted changes if distributing a local build;
  an archive of HEAD alone does not describe a dirty binary.
- [ ] Publish required source downloads with the binary distribution and preserve
  their availability as required by the applicable licenses. Upstream homepage links
  alone are not the release source package.
- [ ] Check plugin SDK/export licensing and generated notices; there is no special
  linking exception. Review combinations before promising proprietary-plugin support.
- [ ] Verify notices and source links in the final installed package and download page.

The local packaging script collects available notices; it does not download or verify
complete corresponding sources and is not a public-release compliance certification.
No release tags, public uploads or source-offer commitments are created by this change.

References: [third-party overview](../../THIRD-PARTY.md),
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html),
[FFmpeg licensing](https://ffmpeg.org/legal.html).
