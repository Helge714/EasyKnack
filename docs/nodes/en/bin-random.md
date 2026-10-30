# Bin Random

## Overview

Supplies deterministic random numbers per bin and frame.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Seed | [Value](../../user/en/data-types.md#value) | 0–2 147 483 647, integer | — | 0 | — | — | Always |
| Vary by bin | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Vary by frame | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Random | [Value](../../user/en/data-types.md#value) | 0–1, excluding 1 | [Audio (bin/frame)](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Uniform values depend on Seed and the enabled bin/frame keys. Bin only gives a fixed random value per frequency. Frame only gives one shared random value per window. Both varies per bin and window; neither gives one constant random value. Equal keys also give equal values across channels. Frame means FFT window, not audio block. Only valid in [Spectral Groups](spectral-group.md).

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

[Node index](index.md) · [Deutsch](../de/bin-random.md)
