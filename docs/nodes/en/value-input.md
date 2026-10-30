# Value Input

## Overview

Supplies an editable numeric value.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md) | Configured limits | — | 0 | — | — | Always |
| Slider type | [Editor](../../user/en/reference-tables.md) | Buttons, Bar | — | Buttons | — | — | Inspector |
| Enable minimum | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Inspector |
| Minimum | [Value](../../user/en/data-types.md) | ±∞ | — | 0 | — | — | Inspector |
| Enable maximum | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Inspector |
| Maximum | [Value](../../user/en/data-types.md) | ±∞ | — | 1 | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Value Out | [Value](../../user/en/data-types.md) | Configured limits | [Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Buttons can have optional bounds. Bar requires both bounds. Changing a bound also clamps Value; an invalid upper bound is raised above the minimum. The output updates once per block, even when editor motion is interpolated.

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
| Supported |

[Node index](index.md) · [Deutsch](../de/value-input.md)
