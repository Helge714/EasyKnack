# Spectral runtime measurements — 2026-09-29

Measured the user's saved FFT-Noise graph without modifying the document. Its
signal path is stereo input → synchronous FFT (512, Hann, 50% overlap) → Spectrum
Blur → two Spectral Groups → IFFT → output. The groups use random phase, scalar
math, comparisons and Discard; they do not contain per-bin oscillators. The saved
document does not contain Spectrum Prune and its connected discard threshold is
zero. That configuration does not discard zero bins with a `magnitude < threshold`
comparison.

Method: offline 48 kHz / 256-frame stereo processing, synthetic 440/660 Hz input,
Clang `-std=c++17 -O2 -static`. Compare old/runtime/baked variants in alternating
200-block batches; report the median of nine batches. A 256-frame stereo callback
has 5333.33 µs of wall-clock budget. This measures DSP, not driver/UI load.

| Variant | µs / stereo block | Block budget |
|---|---:|---:|
| Original executor | 1371.90 | 25.72% |
| Prepared runtime | 151.82 | 2.85% |
| Generated bin kernel | 132.91 | 2.49% |

Before timing, 150 stereo blocks (76800 samples) were compared against the original
executor; maximum absolute error was zero for both new variants. The large saving
comes from avoiding repeated RTTI/context construction and hoisting bin-independent
work. These numbers do not reproduce or explain every reported 300% live peak.
CPU load, the active unsaved parameters and scheduling can affect that display.
For the third variant the generated SpectralKernel replaces the spectral processor
in the same full graph. This isolates code generation; it does not include the
additional outer BakedGraph boundary wrapper of a separately baked group. Actual
end-to-end Bake load can therefore be slightly higher. Export-wrapper correctness
is covered separately by the native SpectralGroupsTest round-trip tests.

The local comparison fixture and original header snapshot are in
`target/spectral-perf/`; `comparison-final.txt` records the final run. Regression tests are in
SpectralOptimizationTest, SpectralGroupsTest, SpectralUniformTest,
SpectralInstancesTest and SpectralComparatorTest. A reusable current-runtime
benchmark accepts an encoded GRAPH file: `native/audio-host/benchmarks/SpectralBenchmark.cpp`.

## Parallelism experiment

Two independent graph lanes with a persistent worker (no per-callback thread
creation), 5000 callbacks per variant:

| Execution | Median µs | p99 µs | Maximum µs |
|---|---:|---:|---:|
| Sequential | 142.9 | 459.4 | 3876.8 |
| Two threads | 108.8 | 274.3 | 2287.6 |

This exploratory run overlapped native test compilation. It suggests a possible
additional gain, but is not a measurement of a production parallel bin scheduler:
the two graphs have completely separate state. A general implementation needs
bounded worker ownership, dependencies, mono sharing, resource lifetime and voice
state rules, plus deadline tests under contention. It is deliberately not enabled
in the audio callback by that first change. The following implementation instead
splits independent bin ranges with separately prepared processor state.

## Implemented bin worker — follow-up

The actual shared worker was measured on the first bin program from FFT-Noise,
with the same math and external-uniform capture, varying FFT size. Warm-up: 100
frames; 1000 interleaved timed evaluations for each variant. These are per-frame
bin-program times, not complete audio-host/FFT/IFFT callback times.

| FFT points | Serial µs | Parallel µs | Compiled serial µs | Compiled parallel µs |
|---|---:|---:|---:|---:|
| 512 | 22.5 | 23.0 (serial fallback) | 19.9 | 19.6 (serial fallback) |
| 2048 | 84.0 | 52.8 | 72.3 | 46.2 |
| 4096 | 168.6 | 143.8 | 146.0 | 130.7 |
| 8192 | 336.1 | 279.6 | 290.4 | 252.6 |
| 16384 | 697.2 | 466.6 | 611.6 | 414.5 |

The test machine also showed improved p99 times in the parallel cases. These are
offline measurements, not a promise of driver deadlines under arbitrary system
load. Logs and the four-variant fixture: `target/spectral-perf/range-parallel.txt`
and `RangeParallel.cpp`. SpectralOptimizationTest checks exact complex output
equality, shared-worker contention fallback and the small-frame serial path.

Parallel work uses one shared persistent thread, a try-lock on submission, and
independent temporary node state. A submitted borrowed-frame job is always joined
before returning, publishing the frame or retiring the graph. The audio thread
computes its own half while the worker runs; it waits for completion if necessary.
No queue grows and no worker is created from the audio callback. Per-bin audio
voices are excluded. Real-device soak tests under sustained CPU contention and
CPU-specific/adaptive thresholds remain follow-ups.
