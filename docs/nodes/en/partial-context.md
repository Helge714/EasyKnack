# Partial Context

## Overview

Provides the current partial values inside a Partial Group.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequency | [Value](../../user/en/data-types.md#value) | 0–Nyquist Hz | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Amplitude | [Value](../../user/en/data-types.md#value) | 0–∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Phase | [Value](../../user/en/data-types.md#value) | ±∞ turn | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| ID | [Value](../../user/en/data-types.md#value) | 1–∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Age | [Value](../../user/en/data-types.md#value) | 0–∞ s | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Index | [Value](../../user/en/data-types.md#value) | 0–32767 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Frame time | [Value](../../user/en/data-types.md#value) | 0–∞ s | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Seed | [Value](../../user/en/data-types.md#value) | 0–16 777 215 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Tonality | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Values describe the current partial in the current frame. Phase uses turns; Index is the current amplitude ordering, ID is the tracked identity. Age and frame time use seconds. Partial Group state follows the ID.

ID remains the track identifier. Seed is a derived 24-bit value exactly representable as float for ordinary Noise seed inputs; it is not a unique identifier. Partial Noise uses the full track ID directly and needs no such connection.

Tonality is Loris’ tonal energy fraction `1 − bandwidth`, from 0 (entirely noisy) to 1 (entirely sinusoidal). It is not a classification probability. With Bandwidth model None it is 1.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| No | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partial Analysis](partial-analysis.md) · [Partials to Spectrum](partials-to-spectrum.md) · [Spectrum Mix](spectrum-mix.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/partial-context.md)
