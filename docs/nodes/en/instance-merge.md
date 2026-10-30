# Instance Merge

## Overview

Reduces instance contributions to a shared signal.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Combine | [Editor](../../user/en/reference-tables.md) | Sum, Mean, Weighted Mean | — | Mean | — | — | Always |
| Per-instance signal | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Per-instance weight | [Value](../../user/en/data-types.md#value) | 0–1 | 0–∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Combined | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Used inside [Instance Groups](instance-group.md) and their spectral equivalent. Sum is `Σ(wᵢxᵢ)`, Mean divides that by active count, Weighted Mean divides by `Σwᵢ`. Negative weights become zero; zero total weight produces silence. Nodes after the merge form shared processing. Several merge branches may be mixed downstream.

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

[Node index](index.md) · [Deutsch](../de/instance-merge.md)
