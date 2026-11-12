# Release readiness audit — 2026-11-10

Audited application baseline: `ae41864`; paired local GLSwing change: `2f5ed7c`.
This is a repository and packaging-readiness review, not acceptance of a release
binary. No installer was rebuilt, installed or published during this audit.

Follow-up: Core source correspondence is now complete for the frozen November
candidate; see [the reviewed reconstruction](source-audit/GLSWING-2026-11-10.md).
The full automated regression run is now complete with five unresolved cases;
see [results and release blockers](source-audit/REGRESSION-2026-11-10.md).
Follow-up on 2026-11-11: the four nonexperimental failures are fixed and verified;
Pitch Hold is explicitly experimental and opt-in. See [fixes and remaining acceptance](source-audit/RELEASE-FIXES-2026-11-11.md).
The original findings below retain the hashes observed at the initial audit.

Latest candidate: [2026-11-11 automated results and remaining acceptance](source-audit/CANDIDATE-2026-11-11.md). A fresh installer and paired sources now exist; the older findings below are historical.

## Current state

- EasyKnack: `0.1.0-SNAPSHOT`; external GLSwing: `0.2.0-SNAPSHOT` in `pom.xml`.
  On 2026-11-12 the first intended EasyKnack release was explicitly set to `0.1.0`.
  This selects a development/release target, not a publication or tag. The older
  `0.0.1-SNAPSHOT` candidate evidence remains historical and unchanged; a fresh
  candidate must be built for the current sources and version. See
  [version policy](BUILD-IDENTITY.md#version-baseline).
- Package identity generation and About/diagnostic display already exist. Official
  CI/publication is still pending; there is no `.github` workflow directory.
  An official CI pipeline is required for CI-labelled builds, not for an honestly
  identified, manually built first release candidate.
- Windows x64 EXE/MSI packaging, per-user installation, file association, source
  archives and inventory hashes are implemented. Current distribution requires
  an external x64 JDK 25, including `javac`, and does not bundle Java.
- Existing `dist/20261108-133010-953` and `dist/20261108-234907-880` images identify
  themselves as `0.0.1-SNAPSHOT`. They predate the latest changes and cannot be
  accepted as the candidate for the audited current sources.
- Only pre-existing Eclipse `.classpath` and resource-preference edits were dirty
  at audit start. Keep these machine-specific changes out of release commits and
  source packages; do not discard them to manufacture a clean checkout.

## Gates before public release

| Gate | Evidence / gap | Completion criterion |
| --- | --- | --- |
| Freeze scope and identity | First target `0.1.0` selected on 2026-11-12; development is `0.1.0-SNAPSHOT`. Release notes remain Unreleased. | Freeze Windows x64 scope and candidate identity for `0.1.0`; commit final release notes and exact dependency selection. Preserve every earlier candidate artifact. |
| Refresh exact Core source evidence | `scripts/sources.lock.json` pins GLSwing runtime SHA-256 `82c0dbbc4561e155fa4c14b227c314a1c22de41a40493c9ac104201383c7766d`; current local Maven JAR is `98b732eff27ff161c48214bc59fe39e4985bd5ee71aeb098096ec46b1bc8e5e4`. October source review is historical evidence. | Capture matching current Core sources, parent/build inputs and runtime; rebuild/compare classes and resources, review changes, then update source/runtime locks. Do not merely replace hashes to pass packaging. Freeze exact candidate bytes even while development uses SNAPSHOT. |
| Full application/native regression | `package-windows.ps1` runs plain Maven package (`easyknack.test.audio=false`) and builds the host with `-SkipTests`. It does not establish native test coverage. Recent focused UI checks are not a full candidate test run. | Run the Java suite with audio tests enabled against matching host/contracts and compiler, native test executables, and Core tests with OpenGL enabled; record failures, skips, environment and logs. Device-gated tests remain separate. |
| Fresh package smoke | Existing installers predate this baseline. | Build a fresh EXE and paired source archives; run `test-windows-package.ps1`, including real launcher checks, against that exact image. Check Help/About, examples, offline help, Formula, plugins, Bake, media and null audio. |
| Clean Windows acceptance | Still open in TODO and source-audit checklist. | On a clean VM/test user: missing Java, selection/setup/cancellation/offline behavior, start, save/load/recovery, shortcuts, association, upgrade and uninstall. Record OS/JDK and artifact hashes. |
| Audio and graph acceptance | TODO explicitly leaves group acceptance and long-running device checks open. | Check real audio device start/stop/change, recording and playback; nested Undo/Redo, forwarded ports, copied FFT editors, inspector changes, duplicate Bakes and Step Switch modes. Open representative older `.knack` files, save copies and reopen; never overwrite the originals during acceptance. |
| Final distribution evidence | Source archive generation exists, but prior reviews do not cover new bytes. | Review final `REVIEW.json`, notices, inventory and paired source snapshot; resolve candidate-specific missing/changed sources. Prepare matching binary/source downloads and checksums. Publication itself requires user scope. |
| First-user walkthrough | Explicitly unchecked in TODO. | A user can install and complete the documented introductory workflow using installed English help alone; report or fix blockers. |

Signing and a chosen distribution channel need a deliberate decision. Signing is
not currently implemented; test and describe the actual unsigned installation
experience if that is the first-release policy. Do not promise an official build
number, trusted signature or automatic update mechanism that does not exist.

## Acceptance scope versus future features

The remaining research-node visibility decision (Sine Cloud) can be resolved by
keeping its current discoverability and documenting its research purpose, or by
a separately scoped UI change. It is not evidence of a broken installer.

Formula oversampling, plugin-help discovery, Markdown text selection, independent
help language, precompiled plugins and expanded spectral/export capabilities can
remain deferred. Existing unsupported exports must continue to fail explicitly;
release documentation must not advertise these pending capabilities. Bundling
Temurin is future work and is not a gate for the current external-JDK package.

## Validation performed in this audit

| Check | Result |
| --- | --- |
| `scripts/test-source-package.ps1` | Passed: deterministic archives, Unicode, exclusions, overwrite/traversal rejection, mutation detection, staged compiler-cache cleanup and exact tree inventory. |
| `scripts/test-java-prerequisite.ps1 -JdkHome <installed JDK 25>` | Passed: JDK validation and real launcher argument tests; no prerequisite installed. Initial sandbox JDK security-file access failure was resolved by running with access to the installed JDK. |
| `scripts/build-docs.py --output target/release-audit/docs` | Passed: 230 pages, 7,781 local links, anchors and bilingual coverage. |
| Version, lock, build scripts, existing image markers and backlog inspection | Completed; gaps recorded above. |

No full Java/native regression, fresh installer smoke, clean-machine install or
physical-device acceptance was run in this audit. Earlier successful focused
checks remain useful development evidence, not candidate acceptance.

## Ordered execution plan

1. Agree first-release scope and version, curate Unreleased notes and freeze the
   candidate source commits. Use separate clean checkouts/build outputs to avoid
   Eclipse incremental output mixing with release builds.
2. Produce and review frozen external GLSwing artifacts and matching build inputs;
   update the source lock only after source correspondence is verified.
3. Run the complete regression gate. Ordinary isolated Maven verification can use
   `-Deasyknack.buildDirectory=target/release-validation`. Native scripts currently
   read generated contracts from default `target`, so prepare the matching default
   contracts in the dedicated packaging checkout rather than assuming that the
   isolated Maven directory redirects every native path.
4. Build via `Build-Installer.bat` in that packaging checkout. This rebuilds native
   code but does not replace the explicit native/audio test gate. Keep the fresh
   output directory, logs, binary/source hashes and source-review results together.
5. Run `scripts/test-windows-package.ps1 -Image <new EasyKnack folder> -JdkHome <JDK 25>`
   and its `-Window` check; complete clean-system, real-device and old-document
   acceptance. Retest affected gates after fixes and build a new candidate.
6. Record a final pass/fail report tied to immutable artifact hashes. Prepare the
   download set and release announcement for user review; tag, push and publish
   only within explicit user authorization.

Related references: [Windows packaging](WINDOWS-PACKAGE.md),
[build identity](BUILD-IDENTITY.md), [source packages](SOURCE-PACKAGES.md),
[distribution checklist](LICENSING.md), [historical source review](source-audit/RELEASE-CANDIDATE.md)
and [current TODO](../../TODO.md).
