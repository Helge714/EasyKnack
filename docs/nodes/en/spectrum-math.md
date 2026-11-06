# Spectrum Math

## Overview

Applies complex arithmetic to two temporally aligned spectra.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/en/reference-tables.md) | Add, Subtract, Multiply, Divide | — | Add | — | — | Always |
| A | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| B | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Gain A | [Editor](../../user/en/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Inspector |
| Gain B | [Editor](../../user/en/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Computes `op(A * Gain A, B * Gain B)` for each complex FFT coefficient. Add and Subtract retain phase relationships. Multiply is complex multiplication, not sample-wise multiplication of the audio signals. Division by zero and nonfinite results produce 0. No automatic level normalization is applied. A disconnected input contributes zero. Both spectra must share FFT size, window, hop, sample rate and frame positions; incompatible batches produce an empty spectrum. For partial reconstruction use Add and both gains at 1. Earlier weighted Spectrum Mix nodes migrate to this node on load, preserving connections and gains.

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

[Deutsch](../de/spectrum-math.md)
