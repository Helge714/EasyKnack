# Partials to Spectrum

## Overview

Reconstructs a complex spectrum from partials for IFFT.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/rates.md#auto) |

## Behavior

Reconstructs positive and negative frequency contributions with the analysis window. Frequency changes integrate into a continuous phase correction between frames. Frequencies outside 0–Nyquist and nonpositive amplitudes are discarded. Unmodified partials plus their associated residual reproduce the original spectrum. Lobe evaluation is limited to eight window bins on either side; remaining original tails reside in the residual.

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

[Partial Analysis](partial-analysis.md) · [Spectrum Mix](spectrum-mix.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/partials-to-spectrum.md)
