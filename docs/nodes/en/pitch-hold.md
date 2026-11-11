# Pitch Hold

## Overview

Experimental: enable **Settings → Enable experimental features** to add this node or open dependent examples from the Examples menu. Pitch detection/acceptance is not yet reliable in all tested cases. Existing projects keep their nodes when the setting is off.

Holds the last sufficiently confident frequency.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequency | [Value](../../user/en/data-types.md#value) | 0–20000 Hz | ≥ 0 Hz | 440 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | [Auto](../../user/en/rates.md#auto) | Always |
| Confidence | [Value](../../user/en/data-types.md#value) | 0–1 | ±∞ | 1 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Minimum confidence | [Value](../../user/en/data-types.md#value) | 0–1 | ±∞ | 0.8 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto / Audio / Control | — | Auto | — | — | Inspector |
| Initial frequency | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 440 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequency [Hz] | [Value](../../user/en/data-types.md#value) | > 0 Hz | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |
| Valid | [Value](../../user/en/data-types.md#value) | 0–1 | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Accepts positive finite Frequency when Confidence meets Minimum confidence. Otherwise the previous accepted value is held; before the first accepted value it uses Initial frequency. Valid describes the current input, even while output is held. No automatic fade or smoothing is applied.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Frequency | Frequency [Hz] |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Frequency | Frequency [Hz] |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/pitch-hold.md)
