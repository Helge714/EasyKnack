# Allpass

## Overview

Changes frequency-dependent phase while retaining the steady-state amplitude response.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Order | [Editor](../../user/en/reference-tables.md) | 1 / 2 | — | 1 | — | — | Always |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Frequency | [Value](../../user/en/data-types.md#value) | 1–20000 Hz | 1 Hz–0.49 × fs | 1000 Hz | [Hz](../../user/en/units.md) / [kHz](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Resonance | [Value](../../user/en/data-types.md#value) | 0.1–30 Q | 0.1–30 Q | 0.70710678 Q | [Q](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Order = 2 |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Order 1 uses a bilinear first-order allpass with `a = (tan(πf / fs) − 1) / (tan(πf / fs) + 1)`. Order 2 uses the RBJ biquad allpass with adjustable Q. With constant parameters, magnitude is unity at every frequency. Mixing with the dry signal creates phase cancellations; cascading sections supports phaser networks. Changing Order resets filter state. Audio-rate modulation is supported, but time-varying parameters need not preserve instantaneous amplitude.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Audio In | Audio Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Audio In | Audio Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/allpass.md)
