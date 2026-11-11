# Buffer Reader (Speed)

## Overview

Reads a buffer with an independent playback clock.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wrap | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Buffer | [Buffer resource](../../user/en/data-types.md#buffer) | — | Buffer resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Speed | [Value](../../user/en/data-types.md#value) | ±8 | ±8 | 1 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Offset | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Reset | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Distance | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Read position | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Speed 1 plays normally, 0 holds and negative values reverse playback. Offset subtracts a fraction of the buffer length from the cursor. Reset places a recording-buffer cursor at the current write position, or a file-buffer cursor at the file beginning, before applying Offset. Read position can drive a [Position reader](buffer-reader-position.md).

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

[Node index](index.md) · [Deutsch](../de/buffer-reader-speed.md)
