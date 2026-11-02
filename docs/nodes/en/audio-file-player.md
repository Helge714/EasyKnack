# Audio File Player

## Overview

Plays an audio file with transport and looping controls.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| File | [Editor](../../user/en/reference-tables.md) | WAV / MP3 | — | — | — | — | Always |
| Play / Pause | [Value](../../user/en/data-types.md#value) | Off / On | 0–1 | 0 | — | [Control](../../user/en/rates.md#control) | Always |
| Reset | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Seek position | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0 | [Factor](../../user/en/units.md), [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Always |
| Speed | [Value](../../user/en/data-types.md#value) | 0–4 | 0–4 | 1 | — | [Control](../../user/en/rates.md#control) | Always |
| Loop | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Loop start | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0 | [Factor](../../user/en/units.md), [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |
| Loop end | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 1 | [Factor](../../user/en/units.md), [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Position | [Value](../../user/en/data-types.md#value) | 0–1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Duration (s) | [Value](../../user/en/data-types.md#value) | ≥ 0 s | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Buffer underruns | [Value](../../user/en/data-types.md#value) | ≥ 0 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

A background stream supplies decoded audio. Speed changes pitch and duration; 0 stops advancement. Play ≥ 0.5 enables playback. Reset starts at Loop start when looping, otherwise at the file beginning. Seek values are fractions of the whole file; seconds and samples are alternative displays of that position. The unconnected position slider follows playback and can be dragged to seek. Without Loop, playback becomes silent at the end. Buffer underruns counts blocks missing decoded data. For reusable in-memory audio use [File Buffer](file-buffer.md).

MP3 import honors available Xing/LAME encoder-delay and end-padding metadata for gapless playback. Without metadata, quiet samples are preserved rather than guessed away. Older MP3 caches are reimported in the background when their file editor is restored; if the node is inside an unopened group, open it or select the source file again. Imported duration and sample count can therefore decrease.

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

[Node index](index.md) · [Deutsch](../de/audio-file-player.md)
