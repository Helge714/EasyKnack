# Dynamics Curve

## Overview

Converts an amplitude level into a compressor gain factor.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Level | [Value](../../user/en/data-types.md#value) | 0–∞ | 0–∞ | 0 | [Factor](../../user/en/units.md) / [%](../../user/en/units.md) / [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Threshold | [Value](../../user/en/data-types.md#value) | −120–24 dB | −120–24 dB | −12 dB | [Factor](../../user/en/units.md) / [%](../../user/en/units.md) / [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Ratio | [Value](../../user/en/data-types.md#value) | 1–100 | 1–100 | 4 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Knee | [Value](../../user/en/data-types.md#value) | 0–48 dB | 0–48 dB | 6 dB | [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto / Audio / Control | — | Auto | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Gain | [Value](../../user/en/data-types.md#value) | 0–1 | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Let `x = 20 log10(max(Level, 10^-12)) − Threshold`, `s = 1 / Ratio − 1` and `W = Knee`. Gain reduction in dB is zero below `−W / 2`, `s × (x + W / 2)^2 / (2W)` inside the knee, and `s × x` above `W / 2`. Knee 0 gives a hard knee. Output is `10^(reduction / 20)`; Ratio 1 gives unity. Negative input levels are treated as zero. The node has no temporal smoothing; connect an Envelope Follower before it and use its Gain output to control a Gain node.

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

[Node index](index.md) · [Deutsch](../de/dynamics-curve.md)
