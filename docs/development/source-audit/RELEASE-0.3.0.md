# EasyKnack 0.3.0 release - 2026-11-18

Release source: `1c837ca8d50ff7e90d5fc97a4bb775bd5a9ad977`, tag `v0.3.0`.
[Public release and downloads](https://github.com/Helge714/EasyKnack/releases/tag/v0.3.0).
The repository is public by explicit owner choice. This manually built Windows x64
release is unsigned and requires an external x64 JDK 25 including javac.
Development continues at `0.3.1-SNAPSHOT`; GLSwing remains independently versioned
at its reviewed, SHA-256-pinned `0.2.0-SNAPSHOT`.

## Automated validation

- Application: 803 cases, 793 passed, 10 explicitly gated skips,
  zero unresolved failures/errors after focused reruns. The full native-audio run
  initially exposed an outdated rate-contract assertion and three missing test
  host/compiler settings. Corrected/configured cases passed subsequent JUnit runs.
  Additional OpenGL coverage exposed one outdated event-center color assertion;
  the corrected final GPU test passed. Original results remain preserved.
- The original full-suite checkout and release contain identical bytecode for all
  597 production classes. Only the two test expectations changed between candidates.
- Five native host/backend/transport executables passed. Core: 205 tests including
  OpenGL passed without skips; independent reconstruction matches 967 classes and
  resources except generated build identity. See [Core correspondence](GLSWING-2026-11-18.md).
- Documentation: 270 pages and 8,002 internal links validated.
- Exact package: offline help, document round trip, repeated image capture, MP3,
  null audio, Formula compilation/cache, group export, fresh Java/C++ plugins,
  host contracts and restart cache passed. The actual launcher opened a responsive
  Untitled workspace and closed normally.
- All four packaged themes rendered documentation search marks, open borderless
  popup menus and sliders. Packaged C++ tools compiled functions, final/const,
  chosen-base logarithms and real roots.
- Inventory: 5912 file hashes,
  1586 project-source entries and
  13 upstream archives verified.
  Source review reports zero missing sources/unreviewed JARs.

## Distribution and limits

Six public assets: installer, matching project sources, source materials, release
notes, validation report and SHA-256 list. Each GitHub upload digest was checked.
The previous 0.2.0 release remains available and unchanged.
Frozen output: `dist/release-0.3.0-1c837ca`; detailed local evidence:
`target/release-0.3.0-20261118`. Downloadable `validation.json` records exact checks,
initial issues, successful reruns and remaining manual acceptance.

Clean-system install/upgrade/uninstall, physical WASAPI/ASIO/MIDI and extended
manual project acceptance remain open. No new clean-machine or physical-device
acceptance is claimed. Official release CI and signing remain future work.

## Additional example content - 2026-11-18

A separate collection of sixteen learning projects and its checksum were added
without replacing the original six assets or installer. See the
[example content validation and provenance](EXAMPLES-2026-11-18.md).
