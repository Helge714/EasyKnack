# Wavetable Viewer

## Overview

Displays the actual samples of a wavetable resource.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Waveform | [Wavetable resource](../../user/en/data-types.md#wavetable) | — | Wavetable resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Samples | [Editor](../../user/en/reference-tables.md) | Sample graph | — | — | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

The graph shows stored resource samples across one period, not an editable Bézier curve. Connect [Waveform](waveform.md) or [Spectrum to Waveform](spectrum-to-waveform.md) to inspect the table actually supplied to oscillators. Resizing the node vertically enlarges the sample graph. Hover shows sample index and value. Right-click to copy the resource; pasting creates a Waveform node in Resource mode.

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

[Node index](index.md) · [Deutsch](../de/wavetable-viewer.md)
