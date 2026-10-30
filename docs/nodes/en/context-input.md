# Context Input

## Overview

Supplies channel, timing and block context values.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — | — | — |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Channel | [Value](../../user/en/data-types.md#value) | ±1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Sample Rate | [Value](../../user/en/data-types.md#value) | 48 000 Hz | [Control](../../user/en/rates.md#control) | Mono |
| Block Size | [Value](../../user/en/data-types.md#value) | 1–256 smp | [Control](../../user/en/rates.md#control) | Mono |
| Time | [Value](../../user/en/data-types.md#value) | 0–∞ s | [Audio](../../user/en/rates.md#audio) | Mono |

## Behavior

Channel is −1 for left and +1 for right. Sample Rate is the host DSP rate; Block Size is the actual number of frames per channel, not total routing latency. Time advances per rendered sample while this node is active and survives graph edits. Use Channel to create explicitly different channel paths.

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

[Node index](index.md) · [Deutsch](../de/context-input.md)
