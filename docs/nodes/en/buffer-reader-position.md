# Buffer Reader (Position)

## Overview

Reads a buffer at an externally supplied position.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wrap | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Buffer | [Buffer resource](../../user/en/data-types.md#buffer) | — | Buffer resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Position | [Value](../../user/en/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Offset | [Value](../../user/en/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Distance | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Read position | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Position is an absolute fraction of the buffer; Offset is subtracted from it. Connect another reader’s Read position and use Offset = 0.5 for a synchronized second head. This reader has no internal playback clock.

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

[Node index](index.md) · [Deutsch](../de/buffer-reader-position.md)
