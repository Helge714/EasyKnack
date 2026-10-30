# Envelope

## Overview

Generates a curve-shaped envelope from Gate and Trigger.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Envelope | [Editor](../../user/en/reference-tables.md) | Curve editor | — | ADSR | — | — | Always |
| Gate | [Value](../../user/en/data-types.md) | 0–1 | ±∞ | 0 | — | [Audio](../../user/en/rates.md) | Always |
| Trigger | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |
| Attack + Decay | [Value](../../user/en/data-types.md) | 0–60 000 ms | 0–60 000 ms | 300 ms | ms, s, smp | [Auto](../../user/en/rates.md) | Always |
| Release | [Value](../../user/en/data-types.md) | 0–60 000 ms | 0–60 000 ms | 300 ms | ms, s, smp | [Auto](../../user/en/rates.md) | Always |
| Sustain position | [Value](../../user/en/data-types.md) | 0.01–0.99 | 0.01–0.99 | 0.5 | — | [Auto](../../user/en/rates.md) | Always |
| Hold | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Envelope | [Value](../../user/en/data-types.md) | 0–1 | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

A Gate rise above 0 or a Trigger starts Attack/Decay. Sustain position divides the curve into attack/decay and release portions. With Hold on, a positive Gate holds the sustain value; releasing Gate starts Release from the current level. With Hold off, Release follows Attack/Decay automatically. Retriggers start from the current envelope level. The result is limited to 0–1.

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
