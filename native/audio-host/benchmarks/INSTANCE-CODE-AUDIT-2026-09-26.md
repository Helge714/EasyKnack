# Instance Bake code audit

Reference: `C:/Users/rende/Documents/EasyKnack/InstanceGroup_Vergleichstest.knack`.
20 sine partials, 440 Hz, falloff 0; maximum instance count 40. The saved root
connects Complex -> Gain -> Output; the Instance Group has no external input
connections. External audio-rate modulation therefore does not explain this case.

The latest inspected local bake (`LocalBakedGroup6920b561e015.hpp` in cache
`5c5fb96099d48d8409c3302f995d1341c63371d95eae6a4bb702bdf5a3849708`)
already contains CompiledInstance and specialized renderScalars. This is not
evidence of which executable was running during the user's measurements.

## What is actually specialized

Generated renderScalars emits concrete Math/UnitConverter expressions for
frequency and weight. The generated processor still inherits MultipleGraphNode
and calls processInstances<CompiledInstance>. Both live and baked versions use
the same oscillator/reduction sample loop. Thus the current specialization
mainly saves work once per block per instance, not per sample.

Remaining work in MultipleGraphNode includes per-instance channel/rate resolution,
reduction eligibility checks, graph/cache indirections, and duplicate left/right
accumulation of mono samples and normalization weights. Normalization is a final
buffer pass. Complex instead holds contiguous partial arrays, caches spectrum
weights until parameters change, accumulates locally per sample, and writes one
mono output. Instance output is conservatively marked stereo, also propagating
stereo execution to downstream Gain/Output.

## Measurement and limits

InstanceGraphAudit.cpp builds a three-node source -> Gain(-12 dB) -> Output graph
using the generated Fixture.hpp from InstancePerformanceTest. Each run warms up
300 blocks and reports the median of nine batches of 500 stereo blocks, 256 frames
at 48 kHz. Live/baked samples are compared at tolerance 1e-5. Compiled with clang++
-O3 -ffp-contract=off, the same optimization flags used by PluginManager.

| Run | Complex us/block | Live | Bake |
| --- | ---: | ---: | ---: |
| 1 | 50.26 | 60.95 | 61.85 |
| 2 | 50.90 | 65.47 | 57.92 |

This shows a remaining cost difference and no robustly established Bake speedup;
it does not reproduce the user's full 2.4x gap. This is a scheduler benchmark,
not an actual WASAPI endpoint or the complete live app. Timing order is fixed;
CPU frequency/scheduling may affect the comparison. Do not extrapolate these
numbers to the application's displayed peaks. WindowsAudioBackend times the
whole engine.render call, including backend integration. AudioTiming peak is a
running maximum over its lifetime, not a per-window peak.

The separate InstancePathAudit confirms that audio-rate or stereo external
modulation can disable the scalar reduction path, but that is a different case.

## Next implementation target

Specialize the actual instance audio kernel, not just renderScalars: compact
state arrays, invariant channel/rate decisions outside the callback, one mono
accumulator when proven mono, and invariant normalization outside the sample
loop. Preserve general fallback for modulation, stereo, changing Count, and
unsupported graphs. Benchmark the real host and identical device configuration
before claiming the reported regression is fully explained or fixed.

No production DSP or user document was changed during this audit.
