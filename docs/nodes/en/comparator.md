# Comparator

## Overview

Converts a numeric comparison into 0 or 1.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/en/reference-tables.md) | <, <=, >, >=, ==, != | — | < | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| A | [Value](../../user/en/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| B | [Value](../../user/en/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md) | 0–1 | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Outputs 1 when the selected comparison is true, otherwise 0. Equality compares the numeric values directly; it is not a tolerance test. The output is a numeric gate, not a Trigger. Use [Threshold Trigger](threshold-trigger.md) to create events.

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

[Node index](index.md) · [Deutsch](../de/comparator.md)
