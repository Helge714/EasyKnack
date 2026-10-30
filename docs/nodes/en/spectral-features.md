# Spectral Features

## Overview

Measures the frequency distribution and its frame-to-frame change.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Analysis resource](../../user/en/data-types.md#analysis) | — | Spectrum or analysis resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Frequency scale | [Editor](../../user/en/reference-tables.md) | Linear / Logarithmic | — | Linear | — | — | Inspector |
| Weighting | [Editor](../../user/en/reference-tables.md) | Power / Magnitude | — | Power | — | — | Always |
| Roll-off fraction | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0.85 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | — | Always |
| Normalize flux | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Centroid | [Value](../../user/en/data-types.md#value) | 0–Nyquist Hz | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Spread | [Value](../../user/en/data-types.md#value) | ≥ 0 Hz / oct | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Skewness | [Value](../../user/en/data-types.md#value) | ±∞ | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Kurtosis | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Flux | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Rolloff | [Value](../../user/en/data-types.md#value) | 0–Nyquist Hz | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Slope | [Value](../../user/en/data-types.md#value) | ±∞ / Hz | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Flatness | [Value](../../user/en/data-types.md#value) | 0–1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Let A[k] = abs(X[k])/Σw[n] and m[k] = 1 at DC/Nyquist, otherwise 2. Weight is W[k] = m[k]·A[k]² for Power or m[k]·A[k] for Magnitude. Set p[k] = W[k]/ΣW. Linear moments use z[k] = f[k]; logarithmic moments use z[k] = log2(f[k]) and exclude DC, renormalizing the remaining weights.

- Centroid: μ = Σp·z; logarithmic output is 2^μ Hz.
- Spread: σ = sqrt(Σp·(z−μ)²), in Hz or octaves.
- Skewness: Σp·(z−μ)³/σ³.
- Kurtosis: Σp·(z−μ)⁴/σ⁴; Gaussian = 3.
- Flux: sqrt(Σ(current−previous)²), using p when normalized, otherwise W. First frame = 0.
- Roll-off: first frequency whose cumulative weight reaches the selected fraction.
- Slope: least-squares slope of p against f, per Hz.
- Flatness: geometric mean of floored W divided by its arithmetic mean; floor = max(1e−300, mean(W)·1e−12).

Logarithmic scale changes only the four moments. Silence and undefined zero-spread moments return zero. Outputs hold between frames. Mean spectral flatness of a single white-noise FFT is below 1; averaging power spectra first reduces estimator variation.

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

[Node index](index.md) · [Deutsch](../de/spectral-features.md)
