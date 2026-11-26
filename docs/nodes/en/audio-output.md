# Audio Output

## Overview

Sends audio to an output device.

macOS uses Core Audio, including selectable hardware channel pairs and multiple
devices. Volume controls are available only where the device exposes hardware
volume. Linked controls follow desktop default-volume changes proportionally;
they do not intercept global keys. Windows ASIO drivers are not used on macOS.

Linux uses PulseAudio or PipeWire-Pulse for device selection and volume controls.
Without that service, the fallback supports only the default audio devices.
Multichannel Linux devices expose separate channel pairs (1/2, 3/4, etc.); a final
single channel uses mono conversion. Device volume applies to the physical sink.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Device | [Editor](../../user/en/reference-tables.md) | Available output devices | — | Default device | — | — | Always |
| Device volume | [Editor](../../user/en/reference-tables.md) | 0–100 % | — | Current device volume | [%](../../user/en/units.md) | — | Windows, Linux or macOS output |
| Link volume keys | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Windows, Linux or macOS output |
| Signal | [Editor](../../user/en/reference-tables.md) | Level display | — | — | — | — | Windows, Linux or macOS output |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Behavior

MIDI voices are summed automatically. Device volume controls the Windows endpoint volume and reflects external changes; it is not an additional signal gain. Linked volume keys change selected endpoints proportionally. Muting this node suppresses its EasyKnack signal without muting the Windows device.

On Linux, device volume controls the Pulse/PipeWire sink. Linked keys follow changes
to the desktop's default volume, including changes in the desktop volume menu,
and preserve the selected sinks' relative levels. This uses desktop volume events
rather than Windows' global keyboard hook. ASIO endpoints remain Windows-only.

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
