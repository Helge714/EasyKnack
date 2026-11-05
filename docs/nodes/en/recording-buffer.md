# Recording Buffer

## Overview

Records audio into a circular buffer.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Size | [Editor](../../user/en/reference-tables.md) | 256 smp–30 s | — | 2 s | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Buffer | [Buffer resource](../../user/en/data-types.md#buffer) | Recorded samples | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Write position | [Value](../../user/en/data-types.md#value) | 0 ≤ position < 1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Size sets the stored duration at the DSP sample rate. Resizing allocates an empty buffer and resets readers. Readers see a sample-accurate write head, including earlier samples in the current block but never future samples. Read the resource with [Buffer Reader (Speed)](buffer-reader-speed.md) or [Buffer Reader (Position)](buffer-reader-position.md).

Write position is the normalized physical position of the next sample to write. It advances sample by sample, wraps from the buffer end to 0, and matches the write-head reference used by both readers. Resizing restarts it. Reader Distance and Read position retain their existing meanings.

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

[Node index](index.md) · [Deutsch](../de/recording-buffer.md)
