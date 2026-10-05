> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Formula groups

Create a **New Formula Group** in the Math category and open it like an ordinary
node group. Its content is a code editor; Group-General, Group-IO and editor
 templates remain available in the Inspector.

## Interface and language

Group-IO defines numeric or Trigger inputs and outputs. Each port has a stable
identity and a separate code identifier. Changing the identifier refactors the
accepted source and draft. Display labels may contain spaces. Slider units define
the numeric base unit seen by the formula; ordinary group conversions still apply.
Combobox templates provide their numeric values, or entry indices for string values.

The initial group has `signal` and `result`:

```c
float amplified = signal * 2;
if (amplified > 1) {
    result = 1;
} else {
    result = amplified;
}
```

Trigger ports are booleans: `if (clock) { fire = true; }`. Every true output emits
a trigger at that sample. Trigger outputs reset to false before each evaluation;
numeric outputs must be assigned on every execution path.

Supported: typed numeric/bool locals, if/else, arithmetic, comparisons, short-circuit boolean
operators, and abs, sqrt, trigonometry, sineWave, exp, log/log2/log10, floor/ceil/round, pow,
min/max, clamp, mix and sign. `pi` and `tau` are constants. Inputs are read-only.
There are no loops, pointers or arbitrary C++ calls. Persistent variables use explicit state declarations (see below).
Limits: 16 inputs, 8 outputs; bounded source size, nesting and generated code.

## Execution and editing

Group-General selects Auto, Audio or Control. Auto considers only used inputs;
Control inputs stay scalar even in an Audio kernel. Used Trigger inputs or Trigger
outputs require sample precision. Explicit Control rejects used Audio/Trigger inputs.

Live applies valid edits after a short pause. Apply works with Live disabled.

Editor completion respects lexical scopes and declaration order, including branches
without braces. Hover shows local and persistent variable types, input/output types,
context types and function signatures. Buffer and Trigger members are resolved using
their receiver, so an ordinary local named `size` is not described as Buffer metadata.
Function completion inserts parentheses with the caret inside (or preserves an
existing opening parenthesis). Typing `(` or `,` requests argument help with the
active parameter, including nested calls and the optional seed of rand. Comments
do not produce hover/completion suggestions. The scope index tolerates incomplete
code; compiler diagnostics remain authoritative for validity and definite assignment.
A bounded interpreter runs the checked program immediately. An isolated native
kernel is compiled in the background and swapped at an audio block boundary.
Only one native build runs at a time. Cached kernels avoid recompilation; the
status line shows preview/build/native state. Invalid drafts retain the last valid
program and show diagnostics. Deliberate discontinuous formula changes can click;
there is no automatic crossfade.

Save/load includes source, draft, accepted port schema and language version. Undo
restores accepted changes. Native libraries are a local cache, not document data.
Bake and plugin export embed generated C++ expressions and portable source.

## Validation

Tests cover persistence, interface edits and identifiers, Inspector/editor navigation,
undo, compiler diagnostics, native/preview parity, sample-accurate triggers including
feedback chunks, DLL loading, engine hot swap and compiled plugin export. Native
tests use `-Deasyknack.test.audio=true` and the configured C++ compiler.

## Context, random values and Buffer inputs

The read-only numbers `sampleRate` (Hz), `blockSize` (samples), `sampleIndex`
(zero-based offset in the current block) and `samplePosition` (samples processed
by this Formula instance) are available everywhere. Block size/index remain relative
to the full audio block inside feedback chunks. Position survives live code swaps;
Control execution advances it by the full block size. It is not the global transport
position and does not advance while the node is not rendered. In typed formulas,
sampleRate is double; blockSize, sampleIndex and samplePosition are ulong. Legacy
version 1/2 formulas without typed syntax still expose these as 32-bit floats.

`rand(min, max)` returns a uniform deterministic pseudorandom value with a stable
per-node seed. `rand(min, max, seed)` selects a numeric seed explicitly, which can be
an ordinary input. Each call site has a separate sequence; execution order, block
partitioning and native compilation do not change it. Equal explicit seeds and code
produce equal sequences. Reversed bounds are exchanged, nonfinite bounds produce 0.
Auto uses Audio for random, sample index/position and Buffer reads; explicit Control
samples them once per block. `sampleRate`, `blockSize` and Buffer size alone remain
Control in Auto mode.

Add an input of type **Buffer** in Group-IO, name its code identifier `buffer`, and
connect a File Buffer or live Buffer. Buffer outputs and arbitrary audio-input history
are not supported. Resources are borrowed, not copied or allocated by the Formula.

```c
// Loop through an immutable file buffer, with linear interpolation.
result = buffer.read(samplePosition, true);
```

- `buffer.size`: capacity in samples, zero if disconnected.
- `buffer.read(index)`: sample index, clamp at the ends, linear interpolation.
- `buffer.read(index, true)`: wrap positive/negative indices.
- `buffer.read(index, true, false)`: wrap, floor the index, no interpolation.

A disconnected/empty Buffer or nonfinite index returns zero. Legacy Boolean reads use the same physical ring positions and sample-accurate
write-head visibility as the
Buffer Readers: future writes from the current block are not exposed. Clamp holds
valid edge samples; wrap can read unwritten slots as silence. This is a Buffer
resource API, not unrestricted access to an incoming audio block.

## Typed input API (language version 2)

New groups use absolute sample positions for `buffer.read(position)`, equivalent to
`buffer.read(position, Clamp, Linear)`. Existing saved groups retain version 1 and
its physical ring-index default. Explicit named options always select the new
absolute timeline, including inside an old group. Legacy Boolean arguments remain
supported with their original physical-index semantics; `false` interpolation there
still floors the index. Saving or editing an old group does not silently migrate it.

Buffer input properties:

- `size`: allocated capacity in samples.
- `begin`: oldest currently available absolute sample position.
- `end`: exclusive end of currently available samples.
- `writePosition`: next write position, currently equal to `end`.
- `sampleRate`: the Buffer's sample rate in Hz.

For a file, begin is zero and end/writePosition remain at the file length. For a
live ring, begin/end move along the absolute timeline. These properties and reads
respect the current sample's write-head visibility, also in feedback chunks.
Empty/disconnected buffers return zero. The legacy float language's long-position precision
limit still applies.

```c
double delaySamples = sampleRate * 0.01;
result = buffer.read(buffer.end - 1 - delaySamples, Clamp, Linear);
```

`Wrap` wraps within the currently available `[begin,end)` span, `Clamp` holds its
edge samples, and `Zero` treats unavailable samples as zero. Linear interpolation
uses two samples with the chosen boundary rule (including across the wrap seam).
`Nearest` selects the nearest sample, with half-sample ties rounded upward.

Numeric inputs expose `.value` (equivalent to the bare input) and `.rate` (compare
with `Audio` or `Control`). Trigger inputs expose `.active`, equivalent to the bare
boolean input. Member completion offers only properties appropriate for the input.

```c
result = signal.value;
if (clock.active && signal.rate == Audio) {
    result = 0;
}
```

Normal numeric inputs still do not expose arbitrary `.read()` or history. Their
block storage is not a persistent Buffer resource. Use an explicit Buffer node for
indexed access; bounded signal history remains a future extension.

## Persistent state and stable oscillators

Declare state before all ordinary statements, at the top level:

```c
state double phase = 0;
result = sin(tau * phase);
phase = phase + frequency / sampleRate;
phase = phase - floor(phase);
```

Add a numeric input named `frequency` (Hz). Auto runs a stateful formula at Audio
rate. Explicit Control updates once per block; it does not automatically scale
increments by blockSize. Ordinary local variables remain temporary.

State supports bool, int, uint, long, ulong, float and double, up to 64 variables. Initializers must be finite
numeric literals (optionally signed) or true/false, not expressions or inputs.
Initialization occurs when a new processor instance is created, not every sample.
The host owns separate state banks for independent processors, stereo channels and
MIDI voices. Mono sharing follows the existing graph channel rules.

Live preview, native compilation, feedback evaluation and export use the same state
bank semantics. On graph/code replacement, matching state names, types and initial
values retain their current values, independent of declaration order. A changed
initializer/type/name starts that variable anew; removed variables are discarded.
This handover allocates no memory on the audio thread. No C++ static/global state
is generated. A new audio-host session initializes all states. Saving a document
saves declarations/initial values, not a snapshot of running DSP state.

To reset explicitly, add a Trigger input named `reset`:

```c
state double phase = 0;
if (reset) phase = 0;
result = sin(tau * phase);
phase = phase + frequency / sampleRate;
phase = phase - floor(phase);
```

A bounded phase avoids the growing floating-point errors of sin(frequency * absolute
time). Changing frequency changes the increment without replacing the current phase.
Abrupt phase resets or arbitrary output-code changes can still be discontinuous.

Numeric locals, state variables and already assigned outputs support `+=`, `-=`,
`*=` and `/=`. For example, `phase += frequency / sampleRate;` is equivalent to
`phase = phase + frequency / sampleRate;` in both live preview and compiled code.
Inputs remain read-only; declarations require `=` and boolean variables do not
support these arithmetic assignments.

## Numeric types and casts (language version 3)

New Formula groups use fixed-width types: bool, int (signed 32-bit), uint (unsigned
32-bit), long (signed 64-bit), ulong (unsigned 64-bit), float (IEEE 32-bit), double
(IEEE 64-bit). `unsigned int` and `unsigned long` are aliases for uint and ulong.
Persistent `state` supports all seven types and retains the complete bit pattern.
Audio ports remain float; output assignment converts numeric results automatically.

```c
ulong position = samplePosition;
double seconds = (double) position / sampleRate;
result = (float) sin(tau * 440.0 * seconds);
```

Use `(type) expression` for explicit casts. Integer literals use int, long or ulong
according to their range; decimal/exponent literals are double. `1.5f` is float,
`1u` is uint, `1L` is long, `1UL` is ulong. pi and tau are double. A declared state
initializer is parsed directly in its declared type and must be a finite literal.

Mixed arithmetic prefers double, then float, then the wider integer of the same
signedness. Bool contributes 0 or 1; bool + bool returns int. Bool also combines
with unsigned values without introducing a signed operand. Other signed/unsigned
integer mixtures require a cast, including comparisons. Integral division truncates
toward zero. Assignment to a narrower local/state type requires an explicit cast,
including compound assignment. Conditions require bool; use a comparison or a bool
cast for numeric conditions. Arithmetic results cannot implicitly assign to bool.

All integer overflow wraps at the type's width, including signed overflow. Integer
division/remainder by zero returns zero; minimum signed integer / -1 returns that
minimum, with remainder zero. Floating point division follows IEEE semantics.
Float/double to integer truncates toward zero and saturates at the destination
bounds; NaN converts to zero. Integer casts retain the low bits when narrowing;
signed widening sign-extends. These rules are shared by preview, DLLs and export.

Math functions retain float/double precision. Transcendental functions given only
integers compute in double; min/max/clamp/abs/sign can retain integer types. rand
and Buffer samples/metadata retain their existing float API. Buffer read positions
accept double, but widening float Buffer metadata does not recover lost precision.

Saved version 1/2 formulas keep their previous numeric semantics unless they use
new typed syntax (int/uint/long/ulong/double/unsigned or an explicit cast). Such
syntax selects the typed compiler, while their original Buffer read convention
remains intact. New version 3 groups use typed semantics throughout. Existing
native DLL caches are versioned by ABI; old bytecode remains readable.

Prefer bounded `state double phase` for oscillators with changing frequencies.
Double absolute-time formulas avoid the old float artifacts, but changing frequency
in `sin(tau * frequency * seconds)` still changes phase discontinuously.

## Wavetable sine

`sineWave(phase)` accepts phase in **cycles**, not radians: 0.25 is the positive
peak, 0.75 the negative peak, and 1 is a complete period. Negative and large finite
phases wrap automatically. NaN/infinity returns zero. The result is float, using
the same 2048-point sine table as the oscillators with linear interpolation.
The typed compiler retains double precision for the phase; legacy formulas pass
their existing float phase. The host prepares and shares the table on its control
thread, with no allocation or table initialization during audio processing.

```c
double seconds = (double) samplePosition / sampleRate;
result = sineWave(frequency * seconds);
```

For continuously changing frequency, retain the phase explicitly:

```c
state double phase = 0;
result = sineWave(phase);
phase += frequency / sampleRate;
phase -= floor(phase);
```

`sin(angle)` remains the standard mathematical function in radians. sineWave is
an audio approximation, not a replacement for double-precision trigonometry.
Actual speed depends on the graph and execution mode; it does not repair phase
precision already lost before lookup. Preview, native DLLs, feedback and exported
groups use the same table lookup. ABI 6 supplies the borrowed table to native code.
