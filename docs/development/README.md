# Developer documentation

User-facing instructions are in the [English user guide](../user/README.md).
This section and the technical notes below are for contributors, not installation
requirements for packaged users.

## Build from source

Use a JDK compatible with the current dependencies, Maven 3.9 and a graphics-capable
desktop. The packaging scripts use JDK 25 with jpackage. Check `pom.xml` for the
required external GLSwing artifact/version and make it available through Maven.
EasyKnack must not depend on a checkout or source folder of GLSwingNext.

```sh
mvn verify
```

For the Windows audio host, use the native profile with a C++17 LLVM-MinGW toolchain:

```sh
mvn -Pnative-audio verify
```

The compiler can be specified with `-Daudio.cxx=C:/tools/llvm-mingw/bin/clang++.exe`.
For packaging, use [Windows packaging](../WINDOWS-PACKAGE.md), including
`Build-Installer.bat`. Package builds include required runtime components; running
source alone is a different setup. Eclipse launch configuration and Maven project
refresh are development conveniences, not end-user steps.

## Contributor references

| Topic | Reference |
|---|---|
| Working agreements and version policy | [AGENTS](../../AGENTS.md) |
| Build metadata | [Build identity](../BUILD-IDENTITY.md) |
| Application/Core responsibilities | [Core boundary](../CORE-BOUNDARY.md) |
| Native host | [Audio host](../../native/audio-host/README.md) |
| DSP planning and benchmarks | [DSP roadmap](../DSP-ROADMAP.md), [baseline](../DSP-BASELINE.md) |
| Formula language implementation | [Formula notes](../FORMULA-GROUP.md) |
| Processing-load semantics | [Telemetry audit](../RENDER-TELEMETRY.md) |
| Product changes | [Release notes](../RELEASE-NOTES.md) |
| Pending work | [TODO](../../TODO.md) |

The older topical files directly under `docs/` include design history, test accounts
and superseded behaviour. They are technical references, not the current user manual.
In particular, do not copy old menu names, split FFT descriptions or numeric Event-rate
instructions into the user guide without checking the implementation.

The former mixed-language README is preserved as a
[historical record](README-before-user-guide.md). It is intentionally not translated
or presented as current instructions. New user-facing documentation belongs in
`docs/user/`; implementation history and test details belong here or in technical notes.

## Release acceptance still required

Confirm install/uninstall/update and retained user data on a clean Windows machine.
Add the product icon when available. Review project and bundled dependency licensing,
source-distribution obligations and notices before a public release. This documentation
cleanup does not complete that review or authorize publication.
