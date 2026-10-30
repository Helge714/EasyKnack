# Pitch Detection

## Overview

Estimates a fundamental from autocorrelation.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Autocorrelation | [Analysis resource](../../user/en/data-types.md#analysis) | — | Correlation resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Minimum | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 40 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | — | Always |
| Maximum | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequency [Hz] | [Value](../../user/en/data-types.md#value) | 0–20000 Hz | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Confidence | [Value](../../user/en/data-types.md#value) | 0–1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Ignores the initial zero-lag lobe and selects the earliest local peak reaching 90% of the strongest peak in range, provided that peak reaches 0.1. Parabolic interpolation refines its lag. Frequency = 1/lag; Confidence is its normalized correlation height. No suitable peak gives zero. Use Rectangular FFT, 2x padding and several periods of the lowest expected frequency. Minimum must be below Maximum. [Correlation Features](correlation-features.md) additionally provides validity and HNR.

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

[Node index](index.md) · [Deutsch](../de/pitch-detection.md)
