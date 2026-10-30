# Value Viewer

## Overview

Displays numeric inputs as values or time histories.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Values | [Editor](../../user/en/reference-tables.md) | Graph / Values | — | Graph | — | — | Always |
| Inputs | [Editor](../../user/en/reference-tables.md) | 1–8 | — | 1 | — | — | Always |
| Time window | [Editor](../../user/en/reference-tables.md) | 0.1–600 s | — | 10 s | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |
| Record every sample | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Inspector |
| Channel | [Editor](../../user/en/reference-tables.md) | Left / Right / Mix | — | Left | — | — | Inspector |
| Values: names, units, order | [Editor](../../user/en/reference-tables.md) | Per-input settings | — | Value 1…8; no unit | — | — | Inspector |
| Value 1…8 | [Value](../../user/en/data-types.md#value) | — | ±∞ | — | — | [Auto](../../user/en/rates.md#auto) | According to Inputs |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

Inputs determines both the ports and displayed series. Inspector rows change names, unit labels and order for ports, display and exports together; unit labels do not convert values. History mode shows separate plots or an overlay; Values mode shows discrete readouts. Freeze holds a snapshot. Expanded views allow zoom, pan and measurement cursors; export supports images and tables for the full history or visible section.

Without Record every sample, each 10 ms interval retains minimum, maximum and last value. With it enabled, every sample is retained. History is bounded to one million input records, so its effective duration may be shorter than Time window. Transport gaps remain visible gaps. Control values hold over their block; Mix observes (L+R)/2.

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

[Node index](index.md) · [Deutsch](../de/value-viewer.md)
