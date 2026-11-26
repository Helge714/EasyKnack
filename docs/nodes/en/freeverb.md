# Freeverb

## Overview

Produces stereo reverberation with adjustable room size, damping and width.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Room Size | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Damping | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Width | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 1 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Wet | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 1/3 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Dry | [Value](../../user/en/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Factor](../../user/en/units.md), [%](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Stereo](../../user/en/data-types.md#channels) |

## Behavior

Eight parallel damped comb filters followed by four serial allpasses per channel produce the reverberation. Delay times scale with the sample rate. Both banks receive the excitation `0.015 × (L + R)`.

Room Size sets feedback to `0.7 + 0.28 × Room Size`; Damping sets the lowpass coefficient to `0.4 × Damping`. Width 0 produces a mono wet signal; Width 1 retains the separate stereo banks. The dry signal retains its original channels.

Wet gain is `3 × Wet`; dry gain is `2 × Dry`, so Dry = 0.5 passes the dry signal at unity gain. Connected parameters are clamped to 0–1 and evaluated at their selected rate.

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

[Node index](index.md) · [Deutsch](../de/freeverb.md)
