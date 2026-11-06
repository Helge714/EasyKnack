# Partial Analysis

## Overview

Separates a spectrum into tracked partials and a complex residual spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Threshold | [Editor](../../user/en/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Inspector |
| Maximum partials | [Editor](../../user/en/reference-tables.md) | 1–256 | — | 64 | — | — | Inspector |
| Minimum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz | — | Inspector |
| Maximum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz | — | Inspector |
| Tracking tolerance | [Editor](../../user/en/reference-tables.md) | 0–2400 cent | — | 100 cent | cent | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Residual | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Local FFT peaks use logarithmic interpolation. Tracking matches frequency distance in cents; new partials receive new IDs. Age advances by hop duration. The strongest candidates are retained up to the configured limit. Identity is not guaranteed at crossings or after lost peaks. Analysis subtracts reconstructed complex window lobes from the original; weak components and unmodelled tails remain in the residual. Negative-start frames remain entirely in the residual.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| No | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partials to Spectrum](partials-to-spectrum.md) · [Spectrum Math](spectrum-math.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/partial-analysis.md)
