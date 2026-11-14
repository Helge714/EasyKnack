# Envelope

## Overview

Generates a curve-shaped envelope from Gate and Trigger.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Envelope | [Editor](../../user/en/reference-tables.md) | Curve editor | — | ADSR | — | — | Always |
| Gate | [Value](../../user/en/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Audio](../../user/en/rates.md#audio) | Always |
| Trigger | [Trigger](../../user/en/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |
| Attack | [Value](../../user/en/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 120 ms | [ms](../../user/en/units.md), [s](../../user/en/units.md), [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Decay | [Value](../../user/en/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 180 ms | [ms](../../user/en/units.md), [s](../../user/en/units.md), [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Release | [Value](../../user/en/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 300 ms | [ms](../../user/en/units.md), [s](../../user/en/units.md), [smp](../../user/en/units.md) | [Auto](../../user/en/rates.md#auto) | Always |
| Hold | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Envelope | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

A Gate rise above 0 or a Trigger starts Attack, then Decay. Each phase has its own duration input (0–60,000 ms). With Hold on, a positive Gate holds the level at the end of Decay; Gate returning to 0 starts Release from the current level, including during Attack or Decay. With Hold off or a Trigger without Gate, Release follows Decay automatically. Zero-duration phases finish immediately. Retriggers start from the current level; output stays within 0–1.

Curve points move vertically; their horizontal positions and count stay fixed. The first interior point ends Attack, the last interior point ends Decay; Bezier handles still shape the transitions. Points and handles appear while hovering or editing and disappear when focus moves outside. Existing curves are adapted phase by phase to the display.

Older combined timing is split at the curve's first interior point. Existing duration connections now control Attack. Sustain position and its connections/copied bindings are retired; Hold uses the end of Decay. This can change older patches with modulated timing or a displaced hold position.

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

[Node index](index.md) · [Deutsch](../de/envelope.md)

Point shapes show the following segment: circle = Bezier, square = Linear, diamond = Step. The last point uses the preceding segment.

Attack, Decay and Release each occupy one third of the edge-aligned graph. Their actual durations come from the three duration inputs. There is no extra Hold marker.
