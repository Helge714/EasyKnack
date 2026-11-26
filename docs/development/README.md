# Developer documentation

- [Code map](CODE-MAP.md): owning implementation and regression tests.
- [Build identity](BUILD-IDENTITY.md) and [Windows packaging](WINDOWS-PACKAGE.md).
- [Build storage](BUILD-STORAGE.md): disposable output, local tools and examples.
- [Core ownership](CORE-BOUNDARY.md): reusable mechanism versus audio policy.
- [Cleanup worklist](CLEANUP-PLAN.md): completed changes and open follow-ups.
- [Node documentation](NODE-DOCUMENTATION.md): bilingual reference maintenance.

`features/` retains technical feature references; current public behavior is
specified in the bilingual node and user references. `history/` preserves dated
plans and investigations and is not an implementation contract.

## Build languages

Install JDK 25, Maven and Python 3, then run
`python scripts/bootstrap-glswing.py` before `mvn package`. The bootstrap installs
the checksum-pinned public GLSwing 0.3.0 SDK as an external Maven dependency.
Use `--maven` and `--repository` to select Maven and its local repository.

PowerShell orchestrates Windows/native builds, packaging and JDK selection. C#
implements the small Windows launcher and packaging file helper. Python renders
and validates documentation and runs source/reference audits. These are active
build tools, separate from the Java application and C++ audio engine. Assembly
files under `native/audio-host/benchmarks/archive` retain compiler-output evidence;
they are not additional application implementations or build inputs.
