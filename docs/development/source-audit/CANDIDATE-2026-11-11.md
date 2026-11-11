# Local acceptance candidate — 2026-11-11

Candidate source: `866d426df8e23953ca51c59b0f267a8831c83155`; external Core: `fc1261bbb03fe5d6c90cfd912439822c08b8cce5`.
EasyKnack remains **0.0.1-SNAPSHOT**, Core **0.2.0-SNAPSHOT**. This is an unsigned
Windows x64 acceptance candidate with external JDK 25, not an approved public release.
Build identity is explicitly local and the candidate source checkout is clean.

## Artifacts

Use `dist/rc-20261111-866d426`. The installer is `EasyKnack-0.0.1.exe`.
Matching project sources, upstream source materials, `SHA256SUMS.txt`,
`validation.json` and build logs sit beside it. Earlier artifacts were retained:
`dc45cbf` is rejected for missing text on repeated image export; `b40ea59` is
superseded by the single offline-link correction in the final candidate.

## Automated results and limits

| Check | Result |
| --- | --- |
| Full application baseline, audio/GL/references enabled | 733 cases: 723 passed, 1 failure, 1 error, 8 deliberate skips. Original run retained unchanged. |
| Native backend/graph/transport/bridge programs | All five passed. Device mocks/callback checks do not replace real-device listening. |
| Fixed fixtures with reviewed final Core | 21 affected export/rate/rendering/tooltip/history cases passed, no failures/errors/skips. |
| Complete Core suite, OpenGL enabled | 172 passed, no failures/errors/skips. |
| Final package's default Maven suite | 728 cases, no failures/errors; 175 intentionally gated audio/GL/etc. cases skipped. |
| Packaged functional test | Offline help, identical repeated image captures, document round trip, playing null-audio host, MP3 decode, Formula cache and source-plugin build/cache passed. |
| Real EXE launcher | Visible responsive workspace opened and closed normally without a false initial save prompt. |
| JDK prerequisite/argument tests | Passed with installed Temurin JDK 25; no prerequisite was installed. |

The initial full run was **not green** and has not been relabeled. The native
compiler shared a 120-second limit with executable runtime. Compiler preparation
now gets 600 seconds; execution remains limited to 120, numerical assertions remain
unchanged, and timed-out process trees are terminated. Its export executable also
passed independently before the helper was corrected.

The original WGL temporary-context activation error did not reproduce verbatim.
Grouped rendering retries exposed stale GPU textures from shared CPU measurement
fonts. Offscreen fixtures now own surface-local fonts and retain the original
pixel, picking and GL-error assertions. The final grouped checks and launcher pass;
this does not substitute for clean-system GPU acceptance.

A separate packaged probe then found a real repeated-image-export defect: first
capture had 663 text pixels, second had zero. Core now isolates capture fonts,
preserves source parent/geometry/font ownership and restores its resolution scope.
The repaired probe has 663 pixels in both images and zero pixel differences.
The packaged smoke test now includes this regression. See
[Core correspondence](GLSWING-CAPTURE-2026-11-11.md).

No second multi-hour full native-enabled suite was run after these focused fixes.
Application Java, `src/main/resources` and native sources are identical to the full baseline;
Core's capture rendering changed and was checked by its complete suite, affected
consumer tests and actual packaged exports. Java/native contracts still hash to
`8022d4fbb88d256c6e90cef6d5f6919c1e3c9a2539b79a37fe8a0c5151cb6df9`. No document format, plugin ABI or audio protocol changed.

The eight original skips are four physical audio/MIDI checks, experimental Pitch
Hold/Harmonizer accuracy, two private-document tests and the optional benchmark.
Pitch Hold accuracy remains an explicit experimental limitation.

## Artifact and source correspondence

- 9,332 staged `app/` files match their recorded size/hash, with no unexpected files.
- 1,526 project source files match the paired archive; IDE settings and uncommitted user edits are excluded.
- All 13 required upstream archives pass their pinned SHA-256 checks; no missing sources or unreviewed JARs.
- Core reconstruction matches 962 classes and all nonmetadata runtime resources; 532 source-JAR entries match captured source bytes. Only generated build/Maven metadata differ.
- All 268 external help pages and 7,881 local file links resolve. Source preview also validates 7,882 links/anchors.

`REVIEW.json` reports source review complete but `releaseReady=false`, because
distribution acceptance/publication is still pending. Exact hashes and original
test totals are in [the machine-readable report](candidate-2026-11-11.json).

## Still required before publication

1. Clean Windows installation with missing Java, selection/setup/cancel/offline handling.
2. Upgrade/uninstall, shortcuts, file association and installed notices on that clean system.
3. Real WASAPI/ASIO/MIDI devices, recording/routing, longer playback and representative old document copies.
4. Manual group/Copy Editor/Undo acceptance and a first-user walkthrough with installed help.
5. Select the public release identity and unsigned/signing policy; publish matching sources/checksums with the binary only after explicit authorization.

No release tag, push, upload or public dependency publication was performed.
