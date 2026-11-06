# Spectrum Mix

## Overview

Adds two temporally aligned complex spectra.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| B | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Gain A | [Editor](../../user/en/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Always |
| Gain B | [Editor](../../user/en/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/rates.md#auto) |

## Behavior

Computes `A * Gain A + B * Gain B` on complex coefficients. Keep both gains at 1 for partial reconstruction. FFT size, window, hop, sample rate and frame positions must match. Incompatible batches produce an empty spectrum; there is no automatic resampling. A disconnected input contributes zero.

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
