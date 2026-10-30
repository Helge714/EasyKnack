# Trigger Divider

## Overview

Passes every nth input trigger.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Division | [Value](../../user/en/data-types.md) | 1–1024, integer | 1–1024, rounded | 2 | — | [Control](../../user/en/rates.md) | Always |
| Reset | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Division n emits on the nth event, then restarts the counter. Reset clears the counter; a simultaneous input is counted after Reset. Division 1 passes every event. Division changes do not themselves emit a trigger.

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

[Node index](index.md) · [Deutsch](../de/trigger-divider.md)
