# Trigger Counter

## Overview

Counts incoming triggers and exposes their zero-based index.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Reset | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Wrap | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Count | [Value](../../user/en/data-types.md) | 1–16 777 216, integer | 1–16 777 216, rounded | 16 | — | [Control](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Index | [Value](../../user/en/data-types.md) | 0–16 777 215 | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

The first trigger outputs Index 0; subsequent triggers increment it. Index is held between events. Wrap cycles through 0…Count−1; without Wrap the counter saturates at 16 777 215. Reset prepares Index 0; a simultaneous Trigger is still counted as the first event. The Trigger output forwards counted events.

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

[Node index](index.md) · [Deutsch](../de/trigger-counter.md)
