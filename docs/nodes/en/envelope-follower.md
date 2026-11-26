# Envelope Follower

## Overview

Extracts a sample-wise amplitude envelope from audio.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Detector | [Editor](../../user/en/reference-tables.md) | Peak / RMS | — | Peak | — | — | Always |
| Attack | [Value](../../user/en/data-types.md#value) | 0–2000 ms | 0–2000 ms | 5 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Release | [Value](../../user/en/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Envelope | [Value](../../user/en/data-types.md#value) | 0–∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Peak follows the absolute input. RMS follows the squared input and takes its square root after smoothing. Attack applies to rising levels, Release to falling levels. For a constant target, the internal detector follows `e = a × e + (1 − a) × target`, with `a = exp(−1 / (time × fs))`. Times use seconds in this equation; zero is instantaneous. One time constant reaches approximately 63% of a Peak step or 63% of the squared RMS target. This is exponential tracking, not a finite RMS measurement window. Channels are measured separately.

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

[Node index](index.md) · [Deutsch](../de/envelope-follower.md)
