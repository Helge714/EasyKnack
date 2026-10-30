# Analysis Average

## Overview

Performs one triggered arithmetic measurement and holds its final result.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Analysis | [Analysis resource](../../user/en/data-types.md#analysis) | — | Spectrum or correlation | — | — | [Control](../../user/en/rates.md#control) | Always |
| Start | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Measure by | [Editor](../../user/en/reference-tables.md) | Time / Measurements | — | Time | — | — | Always |
| Duration | [Editor](../../user/en/reference-tables.md) | 1 smp–86400 s | — | 1 s | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Time |
| Measurements | [Editor](../../user/en/reference-tables.md) | 1–16777216 | — | 16 | — | — | Measurements |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Mean | [Analysis resource](../../user/en/data-types.md#analysis) | Spectrum or correlation | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Count | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Finished | [Trigger](../../user/en/data-types.md#trigger) | — | [Trigger](../../user/en/rates.md#trigger) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Start clears and restarts acquisition, latching Duration/Measurements. Time measures audio-clock duration; Measurements stops after the selected number of accepted values. Finished emits once at completion. The running result is available during acquisition and held afterward until Start.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Analysis | Mean |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Analysis | Mean |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/analysis-average.md)
