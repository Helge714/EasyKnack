# Curve Mapping

## Overview

Maps values through an editable curve.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Curve | [Editor](../../user/en/reference-tables.md) | Curve editor | — | Identity | — | — | Always |
| Outside range | [Editor](../../user/en/reference-tables.md) | Clamp, Linear extension | — | Clamp | — | — | Always |
| Rate | [Editor](../../user/en/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspector |
| Value | [Value](../../user/en/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/en/rates.md#auto) | Always |
| Input min | [Value](../../user/en/data-types.md#value) | ±1 000 000 | — | 0 | — | — | Always |
| Input max | [Value](../../user/en/data-types.md#value) | ±1 000 000 | — | 1 | — | — | Always |
| Output min | [Value](../../user/en/data-types.md#value) | ±1 000 000 | — | 0 | — | — | Always |
| Output max | [Value](../../user/en/data-types.md#value) | ±1 000 000 | — | 1 | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Value](../../user/en/data-types.md#value) | ±1e12 | [Auto](../../user/en/rates.md#auto) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Normalizes the input using Input min/max, samples the curve, then scales to Output min/max. Clamp holds curve endpoints outside the input domain; Linear extension continues their boundary slopes. A zero-width input domain returns Output min. Supports Bézier control points and direct use in Spectral Groups.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Value | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Value | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/curve-mapping.md)

Control points and Bezier handles appear while hovering or editing the graph. Clicking outside hides them and leaves the curve visible.

Point shapes show the following segment: circle = Bezier, square = Linear, diamond = Step. The last point uses the preceding segment.
