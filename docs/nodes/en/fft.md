# FFT

## Overview

Transforms windowed audio into complex spectral frames.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Auto](../../user/en/rates.md#auto) | Always |
| Synchronous | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Minimum frame interval | [Editor](../../user/en/reference-tables.md) | 0–60000 ms | — | 0 ms | [ms](../../user/en/units.md) | — | Inspector; asynchronous |
| Window size | [Editor](../../user/en/reference-tables.md) | 256 / 512 / 1024 / 2048 / 4096 / 8192 / 16384 / 32768 / 65536 smp | — | 2048 smp | [smp](../../user/en/units.md) | — | Always |
| Window | [Editor](../../user/en/reference-tables.md) | Hann / Hamming / Blackman / Rectangular | — | Hann | — | — | Always |
| Zero padding | [Editor](../../user/en/reference-tables.md) | None / Linear correlation (2x) | — | None | — | — | Always |
| Remove DC | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Overlap | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0.75 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | — | Always |
| Channel | [Editor](../../user/en/reference-tables.md) | All / Left / Right / Mix | — | All | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | Complex FFT bins | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Each frame contains complex coefficients `X[k] = Σ x[n]·w[n]·exp(−i·2πkn/N)` with its source sample position. Bin frequency is `k·sampleRate/N`; zero padding increases N without adding measured information. Remove DC subtracts the window mean before windowing. Hop = rounded `windowSize·(1 − overlap)`, with at least 64 samples synchronously and 1 asynchronously. All preserves stereo and voices; Left/Right/Mix sum voices, with Mix = `(L+R)/2`. Synchronous processing is intended for [IFFT](ifft.md). Asynchronous analysis can skip or deliver late frames; Minimum frame interval additionally limits its request rate. For linear [Autocorrelation](autocorrelation.md), choose Rectangular and 2x padding.

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

[Node index](index.md) · [Deutsch](../de/fft.md)
