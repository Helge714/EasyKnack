# Source packages and dependency review

`Build-Installer.bat` now places two source archives beside each new installer:

- `EasyKnack-project-sources.zip`: project source, vendored source, documentation,
  examples and build scripts, including local modifications and new unignored files.
- `EasyKnack-source-materials.zip`: that snapshot, checksum-locked upstream archives,
  the reviewed dependency inventory, runtime identity and `REVIEW.json`.

The first build downloads approximately 800 MB of upstream source material. Later
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

The initial inventory is the locally packaged Windows x64 dependency set from
2026-10-05. FFmpeg reports version 7.1, LGPL-3.0-or-later, GCC 12.2.0 (MSYS2 Rev11),
and no `--enable-gpl` or `--enable-nonfree`. The exact output is in `source-audit`.
The enabled library versions are taken from the Bytedeco 1.5.11 build recipe.
Their archives and the complete preset recipe/patch tree are collected.

Temurin 25.0.4.1+1 identifies OpenJDK commit
`520406d871955300957ef01e406ac2acd0f9b75c` and temurin-build commit
`e6ba7dec3d07654074559310376a3ae89da5f4ac`; both source archives are collected.
The local GLSwing sources JAR is collected but its correspondence to the dirty
binary build has not been established. The collection now includes full LLVM,
MinGW, Python, libffi, LLDB-MI, GNU Make and GCC source archives, plus the MSYS2
GCC recipe/patch tree. Official Temurin SBOM/build metadata and file-by-file native
archive comparisons are recorded in the [native review](source-audit/NATIVE-RUNTIMES.md).

Before public distribution, resolve the remaining items in `sources.lock.json`
and [LICENSING.md](LICENSING.md), especially FFmpeg CRT/pthread provenance,
Microsoft runtime redistribution terms and the GLSwing snapshot. No release
or public upload is performed by these scripts.
