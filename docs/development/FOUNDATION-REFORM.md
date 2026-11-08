# DSP foundation work

## Current public rate semantics

Numeric port capabilities are declared with `.rates(PortRates.AUDIO)`,
`.rates(PortRates.CONTROL)` or `.rates(PortRates.AUDIO_CONTROL)`. Flexible inputs
follow their own source even when the node has a fixed Audio output and no Rate
selector (for example Digital Noise). Flexible outputs follow the shared rate
resolver. An optional `ProcessingRate.addEditor` provides the existing explicit
Audio/Control override. Fixed capabilities are not overridden. Trigger, MIDI and
resource ports retain their separate declarations.

The host owns sampling and holding. C++ processors read
`auto input = context.numericInput(port, fallback);` and use `input[i]` in their
sample loop. Its stride is zero for a Control input and one for Audio; no array
expansion or per-node conversion implementation is needed. Such processors declare
`supportsScalarInputs()`. Nodes supporting a one-value evaluation also declare
`supportsControlRate()`; the host selects their block length. Legacy pointer-based
processors remain supported, reusing an upstream Control buffer where possible.

The resolver is shared by ordinary graphs and exported rate plans. New baked
plans retain flexible input capabilities and fixed output policies, so changing
an external source after baking does not freeze its rate at export-time values.
Digital Noise Frequency/Strength and Delay Time now accept Audio/Control; oscillator
modulation inputs also follow their sources. Wavetable data and Complex Oscillator
partial count retain their explicit block-level behavior.

- **Control** supplies one numeric value per DSP block, constant throughout that
  block. Explicit Control inputs sample at block start; Rate Converter also offers
  reductions such as mean, RMS and last sample.
- **Audio** supplies one numeric value per sample. MIDI Note frequency, gate and
  velocity, and MIDI CC values use Audio so intra-block changes remain sample-accurate.
- **Trigger** is a separate port type for timed occurrences, without a held numeric
  value. MIDI is another separate type carrying messages. Neither is an Auto rate.
- Numeric Auto chooses Control or Audio and resolves each input independently.
  Clock Frequency is numeric; Clock Sync, envelope retrigger, Sample & Hold trigger
  and Buffer Reader Reset are typed Trigger inputs with momentary buttons.
  Edge Detector converts numbers to triggers; Trigger to Pulse converts triggers
  to audio (equal-time occurrences add). Sample & Hold with a connected trigger
  resolves to Audio so an intra-block sample is not delayed to the next block.

`ProcessingRate.addEditor` owns the shared numeric selector and translated tooltip.
The internal `EVENT` enum/protocol code and legacy sparse-value scheduler remain for
source-plugin compatibility; new numeric rate selectors never offer this option.
Old document selections of numeric Event migrate to Audio. Direct numeric/trigger
wires are preserved through visible Edge Detector / Trigger to Pulse adapters.

Connected Control values are no longer automatically smoothed. Math, Mix, Map Range,
Clamp, Comparator, Bitcrusher, Curve, Unit Converter, Clock and Sample & Hold can
read zero-stride scalars without allocating or filling temporary held-input buffers.
Existing manual fallback smoothing remains; Math/Mix Control smoothing advances once
per block. Live Value-slider updates smooth at their source, once per block, without
turning the Control output into an audio ramp. Trigger/discrete editors never glide.
Local bakes resolve input rates independently and retain typed public trigger ports.

The isolated `RateInputBenchmark.cpp` compares Math(Audio, Control) with explicitly
expanded controls (including checksum overhead). On this machine, nine interleaved
runs at 256 samples yielded medians of 899 ns/block versus 915 ns/block (~1.8%).
This is not a whole-graph or realtime latency benchmark. Removing each temporary
held input also saves 1 KiB per processor/voice instance at a 256-sample block size.

The following sections record the preceding implementation stages.

Active order (supersedes the older optimization ordering in TODO.md):

1. Diagnose telemetry costs and preserve separate deadline/underrun counters.
2. Preparation lifecycle, live parameter updates, safe DSP state ownership.
3. Structural contracts versus presentation; automatic/explicit property provenance.
4. Per-output rates and sparse value/trigger events, including exported graphs.
5. Grouped ComboBox presentation and zoom-aware analysis rendering.
6. Typed shared resources, initially history writer/readers.
7. Reusable analysis resources, viewers and bounded background analysis.

Keep the existing nodes and manually edited DSP implementations during migration.
Layout edits must never rebuild DSP. Worker results need timestamps and stable input
snapshots; asynchronous analysis must not silently replace synchronous modulation.
Repeated triggers are events even when their numeric value does not change.

## Audit

- The engine already preserves sample offsets with event masks, but materializes
  held values as dense buffers. This is a representation/dispatch migration.
- `AnalysisEditor` already uses the rendering transform scale in both drawing paths.
- `RenderTelemetry` uses an SPSC queue; sorting/allocation occurs on the protocol
  reader, not inside `observe()`. A render-time peak alone is not an audio dropout.
- Delay currently reserves a fixed 48 kHz buffer and loses buffer contents across
  graph replacement. Preparation and ownership transfer must address both.

## Progress

### Implemented

- `NodeProcessor.prepare(rate, frames)` runs before publication. Delay storage follows
  the prepared sample rate, including fractional sub-sample delays. Delay, history,
  MIDI queues/notes and the larger analyzer state transfer storage at graph handover.
- Compatible `GRAPH` parameter messages become coalesced parameter snapshots. Live
  processors, phase, filter state and delay history survive slider edits. Curve tables
  are prepared on the control thread; retired parameter resources return there for
  destruction. Structural edits still compile a new graph. Configuration prototypes
  are parsed off the audio thread; this is not an allocation-free control path.
- The Core model persists automatic versus explicit properties. Legacy unmarked
  values remain explicit. Reset to automatic is available in slider context menus,
  including the inspector. Math changes automatic defaults without inventing node
  types for labels, ranges or defaults. Explicit hidden operands survive.
- Core ComboBox separators and existing nested groups keep presentation rows out of
  the selectable values. Math uses its operation groups. The manually improved
  zoom-aware analysis renderer is retained.
- Protocol **11** supports individual output rates, including Digital Noise's audio
  factor and event trigger. Exported Java definitions and native plans retain these
  rates. Rebuild source plugins against the updated SDK; old binaries cannot connect
  with protocol 10.
- Fixed-capacity value-event streams distinguish held changes from explicit triggers.
  Equal-time retriggers survive, Clock skips unchanged sample spans, Sample Hold
  reacts to trigger events, and Math/Map Range share the block event adapter. Buffers
  are expanded where dense consumers need them. Legacy processors can continue using
  dense buffers. Each stream holds 512 events per block; overflow drops newest events
  and increments its counter. A numeric audio port can represent only one pulse value
  at a sample; the event port retains multiplicity.
- History and analysis ports are separate compatibility types, with their own theme
  colors. Compiled bindings refer to stable resource handles, and graph-owned storage
  is shared by readers. Resource ports work across group/plugin boundaries.

### Using the analysis nodes

`Audio → Buffer → FFT Analysis → Analysis View → Analysis Viewer`

The Buffer can also feed multiple Buffer Readers, each with independent
speed and offset. The Buffer exposes a Size editor (one DSP block to 30 seconds, default 2 seconds)
without a signal input. The minimum is currently 256 samples (5.333 ms at 48 kHz),
also enforced against the prepared block size in the native host. Capacity is
rounded to a whole sample with no extra guard samples. Resizing prepares a new empty ring outside the audio thread
and resets attached readers. Stable type/port IDs are unchanged by the display-name
rename. Readers wrap around the entire allocated ring and interpolate
across its physical boundary. Unwritten regions remain zero during initial filling.
Offset is now a normalized fraction of the ring length, clamped to 0–1: 0 and 1
are equivalent; 0.5 is a half-ring offset. Previously saved nonzero offsets used
seconds and must be adjusted to the new fraction when reopening an older document.
The audio-rate Distance output is twice the shortest circular distance divided by
the ring length (0 at the write head, 1 opposite it), including the offset.
It follows the sample-accurate write head: for sample i, writes through i are
visible, but later writes in the block are not. Before writing, Buffer preserves
one block of overwritten values in preallocated storage shared by its readers.
Both interpolation endpoints use that temporal view. FFT snapshot consumers still
see the completed block. Reader sample values and Distance therefore match
samplewise write-then-read execution even when capacity equals one DSP block. There is
no built-in crossfade or second head. A changed/reallocated history resets its
read position. The input writer sums polyphonic voices, while stereo channels retain
separate histories. Use Channel Separate when a particular channel is needed.

FFT Analysis takes stable 8192-sample snapshots every 4096 samples and submits them to
one reusable worker. Its bounded queues never wait in the audio callback. Results
contain source identity, end-sample timestamp, sample rate, window/FFT sizes, bin/lag
spacing and estimator metadata. Magnitude, phase, power and the full positive-lag
correlation share one cached FFT. Correlation preserves the existing DC-centered,
unbiased estimator and symmetric interpretation. View selection does not run another
FFT. Statistics provide peak, mean, RMS, integral, frequency, periodicity and
correlation time; the viewer is independent of the calculation.

This path is deliberately **asynchronous**: at 48 kHz, a window spans about 171 ms,
updates arrive about every 85 ms plus worker latency, and statistics hold the latest
completed result. It must not be mistaken for a zero-latency modulation source.
Overloaded workers discard snapshots rather than blocking audio. Existing scope and
autocorrelation nodes remain available with their existing synchronous behavior.

### Diagnostics

Deadline violations, telemetry queue drops and device/ring-buffer xruns remain separate
counters. The protocol reader reuses percentile scratch storage and uses selection
instead of sorting the complete window. Aggregation never runs inside the render
callback. The offline polling experiment and limitations are recorded in
`native/audio-host/benchmarks/archive/foundation-2026-09-23/README.md`.

### Verification

Reader follow-up: the normalized-offset/wrapping change passes the Maven/GL run
(149 tests, 0 failures/errors, 33 optional integration tests skipped), plus a
separate five-test native resource/export/analysis integration run. Its native
regressions cover forward/reverse wrap, interpolation over the ring boundary,
normalized offset modulation, write-head distance and missing-resource silence.
The complete foundation verification below predates this small follow-up.

- Native build and tests pass: WASAPI, ASIO/independent clocks, graph DSP,
  preparation/state adoption, coalesced live parameters, sparse triggers, resource
  boundaries, analysis worker, shared transport and driver bridge.
- The targeted EasyKnack run passes all 22 tests, including compiled resource
  exports in both channels, the asynchronous analysis host/protocol path and bake
  retention after explicit changes to external sliders.
- The eight targeted Core tests pass and the Core artifact is installed locally.
  The full Core run has 92 tests with the same seven pre-existing failures:
  `GLNodeInsertionTest.exclusionReconnectsAndOnlyClosesSimpleChains` and
  `MavenSmokeTest`'s configurationEditors, controlsAndFocus, lists,
  localizationAndThemes, menus and themeCodeSupport. These concern existing layout,
  theme resource and UI expectations; manual theme changes were preserved.
- The final complete EasyKnack Maven `verify` passes **148 tests, zero failures,
  zero errors and zero skipped tests**, with audio, GL rendering, device and
  reference tests enabled. The application JAR, native host and embedded plugin SDK
  are rebuilt. All 32 export reference cases are bit-identical, including state
  transfers; results are retained in
  `native/audio-host/benchmarks/archive/foundation-2026-09-23/export-equivalence.csv`.
  Logs are stored in `target/foundation-verify-final.log`,
  `target/foundation-native-final.log`, `target/foundation-core-final.log` and
  `target/foundation-core-full-final.log`.


### Time units

Time sliders offer seconds, milliseconds and samples (suffix `smp`) through the Unit context menu.
This includes Buffer Size, Delay, Envelope durations, MIDI Delay and Scope Window;
Time Converter and compatible group-port units include samples as well. Stored time
values retain their original seconds/milliseconds representation. The UI uses the
host's current fixed 48 kHz DSP clock, not an external device clock (480 samples =
10 ms). Native conversions use the processing context's sample rate and are tested
at 44.1/48/96 kHz. Sample-dependent conversion nodes stay out of constant-folded
Math regions so exports preserve their clock-dependent semantics.

### Sample-accurate buffer access

The temporal shadow uses one maximum DSP block per channel/voice-local Buffer
(1 KiB per channel at 256 float samples), independently of reader count. Preparation
allocates it off the audio thread; block processing only copies into existing storage.
Graph handover swaps sample and shadow ownership without large realtime copies.
An independent samplewise oracle checks six readers (stopped, forward, reverse,
fractional and faster-than-write), modulated offsets, stereo data, 256/257/1024-sample
rings, changing 1/17/128/256-frame blocks, startup silence and state adoption.
There is still no automatic crossfade: crossing the actual old/new-data seam may
produce a discontinuity, which external windowing can now track sample-accurately.

Temporal-view validation: native FoundationTest and AudioGraphTest pass; the targeted
Java run passes 14 tests (2 optional native cases skipped), followed by all five
FoundationReform integration tests with native execution enabled. Application,
audio host and plugin SDK are rebuilt. The offline before/after benchmark and its
limitations are recorded in `native/audio-host/benchmarks/archive/foundation-2026-09-23/README.md`.

### Reader position and synchronization

Buffer Reader owns a clock (Speed, Offset and Reset). Its Audio-rate Read position
output reports the actual circular address in [0,1), including Offset. Distance
remains the shortest normalized distance to the sample-accurate write head.

Position Reader has only Buffer, Position and Offset. It has no clock or reset;
connect the same Buffer and Buffer Reader.Read position to Position Reader.Position.
An Offset of 0.5 keeps the follower half a ring away, even when connected later.
Position wraps; Control positions hold for the block, Audio positions update each
sample. Its outputs are Audio, Distance and Read position. Crossfade remains external.

Write-head crossing has been removed. Loading an old document removes its obsolete
wires, including exposed group outputs. Old compiled plugin plans keep a silent
former trigger slot until re-exported, never interpreting position values as events.

Reset still accepts sample-accurate triggers, with a button while unconnected.
Trigger Input can reset several clocked readers together. Trigger Converter (formerly
Edge Detector, with its stable type ID retained) converts Audio/Control threshold
crossings into Rising, Falling or Both triggers. The initial value establishes state
without triggering; Control transitions occur at block boundaries, Audio transitions
at their exact sample. Internal baked trigger buttons retain their instance aliases.