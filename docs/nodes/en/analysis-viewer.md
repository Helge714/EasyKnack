# Analysis Viewer

## Overview

Displays a spectrum or autocorrelation resource.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Analysis | [Editor](../../user/en/reference-tables.md) | Graph | — | — | — | — | Always |
| Analysis | [Analysis resource](../../user/en/data-types.md#analysis) | — | Spectrum or correlation | — | — | [Control](../../user/en/rates.md#control) | Always |
| Data | [Editor](../../user/en/reference-tables.md) | Auto / Magnitude / Phase / Power / Correlation | — | Auto | — | — | Always |
| Vertical range | [Editor](../../user/en/reference-tables.md) | ≥ 0.001 | — | 1; Phase: π | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

Auto follows the resource type. Magnitude displays one-sided amplitude, Power its square, Phase radians relative to the FFT window start; Correlation displays lag values. Unsupported views fall back to the resource default. Vertical range affects only display scaling. Spectrum context menus switch linear/logarithmic frequency axes; hover reports bin, frequency and value. The viewer does not calculate another FFT or autocorrelation.

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

[Node index](index.md) · [Deutsch](../de/analysis-viewer.md)
