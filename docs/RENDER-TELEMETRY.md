# Render telemetry: audit and semantics

Implementation step A of the DSP roadmap, September 2026.

## Processing-cycle normalization (September 29, 2026)

Render and Peak now use the SAME complete processing-cycle observations. The
engine measures each internal audio block (including graph activation and IO),
then sums measured seconds AND audio samples over a common audio/STFT-hop cycle.
For conventional power-of-two hops this is the least common multiple of block
length and active hops. A 1024-sample hop with 256-sample blocks therefore produces
one observation containing four blocks, not three idle observations and a spike.
Hops below block length already execute all their frames within each observation.
Normal audio work in mixed graphs is included exactly once; stereo, nested graphs,
compiled spectral kernels and joined worker time use the same outer measurement.

FFT producers report their cadence even on blocks without a completed frame;
resource copies and IFFT consumers also report it. Padding does not change hop.
Only executed graph paths contribute. Graph/cadence/rate changes and measurement
reset discard a partial old cycle. Ordinary audio graphs retain block resolution.
Arbitrary overlap ratios can have enormous least common multiples: the observation
length is bounded to 32768 samples (rounded up to a whole block). Such windows can
contain one frame more or less per FFT, so small scheduling-dependent differences
remain. This bound also prevents overflow and multi-second gaps in the display.

The transport layout remains compatible. Its `frames` field now means cycle
samples and `blocks` counts completed measurement cycles. p99 and session maximum
are cycle computation durations; the detail panel identifies the cycle budget.
The trailing-second mean is budget-weighted; Peak is the maximum cycle load.
Constant workloads have equal mean/peak, genuinely slower cycles retain peaks.

This measures processing throughput, not output deadlines. Actual capture/playback
underflows and driver overloads remain separate counters. No output scheduling or
buffer latency was changed. Independent asynchronous analysis-worker CPU time is
not included, as before; work executed on the render thread and joined spectral
workers is included. Cycle aggregation intentionally does not expose shorter
individual block stalls as throughput peaks.

The historical block-measurement description and baseline below describe the
previous instrumentation. Backend callbacks now opt into engine measurement;
there is no second callback-level observation. Offline render calls opt out.

## Writers and clocks (historical baseline)

`WindowsAudioBackend.deviceCallback` measures the master output's `AudioEngine.render`.
Its capture callbacks only fill rings; secondary outputs only read rings. If no master
output drives rendering, `clockCallback` supplies the render clock. `pauseStreams` / `resumeStreams` and
`setClockEnabled` update ownership while callbacks are stopped.

`AsioAudioBackend.process` measures `AudioEngine.render` with the actual ASIO frame count.
It excludes initial output clearing and `driver->outputReady`. `RoutingAudioBackend`
stops ASIO and disables/stops WASAPI clocks before reconfiguring. ASIO and the WASAPI
render clock are therefore alternative writers, not simultaneous writers. Secondary
capture/playback callbacks may update their own atomic IO counters concurrently.

The timing interval includes graph processing, graph-state activation and graph IO.
It is wall-clock time, so OS preemption affects it. It is neither per-node DSP-only CPU
usage nor end-to-end latency. Changing the existing clock/render scheduling was not
necessary to introduce this telemetry.

## Transport and statistics

`RenderTelemetry` is an SPSC queue: 4096 fixed records (elapsed seconds, frames, rate,
steady-clock timestamp). Producer publishes with release; protocol consumer reads with
acquire and only releases slots after copying. No producer allocation, blocking lock,
file IO or formatting. Full queue drops measurements, never audio, and counts losses.
The one-second consumer window is capped at 65536 records; truncation is also reported.

The protocol thread computes a budget-weighted mean, maximum percentage and exact
nearest-rank p99 of recorded elapsed times for the trailing second. The total count,
maximum elapsed time and budget-exceeded count persist for the current measurement.
The UI samples snapshots at its existing 250-ms cadence. Its 60-second trace displays
these overlapping one-second windows, not a claim of 250-ms independent maxima.
When telemetry was lost, statistics describe only retained samples.

A budget exceedance is `elapsed > frames / sampleRate`. It does not prove that an output
missed its device deadline. Device deadline misses outside the measured scope are not
reported as zero: they remain unavailable. Ringbuffer starvation is independently known.

## IO categories

- WASAPI capture-ring starvation / overflow and secondary playback-ring starvation /
  overflow are aggregated separately from the existing per-stream atomic counters.
- Adaptive priming waits count blocks waiting for reserve, including re-priming after
  starvation. They do not increment the underrun counter.
- ASIO overload notifications have their own field; the legacy aggregate `IOSTATS`
  retains its old behavior for compatibility.
- Native counters may include different callback threads, so IO fields are a diagnostic
  snapshot, not a transaction spanning every device at exactly the same instant.

## Reset, lifetime and UI

`DIAGNOSTICS` response version 1 is additive to protocol 10. The legacy `STATS` remains.
Unsupported or malformed detailed diagnostics disable only the detailed view; ordinary
host communication continues and transport failures still reach normal host handling.

`DIAGNOSTICS_RESET` resets only consumer-owned aggregates and discards already published
queued measurements; ongoing render work may publish into the new epoch. IO deltas use
a control-thread baseline; their source counters are never reset concurrently with audio.
Changes in routing/clock lifecycle start a new epoch at the next diagnostic request.
The window between the change and that request is outside the new measurement.

The UI clears history on new host/epoch, retains last values explicitly while paused,
and exports a bounded snapshot as CSV. Host binary SHA-256 is calculated off the UI
thread during export, when available. It identifies the on-disk executable at export
time; app version and plugin cache build ID supplement it. No audio is included.

## Validation

- Java: strict protocol/version/number validation, bounded history and epoch changes,
  live host protocol/reset, paused UI values, GL offscreen rendering.
- Native: percentile/window boundaries, session extrema, invalid measurements, reset,
  full queue, concurrent producer/consumer wraparound, distinct priming and starvation.
- Existing native tests exercise DSP/MIDI/voice/channel behavior, WASAPI and simulated
  ASIO ownership, independent clocks and process transport. Real ASIO hardware remains
  unavailable on the test machine.
- `native/audio-host/benchmarks/TelemetryBenchmark.cpp` compares the writer overhead
  with the historical timing code, then runs the same 64-partial oscillator graph with
  old/new instrumentation. CSV includes mean/p99/max and budget counts, plus bit-equal
  output verification. Run after builds finish; microbenchmarks are not hardware-latency
  guarantees. Real routing-project baselines are the next roadmap phase.


Validation result: full Maven suite 126 tests, zero failures/errors/skips, plus the native
host suites. Six focused tests were rerun after the final UI reset/pause change, all passed.
GL preview: `target/diagnostics-preview.png`. Telemetry writer and graph comparison raw
measurements live under `native/audio-host/benchmarks/telemetry-*-2026-09-18.csv`.
Writer overhead was approximately 0.104–0.126 microseconds per render call. The short graph
comparison does not establish unchanged tail latency: p99/max varied, with one instrumented
budget exceedance. Real routing and sustained-load validation remains an explicit open item.
