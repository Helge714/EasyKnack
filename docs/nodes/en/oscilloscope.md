# Oscilloscope

## Overview

Displays a triggered audio waveform.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Oscilloscope | [Editor](../../user/en/reference-tables.md) | Graph | — | — | — | — | Always |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Channel | [Editor](../../user/en/reference-tables.md) | Left / Right / Mix | — | Left | — | — | Always |
| Vertical range | [Editor](../../user/en/reference-tables.md) | ≥ 0.001 | — | 1 | — | — | Always |
| Trigger low | [Editor](../../user/en/reference-tables.md) | ±1 | — | −0.1 | — | — | Always |
| Trigger high | [Editor](../../user/en/reference-tables.md) | ±1 | — | 0.1 | — | — | Always |
| Window | [Editor](../../user/en/reference-tables.md) | 6–200 ms | — | 20 ms | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

The trigger arms below Trigger low and captures on the following crossing of Trigger high. If no crossing arrives, capture runs automatically. The graph context menu shows/hides threshold lines. Polyphonic voices are summed before measurement; Mix = (L+R)/2. Vertical range r displays −r–r and changes no signal values.

Right-click the graph to Freeze/Resume or open an enlarged view; double-click also enlarges it. The expanded graph supports wheel zoom, Shift+wheel vertical zoom, middle-drag panning and two draggable measurement cursors. Context menus clear cursors individually or reset the view. Export offers an image of the complete graph or visible section and a CSV table. Freeze holds the display, not audio processing.

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

[Node index](index.md) · [Deutsch](../de/oscilloscope.md)
