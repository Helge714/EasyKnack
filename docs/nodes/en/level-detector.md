# Level Detector

## Overview

Measures audio peak and trailing RMS levels.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Channel | [Editor](../../user/en/reference-tables.md) | Left / Right / Mix | — | Left | — | — | Always |
| RMS window | [Editor](../../user/en/reference-tables.md) | 0.1–2000 ms | — | 50 ms | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |
| Smoothing | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Attack | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 10 ms | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always; enabled with Smoothing |
| Release | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 100 ms | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always; enabled with Smoothing |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Peak | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| RMS | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Peak = max(abs(x)) over the current block; RMS = sqrt(mean(x²)) over the trailing window. At startup RMS uses only received samples. Polyphonic voices are summed first; Mix = (L+R)/2. Optional independent attack/release smoothing follows both measurements, with one time constant reaching about 63% of a step. Zero time responds immediately. Output units are Factor, % or dB; full-scale sine RMS is approximately 0.707. There is no frequency weighting.

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

[Node index](index.md) · [Deutsch](../de/level-detector.md)
