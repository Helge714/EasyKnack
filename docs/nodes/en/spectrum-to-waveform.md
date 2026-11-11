# Spectrum to Waveform

## Overview

Captures a periodic wavetable from a spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Fundamental | [Value](../../user/en/data-types.md#value) | 1–20000 Hz | 1–20000 Hz | 440 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | [Auto](../../user/en/rates.md#auto) | Always |
| Capture | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Normalize | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Copy captured resource | [Editor](../../user/en/reference-tables.md) | Button | — | — | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Waveform | [Wavetable resource](../../user/en/data-types.md#wavetable) | 2048 samples | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Capture requests a background reconstruction from a valid FFT frame using Fundamental as the harmonic reference. The output holds the captured resource until the next capture; it does not rebuild every block. Normalize scales the captured waveform. Copy captured resource places sample data on the application clipboard; paste creates a [Waveform](waveform.md) in Resource mode. Captured samples are saved with the document. Inspect them with [Wavetable Viewer](wavetable-viewer.md).

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

[Node index](index.md) · [Deutsch](../de/spectrum-to-waveform.md)
