# Formula language reference

The Formula language is a bounded, C++-like language for numeric signal processing, not a full C++ or Java runtime. This reference describes the language; configuring ports, rates and the editor is covered by the [Formula Group node](../../nodes/en/formula-group.md) and [workflow](formula-group.md).

[Variables](#variables) · [state](#state) · [final](#final) · [const](#const) · [Types](#types) · [Expressions](#expressions) · [Context](#context) · [Buffers](#buffers) · [Functions and loops](#user-functions-and-bounded-loops) · [Built-in functions](#built-in-function-reference)

## Evaluation

Statements execute in source order. Audio rate evaluates once per sample; Control once per block. Auto selects the permitted rate from the used inputs. In Spectral Groups the enclosing group provides bin/frame evaluation; sample timing must not be treated as a bin index.

Use the configured code identifiers to read input ports and assign outputs. Numeric inputs are float, Trigger inputs bool, and Buffer inputs resources. Inputs are read-only. Assign every numeric output on every path; a Trigger output defaults to false each evaluation and emits an event when assigned true.

```cpp
final float gain = signal * 2;
result = clamp(gain, -1, 1);
```

## Variables

Every declaration needs a type, name and initializer, followed by a semicolon. Names use ASCII letters (A–Z, a–z), digits and underscores, cannot start with a digit and are case-sensitive. Keywords, built-in functions and context names are reserved. A name must be declared before use. A local in a block is only available in that block; an already visible variable cannot be redeclared there. Function parameters have their own scope.

| Declaration | Lifetime | Can be reassigned? | Initializer |
| --- | --- | --- | --- |
| `float x = signal;` | Current evaluation and enclosing scope | Yes | Runtime expression |
| `final float x = signal;` | Current evaluation and enclosing scope | No | Runtime expression |
| `const float X = 2;` | Constant value wherever the declaration is in scope | No | Input-independent constant expression |
| `state float x = 0;` | Retained between evaluations of this processor | Yes | Finite literal in the type's range |

### state

`state` remembers a value between evaluations. Its initializer supplies the initial value; it is not reapplied every sample. Updates become the starting value for the next evaluation. State is independent per channel, voice and instance, rather than shared globally.

Declare all state variables at the beginning, before executable statements, local/final/const declarations or branches. State is not allowed inside functions or blocks. At most 64 state variables are supported. The initializer must be a literal, for example `0`, `0.5` or `false`; an input, `pi`, another variable or a calculation is not a valid state initializer.

```cpp
state double phase = 0;
phase += 440.0 / sampleRate;
phase -= floor(phase);
result = sin(tau * phase);
```

Here phase advances from its previous value. Replacing the first line with `double phase = 0;` would reset phase on each evaluation, preventing accumulation.

During live edits, state with matching name, type and initial value is retained. Changing one of these gives that variable its new initial state. Documents save source and initial values, not running DSP state; opening a document starts from those initial values. Do not use state to store a value that should simply follow an input each sample.

### final

`final` allows one initialization and forbids subsequent assignments, including `+=`, `-=`, `*=` and `/=`. It does not retain a value between evaluations. Audio recalculates the initializer per sample; Control per block. Inside a function or loop it is initialized each time execution reaches that declaration.

```cpp
final float gain = signal * 2;
result = gain;
// gain = 1;   // invalid: gain is final
```

Function parameters can also be final: `float square(final float x) { return x * x; }`. The argument may change between calls; the function cannot reassign x. Without final, parameters can be reassigned locally without modifying the caller's value.

### const

`const` defines a value independent of the current inputs and runtime context. Use literals, `pi`, `tau`, earlier const variables, arithmetic, casts and pure built-in math functions. Uppercase names are a convention, not a requirement.

```cpp
const float MIN_SPEED = 0.5;
const float MAX_SPEED = MIN_SPEED * 4;
final float speed = MIN_SPEED + signal * (MAX_SPEED - MIN_SPEED);
result = speed;
```

The two bounds keep their values; speed can change every evaluation. A const initializer cannot use input ports, context (including sampleRate), state, mutable locals, final locals, user functions, `rand` or `sineWave`. For example, `const float X = signal;` is invalid even if the input currently happens to be unchanged. Use final for such a value. Like other locals, const variables are not captured by user functions; pass them as arguments or declare them inside the function.

Const parameters are not supported; use final parameters. `const` already implies read-only, so `const final` is rejected. Neither const nor final may be combined with state. These qualifiers describe allowed dependencies and assignments, not a guarantee that processing becomes faster or that every initializer is evaluated only once internally.

### Editor emphasis

State names are italic. Final/const names, input names and read-only context variables are bold. Final and const use the same semantic variable color. This visual emphasis does not change the source text or the meaning of a variable.

## Types

| Type | Meaning |
| --- | --- |
| `bool` | true/false; contributes 1/0 in typed arithmetic |
| `int` | Signed 32-bit integer |
| `uint`, `unsigned int` | Unsigned 32-bit integer |
| `long` | Signed 64-bit integer |
| `ulong`, `unsigned long` | Unsigned 64-bit integer |
| `float` | 32-bit floating point, about 7 significant decimal digits |
| `double` | 64-bit floating point, about 16 significant decimal digits |

Decimal literals without a suffix use double in typed formulas; `0.5f` explicitly uses float. Integer suffixes include `u`, `l` and `ul`. Write casts as `(double) value` or `(int) value`. Assignments and arguments can implicitly convert double to float. Float-to-integer and narrowing integer assignments require explicit casts; mixing signed and unsigned integers also requires a cast. Bool conditions need a comparison or explicit `(bool)` cast.

```cpp
int whole = 5 / 2;                 // 2: integer division
float fraction = 5.0 / 2;         // 2.5
int truncated = (int) fraction;   // 2
result = whole + fraction + truncated;
```

Older float-only formulas retain their compatibility behavior. Integer/double declarations, casts, functions, loops, final and const select typed execution. Buffer resources are input ports, not a type for local numeric variables or function parameters.

## Expressions

From highest to lowest precedence: parentheses/casts; unary `+`, `-`, `!`; `*`, `/`, `%`; `+`, `-`; comparisons `<`, `<=`, `>`, `>=`; equality `==`, `!=`; `&&`; `||`. Operators at the same binary precedence associate left to right. Logical operators short-circuit. Use parentheses to make intent explicit. `=` assigns, `==` compares.

Statements end in `;`. Assignments support `=`, `+=`, `-=`, `*=`, `/=`. Comments start with `//` and run to the end of the line. Braces group statements. A condition must be bool; braces are optional around a single statement:

```cpp
if (signal > 0) {
    result = signal;
} else {
    result = 0;
}
```

There are no arrays, pointers, strings, ternary expressions, arbitrary C++ calls, recursion or while loops. Loop-only `++`/`--` forms are described below; they are not general expression operators.

## Context

Context values are read-only and cannot initialize const variables.

| Name | Typed value | Meaning |
| --- | --- | --- |
| `sampleRate` | double | Processing sample rate in Hz |
| `blockSize` | ulong | Full audio block length in samples, including feedback processing |
| `sampleIndex` | ulong | Zero-based sample offset within the current audio block |
| `samplePosition` | ulong | Samples processed by this node; retained across live edits; advances by a full block at Control rate |

Elapsed seconds: `samplePosition / sampleRate`. `sin`/`cos` use radians, whereas `sineWave` uses cycles. `pi` and `tau` are mathematical constants (double in typed formulas). `Audio` = 1 and `Control` = 0 identify rates; Clamp/Wrap/Zero and Linear/Nearest are Buffer read options, not independent numeric variables.

Numeric inputs expose `.value` and `.rate` (Audio = 1, Control = 0); Trigger inputs expose `.active`. The bare input name already gives its current value. An audio input has no arbitrary past-sample access; use a Buffer resource for that.

## Buffers

For a configured Buffer input named buffer:

| Member | Meaning |
| --- | --- |
| `.size` | Capacity in samples |
| `.begin` | Oldest available absolute sample position |
| `.end` | Position after the newest available sample |
| `.writePosition`, `.writepos` | Absolute next-write sample position; fixed at end for file buffers |
| `.sampleRate` | Buffer sample rate in Hz |
| `.read(...)` | Read a sample; see [buffer-read](#buffer-read) |

Metadata and read samples are float. Positions are absolute sample positions, unlike the Buffer node's normalized 0–1 position output. Resources are read-only and supported as inputs only.

### buffer.read

`buffer.read(position[, mode[, interpolation]])` returns a float sample at an absolute sample position. Modes: Clamp (default), Wrap, Zero; interpolation: Linear (default), Nearest. Empty buffers return zero. Legacy version-1 boolean arguments retain physical-index semantics.

```cpp
result = buffer.read(buffer.end - 1, Clamp, Linear);
```

This reads the newest available sample. Empty buffers return zero.

## User functions and bounded loops

Define numeric/bool functions with typed parameters and a return type, before or after their calls. Every execution path must return a value. Functions have local variables and parameters, can read input ports/context and call other functions, but cannot write output ports, capture caller locals/state, declare persistent state or recurse. At most 16 parameters are allowed. Use the return value to assign an output.

```cpp
float shape(float x, float amount) {
    return tanh(x * amount);
}

float sum = 0;
for (int i = 0; i < 4; i++) {
    sum += shape(signal, i + 1);
}
result = sum / 4.0;
```

`for` requires an `int` counter with a new name, literal integer start/bound/step, `<`, `<=`, `>` or `>=`, and `++`, `--`, `+=` or `-=`. The step must approach the bound; the counter is read-only inside the body. Each loop allows at most 1,024 iterations. Loops and calls are expanded during compilation; nested expansion remains subject to the overall instruction/register/source limits. There is no `break` or `continue`. A zero-iteration loop does not assign its outputs. Braces around if/else and loop bodies are optional.

## Built-in function reference

In typed formulas, math functions use promoted argument types; integer arguments use double except abs, sign, min, max and clamp, which preserve promoted integer types. sineWave, rand and Buffer reads return float. No automatic oversampling is applied.

| Area | Functions |
| --- | --- |
| Magnitude and limits | [abs](#abs), [sign](#sign), [min](#min), [max](#max), [clamp](#clamp) |
| Powers and logarithms | [sqrt](#sqrt), [root](#root), [pow](#pow), [exp](#exp), [expm1](#expm1), [log](#log), [log2](#log2), [log10](#log10), [log1p](#log1p) |
| Trigonometry | [sin](#sin), [cos](#cos), [tan](#tan), [asin](#asin), [acos](#acos), [atan](#atan), [atan2](#atan2) |
| Hyperbolic functions | [sinh](#sinh), [cosh](#cosh), [tanh](#tanh), [asinh](#asinh), [acosh](#acosh), [atanh](#atanh) |
| Rounding and interpolation | [floor](#floor), [ceil](#ceil), [round](#round), [mix](#mix) |
| Signal and random values | [sineWave](#sinewave), [rand](#rand) |

### abs

`abs(value)` returns the magnitude. The minimum signed integer retains its value because the positive counterpart is out of range.

### sign

`sign(value)` returns -1, 0 or 1 according to the sign.

### sqrt

`sqrt(value)` returns the square root; negative input produces NaN.

### root

`root(value, degree)` returns the degree-th real root: `root(16, 2)` = 4 and `root(-8, 3)` = -2. Degree must be finite and nonzero. Negative values require an odd integer degree; an even or noninteger degree gives NaN. Positive values also support noninteger degrees.

A negative degree gives the reciprocal root: `root(16, -2)` = 0.25. Zero with a positive degree gives zero, with a negative degree positive infinity. Invalid degrees or NaN input give NaN. Use `sqrt(value)` for a square root, or `pow(value, exponent)` for a power.

### sin

`sin(angle)` returns the sine of an angle in radians. Example: `sin(pi / 2)` = 1.

### cos

`cos(angle)` returns the cosine of an angle in radians. Example: `cos(0)` = 1.

### tan

`tan(angle)` returns the tangent in radians; avoid the poles at pi/2 plus multiples of pi.

### asin

`asin(value)` returns the inverse sine in radians; input must be between -1 and 1.

### acos

`acos(value)` returns the inverse cosine in radians; input must be between -1 and 1.

### atan

`atan(value)` returns the inverse tangent in radians.

### atan2

`atan2(y, x)` returns the direction of (x, y) in radians, retaining the quadrant.

### sinh

`sinh(value)` returns the hyperbolic sine; large magnitudes can overflow.

### cosh

`cosh(value)` returns the hyperbolic cosine, at least 1 for real input; large magnitudes can overflow.

### tanh

`tanh(value)` smoothly saturates toward -1 and 1; gain compensation and oversampling are not automatic.

### asinh

`asinh(value)` returns the inverse hyperbolic sine for any real input.

### acosh

`acosh(value)` returns the inverse hyperbolic cosine; input below 1 produces NaN.

### atanh

`atanh(value)` returns the inverse hyperbolic tangent; finite for -1 < value < 1, infinite at ±1, NaN outside.

### exp

`exp(value)` returns e raised to value; large positive input can overflow.

### expm1

`expm1(value)` computes exp(value) - 1 accurately near zero.

### log

`log(value)` returns the natural logarithm. Positive input gives finite results; zero gives negative infinity, negative input NaN.

`log(value, base)` returns the logarithm to a chosen base: `log(8, 2)` = 3. value must be positive and base finite, positive and unequal to 1; otherwise the result is NaN. Bases between 0 and 1 are supported.

### log2

`log2(value)` returns the base-2 logarithm; input must be positive for a finite result.

### log10

`log10(value)` returns the base-10 logarithm; input must be positive for a finite result.

### log1p

`log1p(value)` computes log(1 + value) accurately near zero; -1 gives negative infinity, values below -1 give NaN.

### floor

`floor(value)` rounds down toward negative infinity; `floor(-0.2)` = -1.

### ceil

`ceil(value)` rounds up toward positive infinity; `ceil(-0.2)` = 0.

### round

`round(value)` rounds to the nearest integer value, halfway away from zero. The result remains floating point.

### pow

`pow(base, exponent)` raises base to exponent; unsupported real domains produce NaN.

### min

`min(a, b)` returns the smaller value.

### max

`max(a, b)` returns the larger value.

### clamp

`clamp(value, minimum, maximum)` limits value to the supplied range; minimum must not exceed maximum.

### mix

`mix(a, b, factor)` computes a + (b - a) * factor. The factor is not clamped; 0 gives a, 1 gives b.

### sineWave

`sineWave(phase)` returns float sine using a shared 2048-point table and linear interpolation. Phase is in cycles, wraps automatically and gives zero for nonfinite input; `sineWave(0.25)` = 1.

### rand

`rand(minimum, maximum[, seed])` returns a reproducible uniform float random value per evaluation. The optional seed selects the sequence; otherwise the node seed is used. Reversed bounds are exchanged.

[Formula Group](../../nodes/en/formula-group.md) · [Manual](index.md) · [Deutsch](../de/formula-language.md)
