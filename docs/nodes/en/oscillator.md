# Oscillator

## Overview

Generates a periodic waveform.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Waveform | [Wavetable resource](../../user/en/data-types.md#wavetable) | Sine, Triangle, Sawtooth, Square | — | Sine | — | [Control](../../user/en/rates.md#control) | Always |
| Frequency | [Value](../../user/en/data-types.md#value) | 1e-5–20 000 Hz | 0–20 000 Hz | 440 Hz | [Hz](../../user/en/units.md#frequency), [kHz](../../user/en/units.md#frequency) | [Auto](../../user/en/rates.md#auto) | Always |
| Phase | [Value](../../user/en/data-types.md#value) | ±∞ turn | ±∞ turn | 0 turn | [turn](../../user/en/units.md#angle), [°](../../user/en/units.md#angle), [rad](../../user/en/units.md#angle) | [Auto](../../user/en/rates.md#auto) | Always |
| Reset | [Trigger](../../user/en/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/en/rates.md#trigger) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

The running phase advances by `f / fs` turns per sample. Phase adds an offset without restarting the cycle; Reset restarts before the corresponding sample. With zero frequency the current phase is held, which can produce a constant output. The selected or connected Wavetable is read with frequency-dependent band limiting. Phase wraps modulo one turn. Manual Reset is delivered at the next block start.

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
| Supported |

[Node index](index.md) · [Deutsch](../de/oscillator.md)
