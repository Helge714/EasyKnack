# Waveform

## Overview

Creates an editable periodic Wavetable resource.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Waveform | [Editor](../../user/en/reference-tables.md) | Curve / Resource editor | — | Sine | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Waveform | [Wavetable resource](../../user/en/data-types.md#wavetable) | — | [Control](../../user/en/rates.md#control) | — |

## Behavior

Curve mode defines a periodic waveform using control points. Resource mode edits sampled waveform values directly, without Bézier segments. Switching modes can discard the previous representation. Captured Wavetables can be pasted as sample resources. Connect the output to an oscillator's Waveform input; this resource does not play by itself.

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

[Node index](index.md) · [Deutsch](../de/waveform.md)

Control points and Bezier handles appear while hovering or editing the graph. Clicking outside hides them and leaves the curve visible.
