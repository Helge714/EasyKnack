# Bin Reader

## Overview

Reads an indexed bin from the original input spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md) | — | — | — | — | [Control](../../user/en/rates.md) | Always |
| Bin index | [Value](../../user/en/data-types.md) | ±∞ | Boundary-dependent | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Outside spectrum | [Editor](../../user/en/reference-tables.md) | Zero, Clamp, Wrap | — | Zero | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Real | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Imaginary | [Value](../../user/en/data-types.md) | ±∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Magnitude | [Value](../../user/en/data-types.md) | 0–∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Phase | [Value](../../user/en/data-types.md) | ±π rad | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Amplitude | [Value](../../user/en/data-types.md) | 0–∞ | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Frequency | [Value](../../user/en/data-types.md) | 0–fs/2 Hz | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Inside a [Spectral Group](spectral-group.md), fractional indices interpolate neighboring complex bins. Reads always use the original source frame, independent of other bin writes. Zero returns zero outside the one-sided spectrum, Clamp holds its edge, Wrap repeats 0…N/2. Unconnected Spectrum produces zero. Amplitude divides raw magnitude by the analysis-window sum and doubles non-DC/non-Nyquist bins; it is suitable for oscillator weights. Phase is in radians.

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

[Node index](index.md) · [Deutsch](../de/bin-reader.md)
