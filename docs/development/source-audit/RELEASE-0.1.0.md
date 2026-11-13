# EasyKnack 0.1.0 release - 2026-11-13

Release source: `fcf85a5f61ccbe94045cf8393d4cf79c28228ca0`, tag `v0.1.0`.
Windows x64, external x64 JDK 25 including javac, unsigned installer. This is a
manual release with an explicitly local build identity; official CI remains pending.
Development continues independently at `0.1.1-SNAPSHOT`; GLSwing stays at its
reviewed, hash-pinned `0.2.0-SNAPSHOT` dependency.

## Distribution

[GitHub release and downloads](https://github.com/Helge97/EasyKnack/releases/tag/v0.1.0).
The repository retains its existing private visibility by explicit user choice;
release downloads require repository access.
Installer, exact project sources, matching dependency source materials, curated
release notes, `validation.json` and `SHA256SUMS.txt` are distributed together.
Local artifacts: `dist/release-0.1.0-fcf85a5`; diagnostic logs remain in its
`build-logs/validation-evidence.zip`. Final publication is recorded in
`github-publication.json` beside the local artifacts.

## Automated acceptance

| Check | Result |
| --- | --- |
| Final Java package suite | 741 cases: 565 passed, 176 intentionally gated skips, zero failures/errors. |
| Documentation | 268 pages, 7,882 local links, section IDs and language coverage passed. |
| Native host | Matching release host built successfully; null-device playback and parameter contracts passed in the packaged application. |
| Exact package smoke | Offline help, repeated text image capture, document round trip, MP3 decode, Formula compilation/cache, group export, fresh Java/C++ plugin compilation and restart cache passed. |
| Real launcher | Responsive Untitled workspace opened and closed normally, without an initial dirty-document prompt. |
| Windows publication lock | Real child-process directory-lock regression passed; ten real host validation/publication cycles passed. |
| Installer | Earlier isolated production-code fixtures passed migration, repair, prepared-failure rollback and uninstall; EXE/MSI Java rejection/acceptance and Java-independent removal passed. |
| Final file/source correspondence | 5,903 inventory entries, 1,552 source files and 13 upstream archives passed size/hash checks; zero unexpected image files or unreviewed libraries. |

The first frozen candidate `d1d4503` was rejected when a real packaged plugin
build hit a Windows directory lock during cache publication. The final source
adds bounded retries for transient access/sharing failures, preserves existing
destinations and retains the original error if the lock persists. The complete
final package test passed after rebuilding from the clean corrected checkout.
Earlier candidate bytes and their failure evidence were not relabeled.

## Remaining manual acceptance

These checks are disclosed in the downloadable validation report and remain open:
clean Windows/test-user interactive Java setup, cancellation/offline handling,
shortcuts/default association/installed notices, real WASAPI/ASIO/MIDI devices,
extended playback and recording/routing, older-document copies, manual group/
Copy Editor/Undo workflows and a first-user walkthrough. The automated checks do
not certify these cases. Experimental Pitch Hold accuracy remains a known limit.

The source bundle's embedded REVIEW.json records build-time distribution checks;
its outstanding manual acceptance is not silently marked passed by publication.
