# Vector Scale

## Overview

Scales each vector axis around an origin.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Vector | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Scale | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (1, 1, 1) × | [×](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Origin | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vector | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

`vout = origin + scale ⊙ (v − origin)`, where ⊙ denotes componentwise multiplication. Scale 1 preserves an axis, 0 collapses it to the origin, negative values mirror it. This node performs no rotation.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Vector | Vector |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/vector-scale.md)
