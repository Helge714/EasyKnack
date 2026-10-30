# Trigger Filter

## Overview

Passes triggers only while a numeric condition is positive.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Condition | [Value](../../user/en/data-types.md) | 0–1 | ±∞ | 1 | — | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

At each trigger sample, a finite Condition greater than 0 permits the event; otherwise it is discarded. Condition changing without a Trigger produces no output event. Use a [Comparator](comparator.md) for index-dependent selection.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Trigger | Trigger |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Trigger | Trigger |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/trigger-filter.md)
