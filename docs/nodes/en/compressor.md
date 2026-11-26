# Compressor

## Overview

Reduces dynamic range using a feed-forward peak compressor.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Sidechain | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Threshold | [Value](../../user/en/data-types.md#value) | −120–24 dB | −120–24 dB | −12 dB | [Factor](../../user/en/units.md) / [%](../../user/en/units.md) / [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Ratio | [Value](../../user/en/data-types.md#value) | 1–100 | 1–100 | 4 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Knee | [Value](../../user/en/data-types.md#value) | 0–48 dB | 0–48 dB | 6 dB | [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Attack | [Value](../../user/en/data-types.md#value) | 0–2000 ms | 0–2000 ms | 10 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Release | [Value](../../user/en/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Make-up Gain | [Value](../../user/en/data-types.md#value) | −60–24 dB | −60–24 dB | 0 dB | [Factor](../../user/en/units.md) / [%](../../user/en/units.md) / [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Stereo Link | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Stereo](../../user/en/data-types.md#channels) |

## Behavior

Uses the Dynamics Curve soft-knee characteristic and smooths gain reduction in dB with exponential Attack and Release. Attack applies when more attenuation is needed; Release when attenuation recovers. Zero is instantaneous. Times are one-pole time constants. An unconnected Sidechain detects the main input; a connected Sidechain controls gain without being mixed into the output. Stereo Link uses the larger absolute detector level and the left-channel parameter values for identical gain on both channels. With linking disabled, detection and parameters are channel-independent. Make-up Gain is a manual output gain; there is no automatic loudness matching. The compressor adds no lookahead latency and does not guarantee a peak ceiling.

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
| Not supported |

[Node index](index.md) · [Deutsch](../de/compressor.md)
