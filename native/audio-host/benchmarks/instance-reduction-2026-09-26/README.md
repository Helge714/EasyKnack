# Instance reduction, 26 September 2026

Same-executable scheduler / fused runtime / baked / Complex comparison, measured
before the full release build (no competing compiler workload).

Source: `InstanceBenchmark.cpp`, generated with `InstancePerformanceTest` from
`InstanceGroup-falloff.knack`. User document uses harmonic^(-falloff), weighted
normalization, sine, 330 Hz, falloff 0.999. The harness overrides Count to 40.
Native compilation: LLVM MinGW clang++, C++17, -O3 -ffp-contract=off -static.
48 kHz, 256 frames; both group channels, mono Complex. 200 warmup blocks,
9 batches of 300; median batch duration. Percent is block deadline usage,
not the full application's DSP meter.

| Case | microseconds/block | deadline % |
| --- | ---: | ---: |
| Complex | 99.213 | 1.86024 |
| Instance, scheduler forced | 226.048 | 4.23839 |
| Instance, fused reduction | 109.781 | 2.05840 |
| Baked Instance | 114.757 | 2.15169 |

The fused path is about 51% faster than this scheduler reference; about 11% more
time than Complex. Bake adds about 5 microseconds in this run. This is one
supported topology, not a guarantee for arbitrary Instance Groups.

Before measuring, 100 blocks compare fused / scheduled / baked samples within
1e-5 and Complex within 1e-4. `InstanceReductionTest` additionally covers varying
frequency, Count fades, zero Count, zero weight and return, Sum/Mean/Weighted,
audio-rate and stereo fallback, and oscillator phase continuity.

Original input SHA256:
`3AA8CE1937B5D197135E48B472F1B5777062A475300311E59083C5BEC49E6BBE`

Adapted document SHA256:
`18FFCE4D9DD37A0FD4125E8705A2677E691D11DC84728B44958A66DD704C0714`

The original is backed up next to the user's document as
`InstanceGroup.before-falloff-20260926.knack`. Root wiring, node identities and
layout are retained; falloff range is 0–4, matching Complex. Stale saved bake
activation is removed so the edited group can be baked again.

Reproduce fixture: opt-in `InstancePerformanceTest` with
`-Deasyknack.test.benchmark=true -Deasyknack.benchmark.document=<adapted document>`.
Compile the benchmark with `-I target/instance-performance -I native/audio-host/src`.
Run `benchmark.exe 40 0.999` (second argument enables the Complex signal check).

Validation: full `-Pnative-audio package` succeeded after these changes, including
native backend/DSP tests and Maven tests. Log: `target/instance-reduction-release.log`.
The opt-in document conversion additionally passed separately before replacing
the user file.
