# Digital Noise

## Overview

Generates cycle-based random frequency modulation.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequency | [Value](../../user/en/data-types.md) | 1e-5–20 000 Hz | 0–20 000 Hz | 440 Hz | Hz, kHz | [Auto](../../user/en/rates.md) | Always |
| Strength | [Value](../../user/en/data-types.md) | 0–1 | 0–1 | 0 | — | [Auto](../../user/en/rates.md) | Always |
| Distribution | [Editor](../../user/en/reference-tables.md) | Uniform, Gaussian | — | Uniform | — | — | Always |
| Seed | [Value](../../user/en/data-types.md) | −1–16 777 215 | −1–16 777 215, rounded | −1 | — | [Control](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Factor | [Value](../../user/en/data-types.md) | 2^(−s)–2^s | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Trigger | [Trigger](../../user/en/data-types.md) | — | [Trigger](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |
| Frequency | [Value](../../user/en/data-types.md) | f × Factor | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Factor is held for a self-modulated cycle; Frequency outputs `f × Factor`. Strength `s` bounds octave deviation to `±s`. Gaussian uses standard deviation `s/3`, truncated at `±s`. Drawing compensates for cycle duration so time-weighted octave deviations are symmetric. This does not preserve the arithmetic mean in Hz. Strength 0 gives Factor 1 and passes Frequency unchanged. Trigger marks a new draw. Seed follows [Noise](noise.md).

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
| Supported |

[Node index](index.md) · [Deutsch](../de/digital-noise.md)
