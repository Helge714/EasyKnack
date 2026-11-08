# DSP compilation and fusion

Group source export and Inspector Bake use the same generator. Ordinary nested
groups are flattened first. Compilation currently has three execution paths:

* Pure Math/Mix/Map Range/Unit Converter regions reuse the native `evaluate`
  methods. Constants and repeated expressions can be eliminated. Eligible Math
  operations use SSE2 (four floats). Unsupported SIMD operations use scalar code.
* Mixed-rate pure regions evaluate control values once, then run the remaining
  audio expressions in one loop. Only values consumed outside the region need
  output buffers. Explicit input sampling policies are respected.
* `SampleFusion` composes existing native methods into one loop for linear chains of Math, Mix,
  Map Range, Unit Converter, Clamp, Curve, Oscillator, Filter, Gain, Noise,
  and Value. Concrete, qualified C++ calls remove virtual dispatch;
  Clang receives an inlining hint. No DSP formula is copied into Java. Oscillator
  and Filter use their prepared sample entry points. Other processors reuse
  `process` with a one-sample context and the correct block/sample flags. Math
  and Mix share `processKernel` with normal execution; export supplies fixed
  operation/mode arguments so the compiler can remove those switches. Independent
  branches and fanout terminate a sample region instead of forcing one large loop.

The generated sample path requires prepared kernels, a uniform channel layout,
one voice, and numeric non-event edges. Custom wavetable inputs keep the block
path. Feedback islands, deferred delay commits, resources, MIDI and unsupported
processors retain the scheduler. These are correctness boundaries, not promises
that fusion is universally faster. In particular, block algorithms and SIMD
can outperform a single large sample loop.

The native Oscillator exposes `prepareSample` and `sample`; ordinary processing
and sample feedback share them. A scalar connected frequency is prepared once
per block. Manual parameter smoothing and oscillator state are preserved.

Boundary adapters resolve actual input rates and can share numeric channels when
their left/right source pointers are identical. Instance programs store adaptive
rate policies in MULT3; MULT1/MULT2 remain readable. This prevents wrapping an
Instance Group from promoting its entire control calculation to audio rate.
Instance Combine shares mono computations and its reduction resolves buffers
outside the sample loop. An Instance execution plan now recognizes a mono
Oscillator feeding a single Instance Combine, with control-only numeric ancestors
(Math, Map Range, Clamp, Curve, Unit Converter, Value, Comparator, boundary and
Instance Context). Controls execute once per instance/block, then oscillator
sampling, weight, count fade and shared accumulation run in one loop without
intermediate audio buffers. This native plan is shared by live, baked and exported
groups. It retains each oscillator's state and uses the shared native sample kernel;
it does not generate an independent oscillator formula in Java.

The plan checks resolved rates/channels each block. Audio modulation, stereo,
custom wavetables, multiple reduction outputs, feedback and unsupported processors
fall back to the ordinary scheduler.

`InstanceSourceSpecialization` now removes the outer baked graph for exports whose
entire body is one numeric Instance engine with directly mapped outputs. Public
ports are mapped explicitly (Count need not be first); missing inputs use the
exported editable defaults. Bake and plugin export call the same generator.
There is no BakedInput -> MultipleGraph -> BakedOutput execution layer in this case.

For supported MULT3 Math/Unit Converter/Instance Context control programs the
generator reads fields through the parameter contracts and emits concrete native
`evaluate` calls with constant operations and parameters. Intermediate values are
local floats; only frequency/weight values escaping into the oscillator/reduction
are stored. This concrete instance type is a template argument of the shared
sample/reduction loop, allowing the C++ compiler to inline and optimize its control
expressions. DSP formulas remain in their existing native implementations.
Unsettled inherited manual values use the ordinary scalar path, preserving smoothing.
Unsupported programs can still benefit from removal of the outer graph; unsupported
outer topologies, dynamic choices and wavetable boundaries retain the regular exporter.

This is specialization of a supported instance program, not complete elimination
of all runtime graph metadata/state. General generated fusion across arbitrary
instance programs remains open. Measurements are recorded
under `native/audio-host/benchmarks/archive/instance-specialization-2026-09-26`.

The bake-only `CompiledInstanceAudio` kernel accumulates mono partials directly
from compact phase/increment/weight/table arrays. Its control program caches
results against boundary values, context index/count and sample rate. Guards
retain the scalar path for unsettled manual controls. A shared, per-operation
operand cache avoids repeating identical pure Math evaluations across instances;
index-dependent expressions still have distinct results. This is memoization,
not a blanket assumption that a whole group's controls are instance-independent.
The channel/rate/reduction plan is reused while the boundary layout and active
count stay stable. Fallback, preparation and state handover invalidate the
appropriate caches. None of these caches allocate in the audio callback.

Noise/Digital Noise and downstream Math/Unit Converter frequency modulation no
longer discard the entire specialization. Pure control expressions retain their
cache; modulators use concrete native block calls, and oscillator evaluation and
reduction remain in the compiled sample loop. The live reduction path also accepts
mono audio frequency modulation. Weight must still be Control, and custom wavetable,
stereo or event-consuming paths retain the general scheduler. This is partial
fusion: upstream audio Math/modulators still use intermediate buffers, not one
fully fused loop for the entire graph. Digital Noise's appended Frequency output
is the sanitized base frequency times the current sample's Factor; Factor and
Trigger keep their existing port indices. Old two-output rate metadata remains
readable. Both noise cache entries and frequency modulation preserve independent
generator state; identical seeds alone do not justify reuse.

Live and baked Instance groups without connected external inputs now expose
the internal output's proven mono layout to downstream nodes. Intrinsically
stereo graphs and connected external boundaries remain conservative. Ordinary
processors use the same processing method with separate state per channel;
channel-combining processors explicitly coordinate both channels. A mono source
does not cause its consumers to render a duplicate channel unnecessarily.

Exactly zero weight skips the oscillator table lookup while advancing phase and
manual frequency smoothing. It does not pause the instance or suppress its control
calculation. A Comparator plus Math can explicitly set small weights to zero;
no automatic audibility threshold is inferred from weight alone. Weight is not a
bound on arbitrary input amplitude; weighted normalization and summing many quiet
instances also matter. Generic discard would need state/tail and error-budget rules.

Regression coverage includes mixed rates, unsupported SIMD operations (power),
mono/stereo, fanout, state transfer, short blocks and comparisons with the ordinary
scheduler. `InstancePerformanceTest` generates an opt-in benchmark fixture from
`-Deasyknack.benchmark.document=...`; `InstanceBenchmark.cpp` accepts the active
instance count as its first argument and checks live/baked output equality before
measuring. The benchmark also disables the reduction plan in a reference instance
to compare the scheduler in the same executable. An optional second argument
sets Complex falloff and enables a sample comparison against Complex (330 Hz,
sine, zero spread/phase). Use an equivalent `harmonic^(-falloff)` group with weighted
normalization for this mode. The original experiment used a different falloff law.

Existing exported source packages retain their generated source until exported
again. Re-bake/re-export groups to obtain the new generated loops. Plugin SDK
changes invalidate the native build cache, but do not rewrite hand-edited plugins.
