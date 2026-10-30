# Bin Smoothing

## Overview

Blends new bin values into per-bin memory.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Update | [Value](../../user/en/data-types.md) | 0–1 | 0–1 | 1 | — | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Each FFT frame computes `held = (1−u) × held + u × value`. The first frame initializes directly. Update 0 holds and 1 replaces. Smaller hops update more frequently, so the same factor has a different time response at another hop size. Audio inputs are sampled at the window end. Only valid in [Spectral Groups](spectral-group.md).

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

[Node index](index.md) · [Deutsch](../de/bin-smoothing.md)
