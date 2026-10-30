# Pitch Monitor

## Overview

Displays detected and target pitches while forwarding the target.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Pitch | [Editor](../../user/en/reference-tables.md) | Readout | — | — | — | — | Always |
| Detected [Hz] | [Value](../../user/en/data-types.md#value) | — | ≥ 0 Hz | — | [Hz](../../user/en/units.md#frequency) | [Auto](../../user/en/rates.md#auto) | Always |
| Target [Hz] | [Value](../../user/en/data-types.md#value) | — | ≥ 0 Hz | — | [Hz](../../user/en/units.md#frequency) | [Auto](../../user/en/rates.md#auto) | Always |
| Confidence | [Value](../../user/en/data-types.md#value) | — | 0–1 | — | — | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Target [Hz] | [Value](../../user/en/data-types.md#value) | ≥ 0 Hz | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

The output is Target unchanged. The readout compares detected pitch, target and confidence; it performs no pitch detection or quantization. Place it in the target path to keep the monitor part of the active graph.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Target [Hz] | Target [Hz] |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Target [Hz] | Target [Hz] |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/pitch-monitor.md)
