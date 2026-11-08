# Actual cached audio host: assembly comparison

Inspected executable (read only):
`C:/Users/rende/.easyknack/plugins/cache/7f9af9917329a10c729011dc95148333680a4860bd75d1b73b6c1224e128c84a/easyknack-audio-host.exe`

Cache timestamp: 2026-09-26 15:22:48.
SHA256: `BF84A111717BAB1E797D029494F4F7A48DD07059B59949957DA457F65315F95A`.
Disassembled with bundled llvm-objdump, demangled, no raw instruction bytes.
This is the latest cached executable, not proof of the executable selected by
a currently running audio session. No DSP or compiler flags were changed.

## Confirmed findings

The generated processor calls CompiledInstanceAudio::process at 0x1400af88e;
on false it calls the generic processInstances at 0x1400af8a2. The specialized
kernel is present in the actual host binary, not only the standalone benchmark.

The specialized sample/partial loop contains inline table interpolation,
weight multiplication and accumulation. No per-node virtual calls remain in
this inner loop. It is a real fused audio loop.

However, its per-instance block preparation still calls:

| Address | Target |
| --- | --- |
| 0x1400b6b6e | BakedGraphProcessor::resolveChannels |
| 0x1400b6b85 | Instance::resolveAutoRate |
| 0x1400b6b8d | Instance::reductionReady |
| 0x1400b6fea | CompiledInstance::renderScalars |

renderScalars still calls InstanceContextNode::process (0x1400b9526) and powf
(0x1400b969c). The falloff power is evaluated again each block, even when all
inputs are unchanged. At 20 active partials and 256 frames/48 kHz, that is 3,750
power evaluations per second in this steady path. Complex caches spectrum
weights and only invokes updateSpectrum when falloff/spread/count changes.

Scalar results are written back via graph/cache pointer chains at 0x1400b96b4
through 0x1400b96d2. The compiler does not eliminate the runtime graph structures.

Two floor calls remain on the nonzero-weight inner path: table phase wrapping
at 0x1400b7362, and phase advance wrapping at 0x1400b7444. Complex also has floor
calls for these operations (separate builtin/custom-wave branches); these are
shared costs, not evidence explaining a Bake-only slowdown. Table sanitation
(finite checks and clamp) likewise survives in both implementations. The
standalone benchmark binary also contains floor calls.

The Bake kernel copies the mono sum to its right-channel cache and output with
memmove (0x1400b75e9, 0x1400b761b). Its inherited channel policy is conservative
stereo; Complex can share mono channels. Consequently downstream nodes can
also execute twice for the group. Copy and graph-preparation costs are absent
from the equivalent direct Complex inner path.

Neither partial loop is a broad SIMD batch of several oscillators. Packed SSE2
instructions occur, e.g. to process two interpolation samples or sum/weight
values; this must not be confused with vectorization across multiple partials.

## Interpretation and next measurement

The compiler has fused/inlined the sample arithmetic but has not cached
unchanged control expressions or removed graph preparation. Remaining
optimizations should target dependency-aware invalidation of control values,
precomputed rate/channel/reduction plans and safe mono propagation. Globally
forcing inlining or enabling fast-math would not solve those structural costs.

Static disassembly does NOT establish which path executed, nor its share of
the observed 4% versus 2% live load. Before attributing that ratio to any one
finding, capture fast-path/fallback counts and separate block preparation from
sample execution in the same host/device configuration. The isolated benchmark
is insufficient evidence for the full live discrepancy.
