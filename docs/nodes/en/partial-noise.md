# Partial Noise

## Overview

Supplies reproducible random values per tracked partial and analysis frame.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Seed | [Value](../../user/en/data-types.md#value) | 0–2 147 483 647, integer | 0–2 147 483 647 | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Vary by partial | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Vary by frame | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Random | [Value](../../user/en/data-types.md#value) | 0–1, excluding 1 | [Audio (partial/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Available only inside a Partial Group. Uniform values in [0, 1) are keyed by Seed, stable partial ID and FFT-frame position. Vary by partial separates tracks; when disabled, all partials in the same frame share one value. Vary by frame produces new values for every FFT window; when disabled, a track retains its value throughout its lifetime. With both options disabled, only Seed determines the value. Amplitude ordering, evaluation order and channel do not change the assignment. For symmetric octave modulation use `f_new = f * pow(2, strength * (2 * random - 1))`. This node does not require the Partial Context Seed output.

A connected Seed is sampled once per evaluated partial, rounded down and limited to 0–2 147 483 647. Non-finite values use the manual Seed. Without a connection the integer editor value is retained exactly. Connect a context index or a group input to vary the seed per instance.

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
| Not supported |

[Node index](index.md) · [Deutsch](../de/partial-noise.md)

[Partial Group](partial-group.md) · [Partial Context](partial-context.md)
