# Bin Context

## Overview

Exposes the current bin of a Spectral Group.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md) | — | — | — | — | [Control](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Real | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Imaginary | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Magnitude | [Value](../../user/en/data-types.md) | 0–∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Phase | [Value](../../user/en/data-types.md) | ±π rad | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Frequency | [Value](../../user/en/data-types.md) | 0–fs/2 Hz | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Bin index | [Value](../../user/en/data-types.md) | 0–N/2 | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Position | [Value](../../user/en/data-types.md) | 0–1 | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Frame time | [Value](../../user/en/data-types.md) | ±∞ s | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Only valid inside a [Spectral Group](spectral-group.md). For FFT size N, bin k has frequency `k × fs/N` and Position `k/(N/2)`. Real/Imaginary and Magnitude are raw complex FFT coefficients, not normalized amplitudes. Frame time is the window start, which can initially be negative due to zero padding. The displayed Audio rate denotes a bin vector evaluated per frame here, not temporal audio.

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

[Node index](index.md) · [Deutsch](../de/bin-context.md)
