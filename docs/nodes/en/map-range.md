# Map Range

## Overview

Maps one numeric interval linearly to another.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| Clamp ±1 | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Value | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| From Min | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| From Max | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Always |
| To Min | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| To Max | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md#value) | ±∞ | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

`t = (x − From Min) / (From Max − From Min)`; `y = To Min + t × (To Max − To Min)`. Clamp limits t to 0–1, not the final value to ±1 despite the control label. Without Clamp, values outside the source range are extrapolated. A degenerate source interval returns To Min. Reversed intervals reverse the mapping.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Value | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Value | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/map-range.md)
