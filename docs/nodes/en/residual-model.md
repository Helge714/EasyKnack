# Residual Model

## Overview

Models a residual spectral envelope and synthesizes a stochastic spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Residual | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Envelope width | [Value](../../user/en/data-types.md#value) | 0–20000 Hz | 0–∞ Hz | 200 Hz | Hz, kHz | [Control](../../user/en/rates.md#control) | Always |
| Envelope smoothing | [Editor](../../user/en/reference-tables.md) | 0–10000 ms | — | 0 ms | ms, s, smpl | — | Inspector |
| Seed | [Editor](../../user/en/reference-tables.md) | 0–2147483647 | — | 0 | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Averages neighboring bin powers within the envelope width (full width in Hz), then assigns fresh reproducible random phases to each frame. At 0 Hz individual bin powers are retained before windowing. Envelope smoothing is an exponential power average with the specified time constant. Generated noise receives the source window; complex conjugate symmetry and removed DC are preserved. Frame power follows the smoothed envelope, not perceived loudness. Seed and frame position determine the random sequence.

Modelling is deliberately lossy and is not classification. Use the original residual for exact reconstruction. FFT size, hop and frame time remain unchanged, so the result can be added to the matching partial spectrum. For an independent shorter residual resolution, first resynthesize through IFFT and analyze with a separate FFT. Mix such branches after their IFFTs and compensate additional latency. Window size is not changed automatically.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Residual | [Spectrum](../../user/en/data-types.md#spectrum) |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Residual | [Spectrum](../../user/en/data-types.md#spectrum) |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partial Analysis](partial-analysis.md) · [Partials to Spectrum](partials-to-spectrum.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/residual-model.md)

[Spectrum Math](spectrum-math.md)
