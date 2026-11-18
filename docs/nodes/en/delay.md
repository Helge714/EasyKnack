# Delay

## Overview

Delays an audio signal and protects feedback cycles.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| In | [Audio](../../user/en/data-types.md#audio) | ±∞ | ±∞ | 0 | — | [Audio](../../user/en/rates.md#audio) | Always |
| Time | [Value](../../user/en/data-types.md#value) | 0–10 s | 0–10 s | 0.25 s | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Time is read once per block. Fractional-sample delays use linear interpolation. Zero bypasses outside feedback; inside feedback at least one sample is enforced. The scheduler chooses block-sized or shorter execution chunks according to the loop delay. Time modulation must originate outside that loop. Mute removes the delay boundary; an unsafe resulting connection is ignored rather than evaluated as instantaneous feedback.

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
| Supported |

[Node index](index.md) · [Deutsch](../de/delay.md)
