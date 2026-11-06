# Partial Group

## Overview

Processes each detected partial once per analysis frame.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Default routing: `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Math (Add) → IFFT`; connect the analysis residual to the second Math input. Inside, use `Partial Context → Math / Formula Group → Partial Output`. Frequency, amplitude and phase remain independent values. State is isolated by ID and reset for new IDs. Up to 64 numeric processors; ordinary nested groups and numeric Formula Groups are flattened. No audio oscillator bank, buffers or trigger programs. External numeric values are sampled at window end from the current publication block; asynchronous historical modulation values are not buffered.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | [Partials](../../user/en/data-types.md#partials) | [Partials](../../user/en/data-types.md#partials) |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partial Analysis](partial-analysis.md) · [Partials to Spectrum](partials-to-spectrum.md) · [Spectrum Math](spectrum-math.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md)

[Deutsch](../de/partial-group.md)
