# Delay Allpass

## Overview

Diffuses audio through a delay-based allpass.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Delay | [Value](../../user/en/data-types.md#value) | 1/48000–1 s | 1/fs–1 s | 10 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Always |
| Feedback | [Value](../../user/en/data-types.md#value) | ±0.99 | ±0.99 | 0.5 | — | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

For integer delay D and feedback g, `y[n] = v[n − D] − g × x[n]` and `v[n] = x[n] + g × y[n]`. This is a unity-magnitude allpass for constant parameters, not the approximate allpass used inside classic Freeverb. Delay rounds to the nearest sample, at least one sample, and is sampled once per block. Delay changes can click. Feedback is bounded to ±0.99. This node has an instantaneous feed-through term and does not secure an external feedback cycle; use a Delay node as the cycle boundary.

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

[Node index](index.md) · [Deutsch](../de/delay-allpass.md)
