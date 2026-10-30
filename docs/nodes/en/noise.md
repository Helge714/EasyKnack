# Noise

## Overview

Generates seeded noise with a selectable spectral color.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Seed | [Value](../../user/en/data-types.md#value) | −1–16 777 215 | −1–16 777 215, rounded | −1 | — | [Control](../../user/en/rates.md#control) | Always |
| Noise | [Editor](../../user/en/reference-tables.md) | White, Pink, Brown, Blue | — | White | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Audio, Control | — | Audio | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md#audio) | ±1 | [Audio / Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

White is spectrally flat in power per Hz; Pink emphasizes low frequencies, Brown integrates a white sequence, and Blue emphasizes changes. Seed −1 uses the logical node's automatic seed. Channel/instance copies share it; drive Seed from context to obtain independent sequences. Changing Seed restarts the sequence. Control produces one held random value per block.

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

[Node index](index.md) · [Deutsch](../de/noise.md)
