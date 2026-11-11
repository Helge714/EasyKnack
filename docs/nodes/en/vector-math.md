# Vector Math

## Overview

Performs arithmetic with vector-valued results.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/en/reference-tables.md) | Add, Subtract, Multiply, Divide, Scale, Normalize, Negate, Absolute, Minimum, Maximum, Cross | — | Add | — | — | Always |
| A | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md); [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle), [turn](../../user/en/units.md#angle); [×](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| B | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md); [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle), [turn](../../user/en/units.md#angle); [×](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Binary operation |
| Factor | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Scale operation |
| Quantity | [Editor](../../user/en/reference-tables.md) | Length, Angle, Scale, Unitless | — | Length | — | — | Inspector |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vector | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Add/Subtract/Multiply/Divide/Minimum/Maximum work componentwise. Division by a zero component returns zero there. Scale multiplies A by Factor. Normalize gives `A/length(A)` or the zero vector for zero length. Cross computes the right-handed cross product. Negate and Absolute act per component. Scalar results use [Vector Length](vector-length.md), [Vector Distance](vector-distance.md) or [Vector Dot Product](vector-dot-product.md).

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| A | Vector |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/vector-math.md)
