# Specialized Instance Bake and export, 26 September 2026

This extends the previous `instance-reduction-2026-09-26` experiment. Latest
saved user document (SHA256
`8B20A41D4B99D204E4D5A9BF09C2803E175E330A4A2266EDCA203EF57D3C7888`),
40 sine partials, 330 Hz, harmonic^(-0.999), weighted normalization.

`InstanceSourceSpecialization` emits the same native code for Bake and export:
no outer BakedGraphProcessor, explicit public-to-internal port mapping, and
concrete Math/Unit/Context scalar expressions. A concrete instance type instantiates
the shared reduction loop. Live execution retains its generic scalar plan.

48 kHz / 256 frames, both group channels and mono Complex, clang++ C++17
`-O3 -ffp-contract=off -static`. 200 warmup blocks, nine batches of 300 blocks,
median batch time. No concurrent builds while timing. Cases compare samples
before timing: scheduled/live/baked <=1e-5; Complex <=1e-4.

| Run | Live µs | Bake µs | Bake reduction |
| --- | ---: | ---: | ---: |
| 40.csv | 116.664 | 108.923 | 6.6% |
| 40-repeat.csv | 115.505 | 111.652 | 3.3% |
| 40-reverse.csv | 121.435 | 109.707 | 9.7% |

The last run reverses case order. Thermal/frequency variation remains visible;
do not interpret these numbers as a guaranteed gain for other graphs.
Full readings including Complex and the forced scheduler are in the CSV files.

The isolated group snapshot is retained as `reference.knack`; the user document
was only read during this change. Reproduce fixture with opt-in
`InstancePerformanceTest` and `-Deasyknack.test.benchmark=true
-Deasyknack.benchmark.document=<reference.knack>`.
Compile `InstanceBenchmark.cpp` with `-I target/instance-performance
-I native/audio-host/src`; run `benchmark.exe 40 0.999 [reverse]`.

`InstanceSourceSpecializationTest` compares generated source for Bake and export,
and native output against live with reordered public inputs, Count fades, partial
blocks, audio modulation, stereo fallback, inherited smoothing, live defaults and
state transfer. `InstanceReductionTest` and the existing Multiple Groups suite
cover reduction behavior and shared/multiple-output fallbacks.

Full release validation: `-Pnative-audio package` succeeded; 255 Maven tests,
0 failures/errors, 18 skipped, plus all native build-script tests. Log:
`target/instance-specialization-release.log` (26 September 2026, 14:16).
