# Math

## Overview

Applies a selectable mathematical operation.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/en/reference-tables.md) | Add, Subtract, Multiply, Divide, Power, Remainder, Modulo, Minimum, Maximum, Floor, Ceil, Round, Fract, Sqrt, Root, Exp, Log, Log Base, Absolute, Negate | — | Multiply | — | — | Always |
| Clamp ±1 | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| A | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| B | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Binary operation only |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md#value) | ±∞ | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Binary operations evaluate A and B; unary operations hide B. Operand names and automatic defaults follow the operation. Remainder follows the dividend sign; Modulo uses a floor-based remainder. Fract is `x − floor(x)`. Division by a near-zero divisor and invalid domains return safe values; nonfinite results become zero. Optional Clamp limits the final result to ±1. Each connected input keeps its own rate. The same operations are available inside [Spectral Groups](spectral-group.md).

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | A | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| A | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/math.md)
