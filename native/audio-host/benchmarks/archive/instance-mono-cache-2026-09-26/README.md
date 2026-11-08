# Mono layout and invariant control caching

Fixture: user's saved InstanceGroup_Vergleichstest.knack, 20 sine partials,
440 Hz, falloff 0, maximum 40, unconnected public controls. Source -> Gain ->
Output graph, 256 frames/48 kHz, both channels, live/bake sample equality check.
The user document was not modified.

Compile InstanceGraphAudit.cpp with this directory and native/audio-host/src on
the include path, clang++ -std=c++17 -O3 -ffp-contract=off -static. Run without
arguments and with `reverse`. Raw results are adjacent CSV files.

| Run | Complex us | Live us | Bake us |
| --- | ---: | ---: | ---: |
| forward | 52.871 | 59.9468 | 35.8676 |
| reverse | 48.085 | 57.2376 | 36.1944 |
| repeat | 48.9708 | 57.332 | 36.1792 |

All three source nodes now have stereo_source=0. The Instance engine still uses
cross-channel scheduling to support its internal shared state; its mono result
can nevertheless be reused by downstream processors. Connected external group
boundaries remain conservatively stereo. Intrinsically stereo internal graphs
retain stereo output.

The generated scalar program caches its results against external numeric values,
index/count and sample rate. Unsettled manual values bypass the cache. Pure Math
operations additionally use a shared operand cache across instances, so equal
instance-independent operands reuse results. This does not merge stateful DSP
or assume index-dependent weights are equal. Preparation, state adoption and
fallback invalidate caches. The audio kernel caches its eligibility plan while
the state, active count, connected-input mask and sample rate are stable.

Validation: full native-audio Maven package succeeded, 257 tests, zero failures
and errors, 18 skipped; native script tests passed. A separate opt-in fixture
generation test passed. This offline graph benchmark is not a WASAPI timing
measurement and does not prove the user's live 4%/2% discrepancy is resolved.
