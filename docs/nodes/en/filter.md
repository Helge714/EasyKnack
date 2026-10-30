# Filter

## Overview

Filters audio with a resonant lowpass, highpass or bandpass.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Mode | [Editor](../../user/en/reference-tables.md) | Lowpass, Highpass, Bandpass | — | Lowpass | — | — | Always |
| Order | [Editor](../../user/en/reference-tables.md) | 2, 4, 6, 8 | — | 2 | — | — | Always |
| Level compensation | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Cutoff | [Value](../../user/en/data-types.md#value) | 5–20 000 Hz | 5 Hz–0.45 × fs | 1000 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | [Audio](../../user/en/rates.md#audio) | Always |
| Resonance | [Value](../../user/en/data-types.md#value) | 0.1–30 Q | 0.1–30 Q | 0.707 Q | [Q](../../user/en/units.md) | [Audio](../../user/en/rates.md#audio) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Cascades second-order sections to obtain the selected order. Cutoff and resonance are smoothed; resonance is distributed across sections for higher orders. Level compensation follows input/output RMS with gain bounded to 0.25–4×. It adds no lookahead latency, is not a limiter and cannot recover removed spectral content.

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

[Node index](index.md) · [Deutsch](../de/filter.md)
