# Repository cleanup worklist

Approved scope: EasyKnack and GLSwing structure, generated storage, builds,
documentation and internal responsibilities. Preserve user edits and external
Maven consumption. No publishing, Git-history rewriting or document/ABI changes.

## Work packages

- [x] Remove tracked Maven repository debris from GLSwing; guard against recurrence.
- [x] Repair the external Core consumer fixture and verify the full reactor.
- [x] Remove old packaging staging after reporting the exact allowed directories.
- [x] Separate durable toolchains and new release artifacts from disposable builds; preserve existing research material.
- [x] Consolidate native compiler configuration and build entry points.
- [x] Organize documentation, historical TODOs and deferred driver material; repair links.
- [x] Separate public examples, test fixtures and historical benchmark artifacts.
- [x] Move language policy out of the UI package and organize application services.
- [x] Separate graph compilation/export from presentation, with matching tests.
- [x] Extract cohesive helpers from oversized application/native controllers.
- [x] Extract cohesive Core editor/workspace/code-editor internals without public API breaks.
- [x] Introduce a reusable Core Markdown model/view, retaining application navigation policy.
- [x] Establish formatter configuration and focused, reviewable formatting boundaries.
- [x] Update maps, release notes and validation results; commit coherent steps.

## Preservation rules

Existing Eclipse settings are user edits. Keep installers, research material,
measurement baselines and active runtime plugin caches unless their safe disposal
is established. Move rather than delete historical source artifacts. Public Java
packages, native SDK include paths and source scanning are contracts: retain
compatibility adapters where needed. Do not reformat vendored libraries.

## Validation and open decisions

Record each completed step and its checks below. Uncertain removals or public
API changes remain explicitly open rather than being guessed.


## Completed changes and checks

- Core commits: repository cache hygiene and current-version consumer fixture;
  shared Markdown model/view, source-text helpers, tessellation cache, workspace
  snapshot policy and module-owned external demo assets. Full reactor `verify`,
  including the isolated Maven consumer, passed; Core was locally installed.
- Application implementation now has `app`, `i18n`, `compiler`, `document`,
  `graph` and `analysis` ownership boundaries. Public UI aliases preserve source
  and binary entry points where applicable; preferences keep their existing path.
  Export orchestration and group-interface policy remain application adapters.
- Native implementation folders are `runtime`, `resources`, `analysis`, `dsp`
  and `protocol`. Forwarding headers preserve SDK paths. Per-node runtime storage
  is separated from the scheduler in `runtime/GraphNode.hpp`.
- Native build flags/libraries come from `build.properties`; PowerShell host and
  installer builds share one compiler invocation, and plugin builds use the SDK
  copy of that policy. CMake also reads the policy and checks generated contracts.
- Eight old GUID packaging directories were removed (about 2.45 GiB). Compilers
  and WiX moved to ignored `.local/tools`; new deliveries go to ignored `dist`.
  Existing packages and research data were retained.
- Technical documentation moved under `development`, dated plans under `history`,
  test projects under `src/test/resources/reference-projects` and dated benchmark
  evidence under `native/audio-host/benchmarks/archive`. Deferred driver sources
  live under `native/experimental/virtual-audio`.
- Formatting rules are explicit (`.editorconfig`, `.clang-format`). Focused new
  native helpers were formatted; vendored code and historical generated snapshots
  were not reformatted. Workflow tests now have functional names. One legacy pair of Windows-1252 quotes in the release notes was normalized to UTF-8.
- Final application package build: 709 tests, zero failures/errors, 174 optional
  skips. Targeted native execution, source-export/plugin integration, documentation
  validation, source-package safety and Java prerequisite checks also run separately.
- Aggregate native tests were updated where old fixtures treated numeric audio
  buffers as Wavetables or expected the retired uniform partial spreading model.
  Current resource validation was kept intact. The export-equivalence fixture now
  describes MIDI numeric outputs at Audio rate, matching the live node contract.
- Real plugin import/cache/failure fallback and serial/parallel/baked Spectrum
  execution passed. All seven advanced export scenarios (including external
  polyphonic voices) passed with current rate metadata.
- Moved `.knack` reference fixtures were checked byte-for-byte against Git.

## Explicit follow-ups / retained material

- [ ] Review old `target` research/audit environments and older installers before
  further removal. Some Python environments contain absolute paths; raw readings
  and measurements may not be reproducible. No blanket cleanup is authorized by
  this document; use the recorded inventory and preserve originals.
- [ ] Continue decomposition of the remaining large renderer, popup and showcase
  classes only with a separate behavior-specific plan. The verified helper
  extractions above do not claim that every large class has been rewritten.
- [ ] A complete split of group-editor/compiler orchestration and every public
  widget/node package needs a dedicated compatibility plan. Existing public names
  remain stable; moving all of them merely to change folder appearance would add
  API risk. Pure compilation kernels and data policy have already been separated.
- [ ] Broader formatter rollout should be a formatting-only change with an agreed
  pinned Java formatter, separate from behavior changes. Current rules establish
  the boundary without rewriting every existing source file.
- [ ] CMake execution remains unverified here because no CMake executable is
  installed. The shared PowerShell/Clang build and its native tests are exercised.

No product release, Git-history rewrite or remote push is part of this cleanup.
Product development versions remain unchanged; document schema, plugin ABI and
host protocol are unchanged. SDK content changes correctly invalidate build caches.


## Final packaging verification

- Installer built from commit `3c2c1a3` under
  `dist/20261108-234907-880`, including matching project/source-material archives.
  The installer build ran 709 tests with zero failures/errors (174 optional skips).
- Packaging staging was automatically removed after success; build logs were
  retained in the destination's `build-logs` directory.
- Packaged window launch, responsiveness and normal application close passed.
  The probe waits for the displayed workspace, excluding transparent/offscreen
  preparation frames. An initial theme/layout checkpoint regression was fixed:
  startup view initialization no longer marks a new document modified, while real
  edits and recovery documents remain dirty (three focused regression tests).
- The final packaged functional smoke test passed with isolated test data:
  group export, real Java/C++ plugin compilation, audio-host parameter contracts
  and a restart loading the plugin cache. MP3 decoding is included in the probe.
- Packaging copies and checksum inventories now use one .NET directory traversal
  rather than one PowerShell pipeline per file. Source-package checks cover exact
  bytes/hashes, Unicode names, empty directories and unsafe destination rejection.
- Existing public-release checks remain open: clean-machine Java installation,
  upgrade/uninstall and publication. The source review reports no missing sources;
  the newly rebuilt first-party Core JAR has a new hash requiring the normal
  release review. No new third-party library was introduced by this cleanup.
- Final source-only polish aligns PowerShell line endings with Git attributes
  and removes two extra EOF blank lines; executable behavior is unchanged.
