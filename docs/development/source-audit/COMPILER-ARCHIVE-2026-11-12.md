# Compiler archive validation — 2026-11-12

The same reduced x64 compiler (5,586 files, 353,553,895 bytes) is represented by
one MSI file/component. The isolated no-op fixture has five File rows in total,
eight components and two build-time ReserveCost rows. The ZIP is 85,893,185 bytes;
the MSI is 85,204,992 bytes. Portable images still contain extracted files.

| Operation | Earlier reduced per-file MSI | Archive MSI | Archive CostFinalize |
| --- | ---: | ---: | ---: |
| Install | 23.950 s | 15.344 s | 0.004 s |
| Repair | 92.145 s | 19.749 s | 0.044 s |
| Uninstall | 47.453 s | 3.545 s | 0.007 s |

These are separate local single runs, not averages or complete application/Java
setup timings. The earlier benchmark used MSIFASTINSTALL=7; the archive uses 5
to keep its extra disk costing enabled. The earlier repair CostFinalize alone
took 66.857 s. The initial archive prototype unnecessarily checked every ancestor
directory for every file and took 117.469 s to install; the final helper checks
existing directories once per operation and hashes files while extracting.

The optimized MSI reused the identical archive and generated fixture XML with
the final helper binary. After deliberately corrupting the extracted Clang
driver, MSI repair restored it and compiled/ran a static C++20 program using
threads and filesystem support. Removal left no installation directory.

The small fixture additionally passed legacy migration, prepared-update failure
before old-product removal, successful upgrade, failed fresh installation,
publication/removal rollback, archive checksum rejection and preservation of
extra user files. The failure injection intentionally precedes old-product
removal: earlier sequencing exposed Windows per-user MSI registration rollback
errors even though file rollback succeeded.

Reproduce with `scripts/test-compiler-installer.ps1`; add
`-CompilerSource .local/tools/llvm-mingw-20250305-ucrt-x86_64` for the full reduced
compiler. The historical file-layout comparison remains available through
`scripts/measure-installer-toolchain.ps1`. Timings, source hashes and validation
limits are in [the machine-readable report](compiler-archive-2026-11-12.json).
Compressed local evidence is retained in
`dist/compiler-archive-20261112-3bfdf8e5/validation.zip`.
