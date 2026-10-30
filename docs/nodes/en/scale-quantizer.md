# Scale Quantizer

## Overview

Rounds frequencies to an equal-tempered scale.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Mode | [Editor](../../user/en/reference-tables.md) | Scale, Fixed | — | Scale | — | — | Always |
| Root | [Editor](../../user/en/reference-tables.md) | C, C#, D, D#, E, F, F#, G, G#, A, A#, B | — | C | — | — | Always |
| Scale | [Editor](../../user/en/reference-tables.md) | Chromatic, Major, Minor, Major pentatonic, Minor pentatonic | — | Chromatic | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| Frequency | [Value](../../user/en/data-types.md) | 0–20 000 Hz | Positive Hz | 440 Hz | Hz, kHz | [Auto](../../user/en/rates.md) | Always |
| Fixed target | [Value](../../user/en/data-types.md) | 1–20 000 Hz | Positive Hz | 440 Hz | Hz, kHz | [Auto](../../user/en/rates.md) | Always |
| A4 tuning | [Value](../../user/en/data-types.md) | 400–480 Hz | — | 440 Hz | Hz, kHz | — | Inspector |
| Hysteresis | [Value](../../user/en/data-types.md) | 0–12 st | 0–12 st | 0.1 st | st, cent | [Auto](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Target | [Value](../../user/en/data-types.md) | 0–∞ Hz | [Auto](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Scale maps `69 + 12 log2(f / A4)` to the nearest allowed note, then converts back to Hz. Exact ties select the lower note. Hysteresis adds a switching margin beyond the midpoint; 0 switches immediately. Nonpositive/invalid input gives 0. Chromatic ignores Root. Fixed uses Fixed target instead of the scale. An inclusive octave interval can contain 13 output notes: twelve semitone intervals have thirteen endpoints.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Frequency | Target |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Frequency | Target |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/scale-quantizer.md)
