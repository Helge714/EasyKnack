# Windows x64 compiler profile — 2026-11-12

The installed runtime compiler now retains native x64 C/C++, complete shared
headers and Clang resources, x64 libraries, actual compiler runtime dependencies,
target/default configuration and vendor notices. Other target architectures,
Python/LLDB and unrelated tools are excluded. The original toolchain is unchanged.
This compatible packaging change is covered by EasyKnack `0.1.0-SNAPSHOT`.

## Size and file count

| Toolchain | Files | Installed MiB | Test MSI MiB |
| --- | ---: | ---: | ---: |
| Full | 9015 | 588.50 | 139.08 |
| Reduced | 5586 | 337.18 | 77.95 |

The profile keeps all shared C/C++ headers. Removing the two Clang configuration
files in the first experiment broke C++ include discovery; the final profile and
retention regression explicitly include both configurations.

## Runtime checks

- `FormulaNativeCompilerTest`: real Formula DLL, cache and native/preview values pass.
- `PluginIntegrationTest#sourceExportBuildsOnceLoadsFromCacheAndRetainsGoodBuildOnFailure`:
  actual source-plugin build, host, cache and fallback checks pass.
- The native Bake compiled/linked and validated all contracts, but its initial
  `GroupBakeTest` failed publishing the directory after host shutdown. Fix
  `37951e8` waits after forced termination; two focused shutdown tests pass.
  Reusing the exact compiled Bake, an immediate directory move, plugin load and
  real PLAY/STOP pass. The complete forced-rebuild Bake test was not repeated.
- A relocated profile in a path containing spaces compiles/runs C++ standard
  library, threads, filesystem, complex numbers and Windows API checks.
- `test-compiler-profile.ps1`: required configuration/headers/libraries retained,
  excluded files omitted, source hashes preserved, unsafe destinations refused.

## Installation comparison

One local run, full then reduced, on the same Windows machine. Both isolated test
products contain the same no-op application and use `MSIFASTINSTALL=7`. The user's
EasyKnack installation and `.knack` association were not changed. A repair runs
against the previously installed files. No C++ build ran during the measurements.

| Operation | Full total | Reduced total | Full CostFinalize | Reduced CostFinalize |
| --- | ---: | ---: | ---: | ---: |
| Install | 40.086 s | 23.950 s | 0.391 s | 0.228 s |
| Repair | 127.922 s | 92.145 s | 85.140 s | 66.857 s |
| Uninstall | 74.704 s | 47.453 s | 4.989 s | 3.031 s |

Repair costing falls from 85.140 to 66.857 seconds: about 21%, while total repair
time falls by about 28%. Fresh installation is faster, but the long existing-file
check remains. A fixed build-time size does not eliminate this phase. Treat these
as exploratory single-run observations, not averages or universal speed guarantees;
cache state, scan activity, test order and product history can affect results.

Both products were uninstalled successfully. Full logs, tests and hashes are under
`dist/compiler-profile-20261112-6c038705`; the two large temporary package/compiler
trees were removed after preserving that evidence (1.98 GiB freed). The structured measurements are
[recorded separately](compiler-profile-2026-11-12.json).

Reproduce selection with `scripts/test-compiler-profile.ps1`, stage a profile with
`PackageFiles.CopyCompiler`, compare via `scripts/measure-installer-toolchain.ps1`,
and extract action timings with `scripts/summarize-installer-measurement.py`.
