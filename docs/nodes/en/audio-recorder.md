# Audio Recorder

## Overview

Records triggered audio takes as WAV or MP3 files.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Recording file | [Editor](../../user/en/reference-tables.md) | WAV / MP3 target | — | None | — | — | Always |
| Recording status | [Editor](../../user/en/reference-tables.md) | Ready / Recording / Saved / Error | — | Ready | s | — | Always |
| Channels | [Editor](../../user/en/reference-tables.md) | Mono / Stereo | — | Stereo | — | — | Always |
| Overwrite | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Index digits | [Editor](../../user/en/reference-tables.md) | 1–12 | — | 4 | — | — | Inspector; disabled with Overwrite |
| Save recording as | [Editor](../../user/en/reference-tables.md) | Save dialog | — | Disabled | — | — | Inspector; completed take required |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | 0 | — | [Audio](../../user/en/rates.md#audio) | Always |
| Start | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Stop | [Trigger](../../user/en/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

Start includes the triggering sample; Stop ends the take before that sample. Repeated Start triggers during recording are ignored. Stop takes precedence when both arrive together. The audio host must run. Stopping the host or removing the node finalizes the take.

WAV stores 32-bit floating-point audio at the host sample rate. Stereo preserves both channels; Mono averages left and right. Polyphonic voices are summed. Without Overwrite, `Take.wav` produces `Take_0001.wav`, then the next free index. Index digits specifies a minimum width: larger indices remain valid. Overwrite replaces the selected file on each new take.

A background thread writes a bounded queue. If the disk cannot keep up, recording stops with an error; accepted audio is finalized as a partial take. Status shows duration and the output path in its tooltip. File/open, queue-overflow, write and WAV size-limit errors are distinguished. A WAV take is limited to the standard WAV/RIFF size (approximately 4 GiB); MP3 requires an available Windows media encoder. MP3 clips input to ±1 before encoding and includes codec delay/padding; use WAV for sample-accurate timing. Choose the format in the file dialog or by the target extension. WAV remains the default.

Save recording as exports a completed take in its recorded format without ending another take or altering the automatic recording target. It is disabled until recorded audio has been finalized. Changing recording target or format/channel settings closes the current take.

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

[Node index](index.md) · [Deutsch](../de/audio-recorder.md)
