# Release regression run — 2026-11-10

Application baseline: clean `20bd9bd`; external Core: clean `2f5ed7c`, frozen and
reviewed in [Core correspondence](GLSWING-2026-11-10.md). Subsequent source-lock
documentation commits do not change application production code. No installer,
release tag, upload or clean-machine acceptance is implied by these tests.

## Environment and execution

- Windows x64; installed Temurin JDK 25, Maven 3.9.11; LLVM-MinGW 20250305 UCRT
  x86_64 from the existing local toolchain.
- Separate clean local clones under `target/release-validation-20261110/core`
  and `app`; each detached at the identified source commit. Eclipse build output
  remains separate. The application's ignored `.local/tools` junction exposes
  the installed toolchain for normal plugin compiler discovery.
- Core: `mvn -pl glswing-core -am -Dglswing.test.gl=true install`.
- Prepare application contracts: `mvn process-classes`, then
  `scripts/build-audio-host.ps1 -Cxx <LLVM-MinGW clang++.exe>` without SkipTests.
- Application: `mvn -Daudio.cxx=<LLVM-MinGW clang++.exe> -Deasyknack.test.audio=true
  -Deasyknack.test.gl=true -Deasyknack.test.references=true package`.
  JDK/toolchain bins are on PATH and the local Maven repository is explicit.
- Device-specific, private-document migration/rendering and optional benchmark
  flags are not enabled. They require separate input/environment and are not
  silently counted as accepted coverage.

## Results

- Core: 171 tests; no failures, errors or skips.
- Native: all five programs pass: WindowsAudioBackendTest, AsioBackendTest,
  AudioGraphTest, SharedAudioTransportTest and DriverBridgeTest. These include
  deterministic backend tests, not physical-device listening acceptance.
- Application full run: 723 tests in 221 test classes, 14 failures, one error and
  seven skips; Maven exits 1 after 58:07 minutes. The package phase is not reached.
  This is the honest original frozen-baseline result, not a green build.
- Focused follow-ups after test corrections/configuration: 14 tests pass, no
  failures/errors/skips. They resolve ten baseline failing/error cases; four
  additional passing theme cases were rerun with their corrected class.
  **Five baseline cases remain unresolved**, detailed below. Production code
  was not changed during these repairs. A second full hour-long run was not
  performed for test-only corrections; original and focused reports remain separate.
- Seven explicit skips: four physical audio/MIDI-device cases, one private falloff
  document conversion, one optional instance benchmark and one private-document
  slider rendering test. None is counted as accepted release coverage.

Exact totals, original failing cases, focused results, unresolved cases and skip
reasons are in [the machine-readable report](regression-2026-11-10.json). Local logs,
reports and a source snapshot are preserved under `dist/regression-20261110-20bd9bd`.

## Corrected test setup

These changes affect tests only; they do not change candidate production code.

- `ApplicationThemeTest` first measured document views under the default Core
  theme, then compared their dimensions with the EasyKnack theme. Startup applies
  the application theme before document presentation. Match that setup before
  testing that switching application variants leaves documents unchanged.
  Focused rerun: all five theme tests pass. Commit `b64f0c7`.
- `CorrelationFeaturesTest.cpp` attempted to copy a processor whose base now owns
  unique control-interpolation state. Initialize its settings through the existing
  parameter-transfer method instead. The native correlation assertions pass in
  the focused Java/native rerun. Commit `45be9a9`.
- `FoundationReformTest` compiled its resource/export fixture without the required
  Loris implementation, causing undefined LorisStream symbols. Use the shared
  native test compile/run helper, which already links that implementation.
  Focused rerun passes the original stereo resource assertions. Commit `54fa0b7`.
- `OptimizationTest.cpp` still expected the retired cent-offset spectrum, despite
  the documented exponent-spreading contract. Compare all 144 ratios against an
  independent power reference for exponents 0, 0.5, 1, 1.4, 2 and 4 (relative
  tolerance 1e-12 for equivalent floating-point evaluations); retain exact weight,
  SIMD and channel-buffer comparisons. The complete focused test passes.
- `PureFusionTest` stopped before its numerical test because it required exactly
  one reused expression, while the export now reports two. Require actual constant
  folding and reuse without freezing their exact counts; retain the fused versus
  scheduler comparison across rates, stereo and short buffers. Focused rerun passes.
- `OscillatorResetTest` synthesized old payloads by truncating current encoder
  output. The additive contract now includes a `power` marker, invalid in the old
  contract. Use fixed historical payload fixtures. The full event/chunk/stereo/
  state-transfer case passes. Commit `1f4bf16` also links the reference matrix's
  standalone native executable with Loris; that complete matrix passes after rerun.
- `SpectralFeaturesTest.cpp` had the same noncopyable-processor settings issue as
  the correlation test. Use parameter transfer; the original moments, flux,
  frame-cadence and peak-capture assertions pass in the focused rerun.
- `WaveformTest` also omitted Loris from its standalone link command. Add the
  required source while preserving its O0 compilation and original assertions;
  the focused native table/capture/oscillator test passes.

One additional baseline error was test configuration: MeasurementRecorderTest
requires `easyknack.test.host` explicitly. Its complete native/manual-capture case
passes when supplied with the freshly built snapshot host path. Record this
property for future full native-enabled runs.

## Open release blocker: grouped feedback with a muted external Delay

Reproduction: `FeedbackWarningAudioTest.groupingMutedDelayPreservesAudio`.
Two multiply Math nodes form a loop through a Delay; the first Math editor
fallback is 0.4 and both B values are 0.5. Delay is muted and its output also
feeds the audio output. Without grouping, the unsafe feedback connection falls
back to the editor value and the audio samples equal 0.1.

The test extracts three selections and compares before/after audio:

| Selection | Before extraction | After extraction |
| --- | --- | --- |
| Both Math nodes and Delay | 0.1 | 0.1 |
| Both Math nodes, Delay stays outside | 0.1 | **0.0** |
| Delay only | 0.1 | 0.1 |

An additional native probe of the six generated patches confirms these values;
the original assertion has not been weakened. The failing grouped patch includes
a boundary unit adapter that falls back to zero. The precise correction still
needs implementation and verification; do not infer that all grouping or all
feedback is broken. Keep this as a release blocker until the existing regression
and relevant group/feedback/export checks pass.

## Open finding: Harmonizer pitch acceptance

`HarmonizerTest.exampleAndExportRunWithAnalysisAndStatefulNodes` fails its native
assertion that PitchHold has accepted the source and holds its frequency within
one percent. The fixture tests 660, 1600, 3000 and 4400 Hz with live analysis and
exported stateful execution. Re-running the already built native executable
independently reproduces the same assertion failure. The particular frequency
and cause are not yet isolated; do not claim a general pitch-detection failure
or weaken the tolerance without diagnosis. This remains an open release gate.

## Other unresolved export validation

- `SourceExportAndAnalysisTest.generatedNativeGroupMatchesOscillatorAndAnalysisDetectsPitch`:
  generated native program exits with status -1073741571 (`0xc00000fd`), without
  completing the assertions. Inspect the standalone fixture's large analysis
  objects and stack usage; no fix or successful rerun is claimed here.
- `SourceExportAndAnalysisTest.hostAcceptsTemporalContractsAndPublishesLiveAnalysis`:
  its root-graph contract sweep attempts to run `easyknack.partials.context`.
  Production correctly rejects Partial-only nodes outside a Partial Group. The
  sweep handles spectral-only nodes but has not yet incorporated this domain;
  adapt it and rerun its remaining host/analysis assertions before closing it.
- `SpecializedChainTest.specializedChainMatchesOriginalPreviousExportAndHandwritten`:
  `SpecializedChainHarness.hpp` attempts to copy OscillatorNode, MathNode and
  FilterNode into optional values. Processor-owned unique state makes those
  copies invalid. The benchmark reference needs a deliberate ownership/state
  initialization update, then compile and numerical equivalence validation.

These three cases remain failed validation gates, not demonstrated faults in
the installed export workflow. They must not be silently excluded to report a
green complete suite.

## Remaining acceptance

The fresh installer/image gate, source-material review of that complete image,
clean-Windows install/upgrade/uninstall, real-device tests, old user-document
acceptance and first-user walkthrough remain open. See
[release readiness](../RELEASE-READINESS.md).
