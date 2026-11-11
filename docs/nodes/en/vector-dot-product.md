# Vector Dot Product

## Overview

Calculates the scalar product of two vectors.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md); [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle), [turn](../../user/en/units.md#angle); [×](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| B | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md); [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle), [turn](../../user/en/units.md#angle); [×](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Quantity | [Editor](../../user/en/reference-tables.md) | Length, Angle, Scale, Unitless | — | Length | — | — | Inspector |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Dot Product | [Value](../../user/en/data-types.md#value) | ±∞ | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

`dot = Ax Bx + Ay By + Az Bz`. For normalized vectors this is the cosine of their angle. The output is a raw number; multiplying length components mathematically gives squared-length units, not another position vector.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/vector-dot-product.md)
