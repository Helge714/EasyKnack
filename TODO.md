# EasyKnack backlog

Current work only. Historical implementation notes, superseded plans and original
bug reports are preserved in [the October archive](docs/development/history/TODO-2026-10.md).

## Current stabilization pass

- [x] Preserve direct group forwarding, copied editor identity and custom viewer
  sizes across outer parameter edits, navigation and Undo/Redo (regression tests).
- [x] Reuse a local Bake for equivalent instances of one group definition, including
  Multiple Groups; keep processor state independent for each instance. Rate/unit
  specializations remain distinct. Internal edits invalidate the affected Bakes.
- [x] Attach MIDI inspector indicators when controls enter the inspector hierarchy.
- [x] Add Step Switch index selection and editable input names in the inspector.
- [x] Replace Complex Oscillator cent spreading with power-law partial stretching
  (exponent 0–4, neutral 1); verify all 144 ratios and one-time scalar migration.
  Review old modulation, inherited group editors and generated exports manually.
- [x] Finish review, validation and focused commits for accumulated Core and
  EasyKnack source changes. Keep machine-specific Eclipse settings local.
- [ ] Manual acceptance: nested group Undo/Redo and directly forwarded ports;
  independently copied FFT window editors; repeated inspector selection; duplicate
  ordinary/Multiple Group Bakes; Step Switch mode changes and named inputs.

## Next technical work

- [x] Audit the optional native contract sweep: Frame Trigger requires spectral
  frame context. Validate all temporal contracts separately, reject bin-only nodes
  before host submission, and verify frame triggers in native spectral execution.

- [ ] Generate shared instance-processor capability declarations instead of keeping
  separate Java supported-node IDs and a native factory whitelist. Cover live
  execution, stereo/rates and Bake/export; document deliberate exclusions.
- [ ] Extend export contracts for coupled standalone sliders, checkboxes and trigger
  actions. Retain explicit export errors until each binding can be represented.
- [ ] Extend spectral-group boundaries and shared effects deliberately, with defined
  frame timing and resource ownership. Keep spectral execution a distinct domain.
- [ ] Support precompiled plugin installation as an optional future distribution
  format; preserve editable source-plugin packages and compatibility checks.
- [ ] Profile remaining mixed/stateful Bake kernels with repeatable benchmarks.
  Share compiled code without accidentally sharing per-instance signal state.

## Release readiness

- [ ] Test installation, upgrade, uninstallation, file association and recovery on a
  clean Windows machine without development tools. Test read-only install paths,
  external x64 JDK 25 discovery/installation assistance and compiler prerequisites.
- [ ] Complete exact-artifact license/source/notice verification before publication;
  use the current legal/distribution documentation, not archived FFmpeg plans.
- [ ] Perform a first-time-user walkthrough using only installed English user docs.
- [ ] Establish the first release baseline and official CI/publication workflow.
  Keep product versions in Maven and local build identities explicitly local.
- [ ] Bundled Temurin is optional future work, pending a complete redistribution
  inventory. Current distribution uses an external JDK.

## Later product work

- [ ] Audio recording and offline rendering; transport/tempo synchronization.
- [ ] Reusable group library, presets, automation and plugin management improvements.
- [ ] Dynamics, pitch tools, performance diagnostics and latency compensation.
- [ ] Long-period autocorrelation using a defined low-rate analysis path.
- [ ] Long-running physical-device/USB/Bluetooth routing acceptance tests.
- [ ] VST3 hosting/plugin integration, then JACK if needed. Own virtual-driver
  development remains deferred; do not require Secure Boot/test-signing changes.

The older [event plan](TODO_EVENT.txt) and [routing plan](TODOs_IO-Routing.txt)
are historical design references, not instructions to reintroduce obsolete public
rate semantics or to resume kernel-driver installation.

## Latest validation (29 October 2026)

- Core: full Maven test/install, 132 tests, no failures/errors, one optional skip.
- EasyKnack: full Maven package, 590 tests, no failures/errors, 154 optional tests
  skipped in the regular profile. Selected native checks were run separately.
- Additional native checks passed for Step Switch sample/trigger/index behavior,
  all-partial power stretching and legacy cent-contract conversion, generated Instance kernels, spectral hold/memory/formulas,
  filters, Blur synthesis/power, IFFT stability and unit conversions.
- A real transient plugin build reused its cache; Force rebuild produced a fresh
  host while the earlier binary remained usable. The development host was rebuilt.
- Hardware, clean-machine installation and the manual acceptance list above remain
  separate checks; these results do not imply those tests were performed.
