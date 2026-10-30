# Smoothing

## Overview

Smooths rising and falling values exponentially.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Audio | — | — | Inspector |
| In | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Rise | [Value](../../user/en/data-types.md) | 0–60 s | 0–60 s | 0.03 s | s, ms, smp | [Auto](../../user/en/rates.md) | Always |
| Fall | [Value](../../user/en/data-types.md) | 0–60 s | 0–60 s | 0.03 s | s, ms, smp | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md) | ±∞ | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

For time constant τ, `y += (1 − exp(−dt/τ)) × (x − y)`, using Rise when increasing and Fall when decreasing. One τ reaches about 63% of a step, three τ about 95%. Zero follows immediately. The first valid value initializes directly. Audio output smooths Control steps samplewise; Control output advances once per block. This is not a fixed-duration linear ramp.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | In | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| In | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/smoothing.md)
