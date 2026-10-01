# Diagnostic polling experiment — 2026-09-23

`DiagnosticPollingBenchmark.cpp` measures a 64-partial oscillator graph with paced
128/256-frame blocks, 360 blocks per case, two rotated-order repetitions.
Modes: 0 = no telemetry; 1 = telemetry writer without snapshots; 2 = writer plus a
separate reader polling every 50 ms (more often than the normal UI).

The CSV contains render-plus-observe durations, deadline misses, maximum snapshot
aggregation duration and telemetry queue drops. There is **no audio device** in this
experiment, so it cannot count or prove absence of audible xruns.

Snapshots took at most 152.2 µs. No telemetry queue drops occurred. One render deadline
was exceeded in a case with telemetry **without polling**; none were exceeded in the
polling cases. Means and maxima varied substantially across repetitions (including
unmeasured host load/power management). This does not prove polling has zero effect
on real-device latency, and it does not establish a reproducible polling-caused spike.
Do not label a DSP peak or a single offline deadline miss as a device dropout.

The existing separate WASAPI/ASIO/ring-buffer counters remain the evidence for device
underruns. A long paired real-device capture under identical load is still necessary
for a strong latency-regression conclusion.

## Export regression

`export-equivalence.csv` records the final reference matrix after the foundation
changes. All 32 scenario/block-size combinations have zero bit differences and
zero absolute error over 36,000 samples per channel, including three state
transfers. Scenarios cover math, stereo, 1/8/32-voice synths, rates, analysis and
the oscillator/gain/filter chain. This is an equivalence check, not a claim of
realtime feasibility for every graph or of improved benchmark performance.

## Sample-accurate Buffer access

`BufferTemporalBenchmark.cpp` compares the previous completed-block reader with the
new temporal view, including buffer writes, stereo and Distance output. Both use
256-frame blocks at 48 kHz; each case warms up for 200 blocks and measures 1000,
with three alternating-order repetitions. No compiler ran during measurement.
Raw means/p99/maxima are in `buffer-temporal.csv`. Median block means (microseconds):

| Capacity | Readers/channel | Previous | Sample-accurate | Extra |
| --- | ---: | ---: | ---: | ---: |
| 256 | 1 | 11.309 | 18.605 | 7.296 |
| 4096 | 1 | 12.081 | 16.928 | 4.847 |
| 96000 | 1 | 10.788 | 15.668 | 4.880 |
| 256 | 8 | 85.256 | 157.325 | 72.069 |
| 4096 | 8 | 85.428 | 119.861 | 34.433 |
| 96000 | 8 | 99.260 | 123.549 | 24.289 |

The reader microbenchmark costs 25–85% more depending on the case, although the
absolute additional cost is 5–7 us for one reader per stereo channel, or 24–72 us
for eight. Total measured means consume 0.29–2.95% of the 5333 us block budget.
This short offline comparison includes neither the full graph scheduler nor audio
hardware, and the running desktop was not isolated. It is not a device-xrun or
worst-case latency guarantee. The independent samplewise oracle, rather than this
timing benchmark, verifies correctness: the old and new readers deliberately differ
when the old blockwise implementation would expose future writes.
