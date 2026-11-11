# Rate Converter

## Overview

Converts numeric signals between Audio and Control.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| In | [Value](../../user/en/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Audio](../../user/en/rates.md#audio) | Always |
| Output Rate | [Editor](../../user/en/reference-tables.md) | Control, Audio | — | Control | — | — | Always |
| Reduction | [Editor](../../user/en/reference-tables.md) | First, Last, Mean, RMS, Minimum, Maximum, Absolute peak | — | First | — | — | Control output |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md#value) | ±∞ | [Audio / Control](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Audio preserves samples. Control reduces each actual block using the selected method and holds that result. Mean is signed; RMS and Absolute peak are nonnegative. Control input is held when upsampling; no intermediate values are invented. Trigger conversion is handled by separate nodes.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | In | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| In | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/rate-converter.md)
