# Instance Context

## Overview

Supplies the current instance index and count.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — | — | — |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Index | [Value](../../user/en/data-types.md) | 0–Count−1 | [Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Count | [Value](../../user/en/data-types.md) | 0–Maximum | [Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Position | [Value](../../user/en/data-types.md) | 0–1 | [Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Seed | [Value](../../user/en/data-types.md) | 0–16 777 215 | [Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Inside an [Instance Group](instance-group.md), Index is zero-based and Count is the active count. Position is `Index/(Count−1)` for more than one instance, otherwise 0. Seed is stable per instance. Connecting Index or Seed makes otherwise identical generators instance-dependent.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| No | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/instance-context.md)
