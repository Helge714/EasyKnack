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
- Operators: `+`, `-`, `*`, `/`, `%`, unary `+` / `-` / `!`, `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`; parentheses control precedence. No loops, pointers or arbitrary C++ calls.
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

Completion, hover and signature help describe function domains and argument details.

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
