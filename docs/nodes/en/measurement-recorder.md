# Measurement Recorder

## Overview

Appends triggered numeric snapshots to a document table.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Measurements | [Editor](../../user/en/reference-tables.md) | Table / CSV / Clear | — | Empty | — | — | Always |
| Record | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Inputs | [Editor](../../user/en/reference-tables.md) | 1–8 | — | 1 | — | — | Always |
| Values: names, units, order | [Editor](../../user/en/reference-tables.md) | Per-input settings | — | Value 1…8; no unit | — | — | Inspector |
| Value 1…8 | [Value](../../user/en/data-types.md#value) | — | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | According to Inputs |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

Every Record trigger appends one row containing all inputs at that sample position. The audio host must run to capture. The table is stored in the .knack document, with up to 4096 rows. Open the table to edit the test label after the row index. Inspector rows control names, unit labels and common port/column ordering; labels do not convert values. CSV exports offer standard and German-Excel formats. Clear removes stored rows.

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

[Node index](index.md) · [Deutsch](../de/measurement-recorder.md)
