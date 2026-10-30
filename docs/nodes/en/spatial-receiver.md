# Spatial Receiver

## Overview

Renders a positioned source at a mono or stereo receiver.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Channels | [Editor](../../user/en/reference-tables.md) | Mono, Stereo | — | Stereo | — | — | Always |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Source Position | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 1) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Receiver Position | [Vector](../../user/en/data-types.md#vector) | ±∞ per component | ±∞ per component | (0, 0, 0) m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Yaw | [Value](../../user/en/data-types.md#value) | ±∞ ° | ±∞ ° | 0° | [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle), [turn](../../user/en/units.md#angle) | [Auto](../../user/en/rates.md#auto) | Stereo |
| Receiver Spacing | [Value](../../user/en/data-types.md#value) | 0–∞ m | 0–∞ m | 0.18 m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Stereo |
| Distance Attenuation | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |
| Reference Distance | [Value](../../user/en/data-types.md#value) | 0.001–∞ m | — | 1 m | [m](../../user/en/units.md), [km](../../user/en/units.md), [cm](../../user/en/units.md), [mm](../../user/en/units.md) | — | Inspector |
| Sound Travel Time | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Distance | [Value](../../user/en/data-types.md#value) | 0–∞ m | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Stereo input is averaged to a mono point source. Stereo receiver positions are offset by half Spacing along the rotated right axis; Distance is measured separately per side. Mono uses the center and ignores Yaw/Spacing. Attenuation is `reference/max(reference,distance)`; stereo uses constant-power directional gains. Travel time delays by `distance/343` seconds, with a maximum of 2 seconds (686 m); more distant sources are silent when enabled. Changing distance then changes pitch. Use Audio-rate smooth motion for continuous Doppler trajectories. This is free-field point-source rendering, not room acoustics, microphone directivity or HRTF simulation.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Audio In | Audio Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Audio In | Audio Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/spatial-receiver.md)
