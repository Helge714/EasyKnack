# File Buffer

## Overview

Loads a selected audio-file region into a buffer resource.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| File | [Editor](../../user/en/reference-tables.md) | WAV / MP3 | — | — | — | — | Always |
| Snap to zero | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Waveform | [Editor](../../user/en/reference-tables.md) | Region display | — | — | — | — | Always |
| Start | [Editor](../../user/en/reference-tables.md) | 0–End − 1 smp | — | 0 s | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |
| End | [Editor](../../user/en/reference-tables.md) | Start + 1 smp–file duration | — | File duration | [s](../../user/en/units.md), [ms](../../user/en/units.md), [smp](../../user/en/units.md) | — | Always |
| Memory | [Editor](../../user/en/reference-tables.md) | Memory display | — | — | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Buffer | [Buffer resource](../../user/en/data-types.md#buffer) | Loaded samples | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Duration (s) | [Value](../../user/en/data-types.md#value) | > 0 s | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Samples | [Value](../../user/en/data-types.md#value) | ≥ 1 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Only the selected region is retained as playable audio. Start/End are fixed-range sliders with at least one sample between them. Waveform handles edit the same region; optional zero snapping helps choose boundaries. Duration and Samples describe the loaded region, with Samples counting frames per channel. The buffer is immutable; its end acts as write head. Both [Speed](buffer-reader-speed.md) and [Position](buffer-reader-position.md) readers can play it. Changing the region replaces the buffer and resets readers.

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

[Node index](index.md) · [Deutsch](../de/file-buffer.md)
