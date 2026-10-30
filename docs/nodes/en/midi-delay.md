# MIDI Delay

## Overview

MIDI Delay processes MIDI events.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | — | MIDI events | — | — | [MIDI](../../user/en/rates.md) | Always |
| Delay | [Editor](../../user/en/reference-tables.md) | 0–10000 ms | — | 100 ms | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | MIDI events | [MIDI](../../user/en/rates.md) | — |

## Behavior

Delays notes and controller messages together. A delay change affects newly arriving events; queued events keep their due time. All notes off clears the queue.

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

[Node index](index.md) · [Deutsch](../de/midi-delay.md)
