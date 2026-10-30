# Trigger Delay

## Overview

Delays triggers and can protect trigger feedback loops.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Time | [Value](../../user/en/data-types.md) | 1 smp–10 s | 1 smp–10 s | 0.25 s | s, ms, smp | [Control](../../user/en/rates.md) | Always |
| Reset | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Time is rounded to samples and limited to at least one sample. Events keep their scheduled due times when later Time values change. Reset clears pending events. Several events due at the same sample collapse to one trigger. The delay is an execution boundary for feedback; control its time from outside the loop.

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

[Node index](index.md) · [Deutsch](../de/trigger-delay.md)
