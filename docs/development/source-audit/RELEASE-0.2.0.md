# EasyKnack 0.2.0 release - 2026-11-14

Release source: `6132264cf1c61f72e79f3621c828d8104dd91a75`, tag `v0.2.0`.
[Main release and downloads](https://github.com/Helge97/EasyKnack/releases/tag/v0.2.0). Repository visibility remains
private; downloads require repository access. This manually built Windows x64
release is unsigned and requires an external x64 JDK 25 including javac.
Development continues at `0.2.1-SNAPSHOT`; external GLSwing remains independently
versioned at its reviewed, SHA-256-pinned `0.2.0-SNAPSHOT`.

## Final automated acceptance

- Application: 745 cases, 569 passed, 176 intentionally gated skips, zero failures/errors.
- Core: 184 tests including OpenGL, all passed without skips. Independent source
  reconstruction matches all 963 classes and resources except generated identity;
  see [correspondence](GLSWING-2026-11-14.md).
- Documentation: 268 pages and 7,882 local links passed validation.
- Exact package: offline help, repeated image capture, document round trip, MP3,
  null audio, Formula compilation/cache, group export, fresh Java/C++ plugins,
  host contracts and restart cache passed. The actual launcher opened a clean,
  responsive Untitled workspace and closed normally.
- All four packaged themes rendered open borderless popup menus and button sliders
  through OpenGL without exceptions. Images remain local validation evidence.
- Inventory: 5,903 file hashes, 1,566 project-source entries and 13 upstream
  archives verified. Source review reports zero missing archives/unreviewed JARs.

The installer implementation and native audio sources are unchanged from 0.1.0.
Clean-system installation/upgrade, physical WASAPI/ASIO/MIDI and extended manual
acceptance remain open, explicitly recorded in downloadable `validation.json`.
No new clean-machine or physical-device acceptance is claimed.

## Distribution and provenance

Six published assets: installer, project sources, source materials, release notes,
validation report and SHA-256 list. GitHub upload checks each asset's digest.
Frozen output: `dist/release-0.2.0-6132264`. Logs: `target/release-0.2.0-20261114`
and the output's `build-logs` directory.

Candidate `fbc2133` was rejected by three assertions describing the old palette
and theme selection. The expectations were updated, then the complete suite and
package were rebuilt from clean source `6132264`. Failed artifacts were retained,
not relabeled or overwritten.

After 0.2.0 publication, the old 0.1.0 GitHub release and its six assets were removed
at the user's explicit request. Its source tag and immutable local backup remain.
Publication metadata is stored beside the artifacts in `github-publication.json`;
old release metadata is retained in `target/release-0.2.0-20261114`.
