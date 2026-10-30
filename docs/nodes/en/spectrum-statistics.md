# Spectrum Statistics

## Overview

Reduces displayed spectral values to one scalar.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Data | [Editor](../../user/en/reference-tables.md) | Magnitude / Phase / Power | — | Magnitude | — | — | Always |
| Statistic | [Editor](../../user/en/reference-tables.md) | Peak / Mean / RMS / Integral | — | Peak | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Value | [Value](../../user/en/data-types.md#value) | Statistic-dependent | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Magnitude is one-sided amplitude, Power its square and Phase the angle in radians. For selected bin values v[k]: Peak = max(abs(v)), Mean = Σv/K, RMS = sqrt(Σv²/K), Integral = Σv·Δf. Spectral-bin RMS is not time-domain audio RMS. The result holds until the next frame.

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

[Node index](index.md) · [Deutsch](../de/spectrum-statistics.md)
