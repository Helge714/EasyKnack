# Distribution licensing checklist

EasyKnack uses **GPL-3.0-only**, GLSwing remains **Apache-2.0**, and third-party
components retain their own licenses. See ../../THIRD-PARTY.md and the full texts.

## Current external-JDK candidate

- [x] Project license and third-party notices included in JAR and Windows package.
- [x] Icon/splash author permission recorded (original work, GPL-3.0).
- [x] Current Maven JAR hashes reviewed; JogAmp/ANTLR notices and sources collected.
- [x] GLSwing runtime classes/resources matched to an isolated source rebuild;
  parent POM, tests and build scripts captured with per-file hashes.
- [x] ASIO GPLv3 choice and miniaudio MIT-0 choice recorded, original notices retained.
- [x] LLVM-MinGW component sources/build recipes and notices collected, including
  GNU Make and gendef. Upstream binary provenance is recorded, not independently rebuilt.
- [x] WiX/Burn MS-RL license and full release-tag sources included.
- [x] SDK and new source-plugin exports carry their license and scope notice.
- [x] Packaging captures the working tree, checks for edits during the build and
  pairs the binary image with its exact source archive by hash.
- [ ] Validate a clean Windows install with missing Java, upgrade and uninstall,
  and inspect file associations, shortcuts and the installed notices.
- [x] Publish matching project and dependency source downloads with release 0.1.0
  on 2026-11-13; see [release evidence](source-audit/RELEASE-0.1.0.md).

See [candidate evidence](source-audit/RELEASE-CANDIDATE.md). REVIEW.json separates
source review from still-pending distribution tests; it is not a legal warranty.
The final candidate's external validation report records actual test results.

## Release-specific legal preflight

The [2026-11-13 preflight](source-audit/LEGAL-PREFLIGHT-2026-11-13.md) checks the
actual 0.1.0 artifact hashes, notices and source availability and separates those
checks from unresolved GUI originality, trademark/design and installer-aggregation
assessments. It is not legal clearance. Give every binary recipient access to the
matching source downloads; private release permissions must not exclude recipients.

## Historical / future bundled runtimes

FFmpeg/JavaCPP are removed. Current images neither bundle Java nor its Microsoft
DLLs. The [Temurin/Microsoft review](source-audit/MICROSOFT-RUNTIME.md) therefore
is not a blocker for these external-JDK images; resolve its downstream terms if
Java is bundled again. The prepared upstream question remains unsent.

Users install Java under its provider's terms. Setup assistance does not shift
EasyKnack's own distribution obligations to its users. No linking exception for
proprietary plugins is granted; user graphs are not automatically claimed as
EasyKnack-owned merely because they are exported.
