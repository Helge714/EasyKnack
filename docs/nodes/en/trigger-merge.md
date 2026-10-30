# Trigger Merge

## Overview

Combines several trigger inputs into one stream.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Inputs | [Value](../../user/en/data-types.md#value) | 2–16, integer | — | 2 | — | — | Always |
| Input 1…N | [Trigger](../../user/en/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md#trigger) | — | [Trigger](../../user/en/rates.md#trigger) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Events at different samples retain their positions. Events arriving at the same sample collapse to one output trigger. Reducing Inputs removes trailing ports and their connections; Undo can restore the change.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Input 1 | Trigger |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Input 1 | Trigger |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/trigger-merge.md)
