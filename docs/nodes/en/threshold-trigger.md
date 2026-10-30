# Threshold Trigger

## Overview

Emits triggers when a value crosses a threshold.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Edge | [Editor](../../user/en/reference-tables.md) | Rising, Falling, Both | — | Rising | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| In | [Value](../../user/en/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Threshold | [Value](../../user/en/data-types.md) | ±1 000 000 | ±∞ | 0.5 | — | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Compares `input > threshold`. Rising fires on false-to-true, Falling on true-to-false, Both on either change. The first value initializes without an event. Audio checks each sample; Control checks block values. Moving Threshold can also cause a crossing.

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

[Node index](index.md) · [Deutsch](../de/threshold-trigger.md)
