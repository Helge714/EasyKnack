# Bin Sample & Hold

## Overview

Captures values separately for each bin and channel.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Capture | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Before capture, the incoming values pass through. Capture stores the next complete FFT frame's value independently per bin/channel, then holds it until the next capture. Holding magnitude alone still uses a live phase if phase is wired directly; this is not an autonomous freeze of the complete complex spectrum. Only valid in [Spectral Groups](spectral-group.md).

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Value | Value |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Value | Value |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/bin-sample-hold.md)
