# Audio Input

## Overview

Captures audio from a selected source.

On macOS 14.2 or newer, Core Audio provides hardware input and system/application
taps. Grant Microphone or Audio Capture permission when requested, then refresh
devices. System capture excludes EasyKnack's audio host. Copy does not mute the
source application; exclusive routing requires external tools. Hardware channel
pairs are selectable; stored device IDs from other operating systems need changing.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Source | [Editor](../../user/en/reference-tables.md) | Available capture sources | — | System mix | — | — | Always |
| Mode | [Editor](../../user/en/reference-tables.md) | Copy | — | Copy | — | — | System mix or application |
| Channels | [Editor](../../user/en/reference-tables.md) | Mono / Stereo | — | Stereo | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Capture can use a device, the system mix or an application. Copy captures the signal without suppressing the original playback. For exclusive rerouting, select a virtual routing device and route the source application to that device in Windows. Mono combines channels; Stereo preserves left and right.

Linux uses PulseAudio or PipeWire-Pulse for device, sink-monitor and per-application
capture. Applications appear while they expose an audio stream; EasyKnack's own
streams are excluded. Exclusive rerouting remains external, through the desktop's
audio routing tools. Saved Windows device IDs must be reselected on Linux.
Multichannel sources and monitors expose separate channel pairs; mono hardware
is duplicated for stereo processing or retained by the node's Mono option.

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

[Node index](index.md) · [Deutsch](../de/audio-input.md)
