# Spectrum Prune

## Overview

Retains the loudest fraction of spectral bins.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Keep | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 1 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | Complex FFT bins | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

The retained bin count is rounded from Keep times the nonnegative bin count, including DC and Nyquist. Ranking uses single-sided amplitude; ties favor lower indices. Retained coefficients and phases are unchanged, with no gain normalization. Keep 0 clears all bins; 1 bypasses processing. [Spectral Group](spectral-group.md) can skip zero bins when Skip empty bins is enabled.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Spectrum | Spectrum |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Spectrum | Spectrum |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/spectrum-prune.md)
