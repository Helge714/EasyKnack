# MIDI Note

## Overview

Converts MIDI notes into per-voice frequency, gate, velocity and triggers.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/en/data-types.md#midi) | — | MIDI events | — | — | [MIDI](../../user/en/rates.md) | Always |
| Channel (0 = all) | [Editor](../../user/en/reference-tables.md) | 0–16 | — | 0 | — | — | Always |
| Voices | [Editor](../../user/en/reference-tables.md) | 1–32 | — | 1 | — | — | Always |
| Pitch bend | [Editor](../../user/en/reference-tables.md) | 0–48 st | — | 2 st | [cent](../../user/en/units.md), [st](../../user/en/units.md), [oct](../../user/en/units.md), [Factor](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequency | [Value](../../user/en/data-types.md#value) | > 0 Hz | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Gate | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Trigger | [Trigger](../../user/en/data-types.md#trigger) | — | [Trigger](../../user/en/rates.md#trigger) | [Auto](../../user/en/data-types.md#channels) |
| Velocity | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Frequency = `440 · 2^((note − 69 + transpose + bend)/12)`. Gate is 1 while a note is held, including sustain. Velocity is note-on velocity/127 and becomes 0 on release. Frequency retains the last pitch on release. Each note-on emits a Trigger. One voice uses last-note priority with held-note fallback; polyphony assigns separate voices and steals the oldest held voice when full. Use [Voice Merge](voice-merge.md) to combine them before shared effects; an Audio Output also merges voices.

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

[Node index](index.md) · [Deutsch](../de/midi-note.md)
