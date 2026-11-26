# Limiter

## Overview

Limits stereo sample peaks to an adjustable ceiling.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Ceiling | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0.98 | [Factor](../../user/en/units.md) / [%](../../user/en/units.md) / [dB](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Release | [Value](../../user/en/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Lookahead | [Editor](../../user/en/reference-tables.md) | 0–20 ms | — | 5 ms | [ms](../../user/en/units.md) / [s](../../user/en/units.md) / [smp](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±Ceiling | [Audio](../../user/en/rates.md#audio) | [Stereo](../../user/en/data-types.md#channels) |

## Behavior

Both channels share gain derived from the larger absolute sample. Ceiling and Release use the left-channel parameter values. A sliding maximum covers the lookahead window; gain is reduced immediately when required and recovers exponentially over Release. Lookahead delays both channels by `round(ms × fs / 1000)` samples; zero adds no delay. Changing Lookahead clears the delay history. Mute bypasses the node and therefore removes its latency and limiting. Align parallel dry paths manually when needed. The ceiling applies to digital samples, including during parameter changes; this is not an oversampled true-peak limiter and cannot guarantee reconstructed inter-sample peaks. Ceiling 0 outputs silence.

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

[Node index](index.md) · [Deutsch](../de/limiter.md)
