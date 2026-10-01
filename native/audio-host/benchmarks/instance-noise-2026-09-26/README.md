# Instance frequency modulation audit

Fixture generated from the saved `InstanceGroup_Vergleichstest.knack` on
2026-09-26 (file modification time 17:36:10). The document was not edited.
20 active instances, scalar harmonic/falloff controls, Digital Noise Factor
multiplied with partial frequency, sine oscillator, weighted Instance Combine.
The generated defaults are recorded in Fixture.hpp (including Noise strength 1).

LLVM MinGW clang++, `-std=c++17 -O3 -ffp-contract=off -static`, 48 kHz, 256 frames.
The harness checks live/baked sample equality for 320 blocks before timing, then
warms 300 blocks and reports the median of nine 500-block batches. Both variants
were proven mono. The ordinary application host build uses -O2; these measurements
are a controlled comparison, not a prediction of the application's CPU meter.

| Path | Before (µs/block) | After (µs/block) |
| --- | ---: | ---: |
| Live | 168.352 | 114.354 |
| Bake | 180.192 | 88.4308 |

Before: adding Digital Noise disabled both the reduction plan and the generated
control specialization. After: mono audio modulators retain direct oscillator
reduction; generated code caches the independent control branch and invokes
concrete modulator processors. Intermediate upstream audio buffers remain.

The before directory preserves generated source/parameters, not a copy of the
entire old runtime. Rebuilding it against the current runtime will include the
new live optimizations, so it does not reproduce the old baseline timings.
The after directory can be rebuilt against `native/audio-host/src` with its own
directory first on the include path. No Complex comparison is claimed: this
graph includes noise frequency modulation that Complex does not implement.

Automated regressions additionally compare against the general scheduler with
reduction disabled, including nonzero noise strength, count fades, short blocks,
Control/Audio transitions, stereo, state handover and the new Frequency output.
