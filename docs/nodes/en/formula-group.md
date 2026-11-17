# Formula Group

## Overview

Computes custom formulas through a configurable group interface.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| signal | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Source | [Editor](../../user/en/reference-tables.md) | Formula editor | — | result = signal; | — | — | Inspector |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| Live preview | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Inspector |
| Group IO | [Editor](../../user/en/reference-tables.md) | Numeric, Trigger, Buffer ports | — | signal → result | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| result | [Value](../../user/en/data-types.md#value) | ±∞ | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

For a Buffer input, choose **Buffer resource** as the Group-IO data type. It was previously displayed as **HISTORY**. Buffer resources are supported as inputs only.

Group-IO defines port labels, units and stable code identifiers. The initial interface is signal → result. The language supports typed local/state variables, arithmetic, if/else, casts and math functions; Trigger inputs are boolean at their event sample. Audio executes per sample; Control per block, subject to validation against loss of used Audio/Trigger inputs. State is independent per channel/voice/instance. Input-specific read methods distinguish current signal blocks from resource buffers. Live preview applies valid edits before native compilation completes; invalid drafts keep the last accepted program. In Spectral Groups, valid formulas execute per bin/frame; temporal context is not interchangeable with bin context. See [the Formula workflow](../../user/en/formula-group.md).

Hyperbolic functions: `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`.
`tanh` supplies smooth saturation; gain compensation remains part of the formula.
`expm1` and `log1p` preserve accuracy near zero. Function help describes domains
and nonfinite results; no oversampling is applied automatically.


Opening Formula while background preparation is still running shows a loading message; the editor appears automatically when ready.

### Language

- Types: `bool`, `int`, `uint` (`unsigned int`), `long`, `ulong` (`unsigned long`), `float`, `double`; casts such as `(double) value`.
- Statements: local declarations, `state` declarations before other statements, assignments, `+=`, `-=`, `*=`, `/=`, `if` / `else`, blocks `{ }`, comments `//`.
- Operators: `+`, `-`, `*`, `/`, `%`, unary `+` / `-` / `!`, `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`; parentheses control precedence. No pointers, recursion, while loops or arbitrary C++ calls.
- Constants: `true`, `false`, `pi`, `tau`, `Audio`, `Control`, `Clamp`, `Wrap`, `Zero`, `Linear`, `Nearest`.
- Context: `sampleRate` (Hz), `blockSize` (samples), `sampleIndex` (within the block), `samplePosition` (absolute sample count). Elapsed seconds: `samplePosition / sampleRate`.
- Numeric inputs: `.value`, `.rate`; Trigger inputs: `.active`.
- Buffer inputs: `.size`, `.begin`, `.end`, `.writePosition`, `.writepos`, `.sampleRate`, `.read(position[, mode[, interpolation]])`. Positions are absolute sample positions; `writepos` aliases `writePosition`, rather than the Buffer node's normalized 0–1 output. File buffers hold the write position at `.end`. Read defaults: `Clamp`, `Linear`; use `Wrap` or `Zero`, and `Nearest` as needed. Legacy version-1 reads retain physical-index semantics.

| Functions | Arguments |
| --- | --- |
| `abs`, `sign`, `sqrt`, `exp`, `expm1`, `log`, `log2`, `log10`, `log1p`, `floor`, `ceil`, `round` | `(value)` |
| `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh` | `(value)`; angles in radians |
| `sineWave` | `(phase)`; phase in cycles |
| `atan2` | `(y, x)` |
| `pow` | `(base, exponent)` |
| `min`, `max` | `(a, b)` |
| `clamp` | `(value, minimum, maximum)` |
| `mix` | `(a, b, factor)` |
| `rand` | `(minimum, maximum[, seed])` |

Completion appears while typing identifiers (or with Ctrl+Space). Hover, signature help and inline parameter labels describe built-in and user-defined functions. Enter and Format indent single-statement if/else bodies even without braces.

The editor distinguishes Applied, Not applied yet and Invalid draft, separately from native optimization progress. An invalid draft keeps the last applied program running and shows a right-aligned red badge with contrasting ! in a reserved title slot on every Formula node using that asset, with the error available on hover. Built-in and user-defined function names, variables, parameters and read-only input ports use the theme's corresponding semantic colors. Long titles are truncated before the warning badge, which remains visible. Backspace does not open completion. Format uses `name(...)` for calls, `name(...) {` for definitions and `if (...)` / `for (...)` for control headers, with one space after argument commas. Comments remain unchanged. Temporary and persistent parameter hints share a single label per argument.

Ctrl+left-click (or Go to Definition) jumps to a user function, local variable or parameter declaration in the same Formula document, including forward function calls. Built-in functions open this reference at their function entry, using the independently selected documentation language. Empty editor areas show no component-name tooltip.

### User functions and bounded loops

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

Assignments and function arguments may implicitly convert `double` results to `float`; a float variable therefore does not need repeated casts after calculations with decimal literals. Float-to-integer and narrowing integer conversions still require casts for locals, state and function arguments. Conditions require bool; use a comparison or explicit bool cast. Existing formula language versions remain readable; function/loop syntax selects typed execution without changing the document format.

### Read-only variables

`final float gain = signal * 2;` computes a new value each evaluation (Audio: each sample; Control: each block), then prevents reassignment during that evaluation. Function parameters may use `final` too.

`const float MAX_SPEED = 2;` defines an input-independent constant expression: literals, earlier const variables and pure built-in math functions are allowed. Inputs, context, state, final locals, user functions, `rand` and `sineWave` are rejected in its initializer. Const parameters are not supported; use final parameters. `const` already implies read-only: do not combine it with `final`. `state` remains persistent and mutable; neither qualifier can be combined with state.

Final and const variable names are bold with the same semantic color; read-only inputs and context variables are bold, state names italic. These are display styles only. Qualifiers prevent mistakes; they do not guarantee faster processing.

### Built-in function reference

In typed formulas, math functions use promoted argument types; integer arguments use double except `abs`, `sign`, `min`, `max` and `clamp`, which preserve promoted integer types. `sineWave`, `rand` and Buffer reads return float. Legacy float formulas retain float arithmetic.

### abs

`abs(value)` returns the magnitude. The minimum signed integer retains its value because the positive counterpart is out of range.

### sign

`sign(value)` returns -1, 0 or 1 according to the sign.

### sqrt

`sqrt(value)` returns the square root; negative input produces NaN.

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

### buffer-read

`buffer.read(position[, mode[, interpolation]])` returns a float sample at an absolute sample position. Modes: Clamp (default), Wrap, Zero; interpolation: Linear (default), Nearest. Empty buffers return zero. Legacy version-1 boolean arguments retain physical-index semantics.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Configured group input | Configured group output |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Configured group input | Configured group output |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/formula-group.md)
