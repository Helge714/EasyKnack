# Partial Output

## Overview

Sets the processed partial frequency, amplitude and phase.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequency | [Value](../../user/en/data-types.md#value) | 0–∞ Hz | ±∞ Hz | 440 Hz | Hz | [Auto](../../user/en/data-types.md#channels) | Always |
| Amplitude | [Value](../../user/en/data-types.md#value) | 0–∞ | ±∞ | 1 | — | [Auto](../../user/en/data-types.md#channels) | Always |
| Phase | [Value](../../user/en/data-types.md#value) | ±∞ turn | ±∞ turn | 0 turn | turn, °, rad | [Auto](../../user/en/data-types.md#channels) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Frequency, amplitude and phase define the output partial. Connect the three matching Context outputs for an unchanged pass-through. Phase uses turns. Original analysis parameters remain attached for subsequent continuous synthesis.

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

[Partial Analysis](partial-analysis.md) · [Partials to Spectrum](partials-to-spectrum.md) · [Spectrum Mix](spectrum-mix.md) · [Partial Context](partial-context.md) · [Partial Group](partial-group.md)

[Deutsch](../de/partial-output.md)
