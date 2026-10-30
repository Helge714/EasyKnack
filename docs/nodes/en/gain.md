# Gain

## Overview

Scales an audio signal by an amplitude factor.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Gain | [Value](../../user/en/data-types.md) | 0–1.995262 × | 0–1e6 × | 0.251189 × | ×, %, dB | [Auto](../../user/en/rates.md) | Always |
| Audio In | [Audio](../../user/en/data-types.md) | — | ±∞ | — | — | [Audio](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md) | ±∞ | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

`y = x × gain`, with `gain = 10^(dB/20)`. Manual −12 dB corresponds to about 0.251189×. Values at or below −160 dB mute the signal. Connected gain is limited to 120 dB; manual changes are smoothed. Gain does not limit the audio output.

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

[Node index](index.md) · [Deutsch](../de/gain.md)
