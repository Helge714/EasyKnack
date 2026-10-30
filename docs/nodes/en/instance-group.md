# Instance Group

## Overview

Runs an internal graph for multiple independent instances.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Count | [Value](../../user/en/data-types.md) | 0–256, integer | 0–Maximum, rounded down | 8 | — | [Control](../../user/en/rates.md) | Always |
| Frequency | [Value](../../user/en/data-types.md) | ±∞ | ±∞ | 220 | — | [Auto](../../user/en/rates.md) | Always |
| Maximum | [Value](../../user/en/data-types.md) | 1–256 | — | 16 | — | — | Inspector |
| General / IO / Editors | [Editor](../../user/en/reference-tables.md) | Group-defined | — | — | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Audio](../../user/en/data-types.md) | ±∞ | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

The initial graph creates harmonic oscillators and an [Instance Merge](instance-merge.md); Frequency is its initial public parameter, not a mandatory permanent port. Each instance has independent DSP state. [Instance Context](instance-context.md) supplies Index, Count, Position and Seed. Maximum reserves state/memory; Count selects active instances and is not an ordinary editable Group-IO port. Multiple merge branches and shared downstream processing are allowed. Nested instance/spectral groups are supported. Public inputs accept numeric, Vector, Wavetable and Spectrum; public outputs currently accept numeric or Spectrum. MIDI and other resource boundaries remain unsupported. Identical generators can share work until their context or history differs.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Configured group input | Configured group output |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Configured group input | Configured group output |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/instance-group.md)
