# Autocorrelation

## Overview

Computes autocorrelation from an existing spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Normalization | [Editor](../../user/en/reference-tables.md) | Biased / Unbiased | — | Biased | — | — | Always |
| Smoothing factor | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 1 | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Correlation | [Analysis resource](../../user/en/data-types.md#analysis) | ±1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

`r = IFFT(|X|²)`, normalized to lag zero; silence returns zero. FFT padding 2x gives linear correlation, without padding it is circular. Unbiased compensates the decreasing sample overlap at larger linear lags, then limits values to ±1; window tapering is not undone. For untapered, DC-centered correlation choose Rectangular, 2x padding and Remove DC in [FFT](fft.md). Each frame updates `held = old·(1 − factor) + new·factor`, initialized from the first frame. Factor 0 holds that frame; 1 updates immediately. [Correlation Features](correlation-features.md) reduces the result to scalar descriptors.

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

[Node index](index.md) · [Deutsch](../de/autocorrelation.md)
