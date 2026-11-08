# Instance/Bake comparison, 2026-09-26

Fixture: the user's InstanceGroup.knack, SHA-256
`AC37E115C5557725CD2FC18900E3AF97D56DE68B8849396F6B525F20CCD69AAA`.
The saved instance count is 20, capacity 40, frequency 330 Hz and falloff 0.999.
Only the selected Instance Group is measured; the original document is unchanged.
The Count boundary is overridden to measure both 20 and 40 active instances.

Both executables use LLVM-MinGW clang++, C++17, `-O3 -ffp-contract=off -static`,
48 kHz and 256-frame blocks. Each timing is the median of nine batches of 300
blocks after 200 warmup blocks. Before timing, 100 blocks of both channels are
compared between live and baked execution (absolute tolerance 1e-5).
No build runs concurrently with the recorded measurements.

The baseline uses the native source snapshot taken before these changes. Its
fixture has the new MULT3 policy metadata removed and the equivalent MULT1/MULT2
header restored, leaving the DSP graph/parameters unchanged. The new executable
uses the same graph with its adaptive policies and the current native sources.

| Active instances | Path | Before (µs/block) | After (µs/block) |
| --- | --- | ---: | ---: |
| 20 | Live | 139.826 | 107.540 |
| 20 | Baked | 558.683 | 115.882 |
| 40 | Live | 311.578 | 235.757 |
| 40 | Baked | 1288.870 | 245.336 |

The 40-instance repeat reverses execution order: live 306.472 → 230.529 µs;
baked 1307.480 → 245.681 µs. This is approximately 24–25% less time live and
81% less time baked. The new bake is still slightly more expensive than live
execution: the instance domain itself has not yet been emitted as a fused kernel.

`deadline_percent` divides time by the 5333.33 µs block budget. It is not the
whole application's device/transport/UI-inclusive DSP meter.

Complex Oscillator is a cost reference with matching frequency/count, but uses
its own falloff law. Its signal is not asserted equivalent to this custom group.
CPU frequency/thermal variation is visible even in that unchanged reference.

Reproduction: run `InstancePerformanceTest` with
`-Deasyknack.test.benchmark=true -Deasyknack.benchmark.document=<path>`; compile
`../InstanceBenchmark.cpp` with `target/instance-performance` and
`native/audio-host/src` as include directories; pass `20` or `40` to the executable.
The old source snapshot is a local diagnostic artifact under
`target/instance-performance-baseline/src`, not bundled with the application.

Other checks: the complete `native-audio` Maven package run passes 251 tests
(17 optional tests skipped), plus the build script's native backend/DSP/transport
test executables. Log: `target/bake-release.log`. A pure Math microbenchmark in
that run measures 408.188 ms scheduled versus 30.061 ms fused. A mixed stateful
reference is approximately neutral (49.820 versus 49.512 ms). These are reference
cases, not a promise of universal speedup from sample fusion.
