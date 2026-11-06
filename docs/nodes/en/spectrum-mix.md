# Spectrum Mix

## Overview

Crossfades two temporally aligned complex spectra.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Mode | [Editor](../../user/en/reference-tables.md) | Linear, Equal Power | — | Linear | — | — | Always |
| Factor | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0.5 | Factor, % | [Control](../../user/en/rates.md#control) | Always |
| A | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| B | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Linear computes `A * (1 − f) + B * f`. Equal Power uses cosine and sine weights, `cos(πf/2)` and `sin(πf/2)`. Factor 0 selects A, factor 1 selects B. Factor is clamped to 0–1 and applied per FFT frame using the current block Control value. Equal Power preserves expected power for uncorrelated signals; identical signals gain approximately 3 dB at the midpoint. Opposite-phase coefficients can cancel. FFT size, window, hop, sample rate and frame positions must match; incompatible batches produce an empty spectrum. A disconnected input contributes zero. For partials plus residual use Spectrum Math with Add instead.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | A | [Spectrum](../../user/en/data-types.md#spectrum) |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| A | [Spectrum](../../user/en/data-types.md#spectrum) |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partial Analysis](partial-analysis.md) · [Partials to Spectrum](partials-to-spectrum.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/spectrum-mix.md)

[Spectrum Math](spectrum-math.md)
