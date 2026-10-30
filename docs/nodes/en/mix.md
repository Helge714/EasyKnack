# Mix

## Overview

Crossfades between two numeric signals.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Mode | [Editor](../../user/en/reference-tables.md) | Linear, Equal Power | — | Linear | — | — | Always |
| Factor | [Value](../../user/en/data-types.md) | 0–1 × | 0–1 × | 0.5 × | ×, % | [Auto](../../user/en/rates.md) | Always |
| A | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| B | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md) | ±∞ | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Linear: `y = (1−t)A + tB`. Equal Power: `y = cos(πt/2)A + sin(πt/2)B`, with t clamped to 0–1. Equal Power preserves expected power for uncorrelated signals; identical inputs can gain about 3 dB at the midpoint. Linear preserves identical-input level.

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

[Node index](index.md) · [Deutsch](../de/mix.md)
