# Buffer Context

## Overview

Reads the size and write position of a Buffer resource.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Buffer | [Buffer resource](../../user/en/data-types.md#buffer) | — | Buffer resource | — | — | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Size | [Value](../../user/en/data-types.md#value) | 0–+∞ smp | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Write position | [Value](../../user/en/data-types.md#value) | 0–1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Size is the allocated number of samples per channel, including unwritten recording capacity. For File Buffer it is the loaded start/end selection. The default unit is smp; seconds and milliseconds are available through the output unit menu.

Write position is the normalized next-write location. Recording buffers advance sample by sample and wrap to 0, matching the Buffer Readers. File buffers report 1 (end of file). Empty or unconnected resources produce 0 at both outputs.

Old Recording Buffer Write position connections are automatically moved onto Buffer Context when loading, including exposed group outputs. Loading a migrated file displays a notice; saving retains the new routing.

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

[Node index](index.md) · [Deutsch](../de/buffer-context.md)
