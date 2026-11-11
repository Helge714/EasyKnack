# MIDI Transpose

## Overview

MIDI Transpose processes MIDI events.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | — | MIDI events | — | — | [MIDI](../../user/en/rates.md) | Always |
| Transpose | [Editor](../../user/en/reference-tables.md) | −48–48 st | — | 0 st | [cent](../../user/en/units.md), [st](../../user/en/units.md), [oct](../../user/en/units.md), [Factor](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | MIDI events | [MIDI](../../user/en/rates.md) | — |

## Behavior

Adds a pitch offset to new notes, including fractional semitones. Existing held notes retain their original offset; note-off matching is preserved.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | MIDI | MIDI |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| MIDI | MIDI |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/midi-transpose.md)
