# Audio Output

## Overview

Sends audio to an output device.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Device | [Editor](../../user/en/reference-tables.md) | Available output devices | — | Default device | — | — | Always |
| Device volume | [Editor](../../user/en/reference-tables.md) | 0–100 % | — | Current Windows volume | [%](../../user/en/units.md) | — | Windows output |
| Link volume keys | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Windows output |
| Signal | [Editor](../../user/en/reference-tables.md) | Level display | — | — | — | — | Windows output |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

MIDI voices are summed automatically. Device volume controls the Windows endpoint volume and reflects external changes; it is not an additional signal gain. Linked volume keys change selected endpoints proportionally. Muting this node suppresses its EasyKnack signal without muting the Windows device.

Linked volume keys show a brief desktop overlay, including over other applications. For several linked outputs, the percentage is the highest output volume; their relative levels remain unchanged.

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

[Node index](index.md) · [Deutsch](../de/audio-output.md)
