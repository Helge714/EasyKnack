# Source packages and dependency review

`Build-Installer.bat` now places two source archives beside each new installer:

- `EasyKnack-project-sources.zip`: project source, vendored source, documentation,
  examples and build scripts, including local modifications and new unignored files.
- `EasyKnack-source-materials.zip`: that snapshot, checksum-locked upstream archives,
  the reviewed dependency inventory and `REVIEW.json` (runtime identity only when bundled).

The first build downloads several hundred MiB of upstream source material. Later
builds reuse `target/source-materials` after checking every SHA-256. The lock file is
`scripts/sources.lock.json`. A missing local GLSwing sources artifact is reported;
it is not silently substituted by a newer snapshot. GLSwing remains an external
Maven dependency.

## What is reproducible

For identical input bytes and the same ZIP implementation, source entries have
fixed timestamps and a stable order. Running the source packager twice produces
the same archive bytes. Application binaries still contain a fresh local build
identity and are not claimed to be bit-for-bit reproducible.

The Windows packager captures project sources before compilation, records the
snapshot SHA-256 in `app/installation.properties`, and checks for source changes
after building. A changed working tree fails packaging; rebuild in a fresh output
directory. Existing archives are never overwritten. Excluded material includes
IDE settings, caches, generated binaries and private-key files. Review the archive
before sharing; an allowlisted source file can still contain private information.

## Inspect or collect materials separately

With PowerShell 7, from the repository root:

```powershell
./scripts/package-sources.ps1 -Image 'target/distribution/BUILD/EasyKnack' `
  -ProjectSources 'target/distribution/BUILD/EasyKnack-project-sources.zip' `
  -Destination 'target/source-review' -Download
./scripts/test-source-package.ps1
```

Omit `-Download` to collect only cached material and report missing archives. The
destination must be new. `-RequireComplete` fails while the review is incomplete;
it must not be used as an assertion that merely collecting archives satisfies
every distribution requirement.

`REVIEW.json` records missing archives, changed/unreviewed JAR hashes, whether the
runtime matches the reviewed build, and whether the source snapshot matches the
image's build marker. An older image without this marker is explicitly unpaired.
The inventory is evidence, not a proof that an upstream binary was rebuilt from
the collected source. Download hashes pin the reviewed bytes; they are not a
substitute for upstream signature verification.

## Current review boundary

The dependency inventory was updated on 2026-10-07 to remove FFmpeg, JavaCPP and
their exclusive transitive sources. Audio import now uses the vendored miniaudio
header in the project source snapshot. The earlier native audit remains historical
evidence for older images; it is not the dependency list of new packages.

New Windows images use an external JDK: the source packager omits OpenJDK and
Temurin build archives and records `runtimeBundled: false`. It excludes the
Temurin-specific open review item for those images only; other obligations remain.
The old lock entries are retained for inspecting older bundled images.

For those older images, Temurin 25.0.4.1+1 identifies OpenJDK commit
`520406d871955300957ef01e406ac2acd0f9b75c` and temurin-build commit
`e6ba7dec3d07654074559310376a3ae89da5f4ac`; both source archives are collected.
The earlier GLSwing source correspondence gap is resolved by the isolated rebuild
documented below; the full build-input archive accompanies the sources JAR. The collection now includes full LLVM,
MinGW, Python, libffi, LLDB-MI, GNU Make source archives. Official Temurin SBOM/build metadata and file-by-file native
archive comparisons are recorded in the [native review](source-audit/NATIVE-RUNTIMES.md).

Before public distribution, resolve the remaining items in `sources.lock.json`
and [LICENSING.md](LICENSING.md), including Microsoft runtime redistribution terms only if bundling Java again. No release
or public upload is performed by these scripts.

## Candidate source match (2026-10-07)

The former GLSwing source mismatch review is resolved: an isolated rebuild of
907 captured source/build files produced all 927 runtime classes and resources
identically. Only generated build identity and Eclipse-specific Maven metadata
differ. The full build-input ZIP and parent POM are now included alongside the
sources JAR. See source-audit/RELEASE-CANDIDATE.md and glswing-comparison.json.

WiX 3.14.1 sources are included because the EXE embeds its Burn bootstrapper.
REVIEW.json format 2 distinguishes `sourceReviewComplete` from `releaseReady`;
clean-machine validation and actual source publication remain distribution tasks.

To rebuild from a published source-material ZIP, extract `easyknack-sources.zip`
(the project snapshot), copy the files under `upstream/` into the project's
`target/source-materials`, and unpack the pinned GLSwing build-input ZIP in a
separate directory. Install GLSwing using `mvn -pl glswing-core -am install` there
before building EasyKnack. Keep GLSwing as a Maven dependency. Newly rebuilt JAR
metadata changes hashes; compare classes/resources before updating a source lock,
and do not label that newly built artifact as the original distributed binary.
