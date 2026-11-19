# MIDI Input

## Overview

Receives MIDI from a device or the computer keyboard.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Device | [Editor](../../user/en/reference-tables.md) | Available MIDI devices | — | Keyboard | — | — | Always |
| Transpose | [Editor](../../user/en/reference-tables.md) | −48–48 st | — | 0 st | [cent](../../user/en/units.md), [st](../../user/en/units.md), [oct](../../user/en/units.md), [Factor](../../user/en/units.md) | — | Keyboard selected |
| Keyboard | [Editor](../../user/en/reference-tables.md) | Keyboard focus | — | Inactive | — | — | Keyboard selected |
| All notes off | [Editor](../../user/en/reference-tables.md) | Button | — | — | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | MIDI events | [MIDI](../../user/en/rates.md) | — |

## Behavior

Select a hardware input or Keyboard. Click the keyboard control to capture musical keys; clicking elsewhere releases that focus. At zero transposition, A plays C4. All notes off releases active notes and clears pending MIDI delays.

Linux hardware and virtual inputs use the ALSA sequencer. The system needs
`libasound.so.2` and an accessible sequencer device; unavailable/disconnected
inputs are reported without disabling the computer keyboard.

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

[Node index](index.md) · [Deutsch](../de/midi-input.md)
