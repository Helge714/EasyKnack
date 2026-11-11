# Release findings follow-up — 2026-11-11

This closes the four actionable nonexperimental failures from the November 10
audit and places Pitch Hold behind an explicit experimental opt-in, as requested.
It is focused regression evidence, not a new complete-suite run or installer
acceptance. Original results remain in [the baseline report](REGRESSION-2026-11-10.md).

## Changes and causes

1. **Grouped feedback:** a socket-only public input had no explicit editor default.
   The unit boundary adapter and instance parameter rewrite both substituted zero,
   losing the internal Math node's stored 0.4 fallback. When the unsafe feedback
   connection was cut, this produced silence instead of 0.1. Inherit the internal
   numeric fallback, including nested inputs and unit conversion, while preserving
   explicit public editor defaults and Multiple Group Count behavior. Regression:
   all three grouping selections render 0.1 before and after extraction.
   Commit `472eb5a`.
2. **Pitch Hold:** add reusable `@RegisterNode(experimental=true)` metadata and an
   application preference, off by default. Pitch Hold is labeled Experimental.
   Creation menus, dependent group entries and examples follow the preference;
   cached example classification observes file changes. Newly exported plugins
   inherit the metadata, verified by compiling/loading an actual exported class.
   Loading existing graphs retains their nodes and execution. The setting does not
   claim to repair Pitch Hold accuracy. Its known failing Harmonizer accuracy case
   remains available only when both `easyknack.test.audio=true` and
   `easyknack.test.experimental=true` are explicitly set. Other Harmonizer tests
   remain enabled. Commit `111f70c`.
3. **Native export/analysis crash:** the standalone test program put multiple large
   analysis objects on its Windows stack and exited with status `0xc00000fd`.
   Allocate those fixture objects on the heap. The existing oscillator/export,
   pitch/confidence, silence, stereo-voice and analysis-frame assertions now pass.
   Runtime processors already use heap ownership; DSP implementation is unchanged.
4. **Temporal contract sweep:** assert that Partial-only nodes are rejected outside
   a Partial Group, matching the existing spectral-domain check and production
   policy. Continue validating the remaining temporal contracts and live analysis.
5. **Handwritten benchmark reference:** transfer ownership of the configured
   oscillator, Math and Filter processors instead of attempting forbidden copies
   of their unique runtime state. Link the standalone comparison with Loris.
   The original numerical comparisons pass. Items 3–5: commit `205f791`.

## Validation

- Bundled focused suite: 52 tests, no failures/errors, one deliberate experimental
  skip; includes native audio/export checks and enabled OpenGL coverage.
- Final Count/default/palette checks after the small Count preservation refinement:
  successful Maven package. Across the retained focused reports, **56 cases** are
  represented: **55 passed, one experimental skip**, no failures or errors.
- [Machine-readable results](release-fixes-2026-11-11.json) identify each suite and
  the skipped Harmonizer case. The full 723-case suite was not repeated for this
  focused follow-up.
- Documentation preview: 230 pages and 7,781 local links/anchors validated, including
  both Pitch Hold references and both settings explanations.
- Generated Java/native parameter contracts match the baseline host byte for byte:
  SHA-256 `8022d4fbb88d256c6e90cef6d5f6919c1e3c9a2539b79a37fe8a0c5151cb6df9`.
  No audio protocol, document format or plugin ABI version was changed.
- EasyKnack remains at its current first-development `0.0.1-SNAPSHOT`; the pending
  first-release baseline covers this feature/fix scope. GLSwing is unchanged.
- Local Eclipse classes, translations and bundled user documentation were refreshed.
  Build logs/reports are preserved under `dist/release-fixes-20261111-111f70c`.

The fresh installer/source pairing, clean-machine installation/upgrade/uninstall,
real-device acceptance and first-user walkthrough remain pending. The known
experimental pitch-accuracy limitation is retained explicitly; no release tag,
push or publication was performed.
