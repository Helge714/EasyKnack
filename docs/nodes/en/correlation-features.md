# Correlation Features

## Overview

Reduces autocorrelation to periodicity, period and noise descriptors.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Autocorrelation | [Analysis resource](../../user/en/data-types.md#analysis) | — | Correlation resource | — | — | [Control](../../user/en/rates.md#control) | Always |
| Minimum frequency | [Editor](../../user/en/reference-tables.md) | ≥ 0.001 Hz | — | 20 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | — | Inspector |
| Maximum frequency | [Editor](../../user/en/reference-tables.md) | ≥ 0.001 Hz | — | 20000 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | — | Inspector |
| Periodicity threshold | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0.3 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | — | Inspector |
| Silence threshold | [Editor](../../user/en/reference-tables.md) | ≥ 0 | — | 0.000001 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Periodicity | [Value](../../user/en/data-types.md#value) | 0–1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Period | [Value](../../user/en/data-types.md#value) | ≥ 0 s | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Frequency | [Value](../../user/en/data-types.md#value) | ≥ 0 Hz | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| HNR | [Value](../../user/en/data-types.md#value) | ±120 dB | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Valid | [Value](../../user/en/data-types.md#value) | 0–1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Searches positive local correlation peaks within the frequency bounds, with sub-sample peak interpolation. At least three periods must fit the analyzed window. Periodicity p is the selected normalized correlation peak, not a probability. Period is its lag in seconds; Frequency = 1/Period. HNR = `10·log10(p/(1−p))`, limited to ±120 dB: 0 dB means equal estimated periodic and residual power, positive values favor periodic power. It depends on the upstream window and normalization. Valid = 1 only when peak and source RMS pass both thresholds; otherwise Period/Frequency are zero. Values hold between frames. Feed [Autocorrelation](autocorrelation.md) or its averaged result, not a raw spectrum.

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

[Node index](index.md) · [Deutsch](../de/correlation-features.md)
