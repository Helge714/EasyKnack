# Trigger Timer

## Overview

Measures elapsed time and intervals between triggers.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Smoothing | [Value](../../user/en/data-types.md) | 0–1 | 0–1 | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Unit | [Editor](../../user/en/reference-tables.md) | s, ms, smp | — | s | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Elapsed | [Value](../../user/en/data-types.md) | 0–∞, selected unit | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Interval | [Value](../../user/en/data-types.md) | 0–∞, selected unit | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Elapsed starts at 0 on the first Trigger and resets at each subsequent Trigger. Interval remains 0 until two triggers have arrived, then holds their distance. Smoothing s retains the previous interval: `I = s × Iold + (1−s) × Inew`; the first measured interval initializes directly. Both outputs share the selected unit.

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

[Node index](index.md) · [Deutsch](../de/trigger-timer.md)
