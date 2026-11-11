# Spectral Group

## Overview

Processes each bin of an incoming spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Frame inputs | [Value](../../user/en/data-types.md#value) | Interface-defined | Audio / Control / Trigger | Interface-defined | — | [Auto](../../user/en/rates.md#auto) | Always |
| Skip empty bins | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Inspector |
| Parallel bins | [Editor](../../user/en/reference-tables.md) | Off / On | — | On | — | — | Inspector |
| General / IO / Editors | [Editor](../../user/en/reference-tables.md) | Group-defined | — | — | — | — | Inspector |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Out | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

One Spectrum sets bin count and frame timing. [Bin Context](bin-context.md) exposes complex components, frequency and index; [Bin Reader](bin-reader.md) reads other bins. [Bin Output](bin-output.md) writes independent complex outputs; the default wiring is unchanged complex passthrough. External frame values are sampled at the FFT window end; asynchronous paths retain their time alignment. Frame triggers apply to the corresponding frame. Frame-only processing evaluates once per bin/frame, not per audio sample. Oscillators/noise create persistent audio per bin, reduced through Instance Merge; Bin Output cannot accept that audio. Bin Discard is scheduled before bin work and affects all Spectrum outputs. Parallel workers preserve independent bin state. [IFFT](ifft.md) reconstructs complex spectra; an oscillator bank is not phase-equivalent IFFT.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Spectrum | Out |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Spectrum | Out |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/spectral-group.md)
