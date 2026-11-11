# Hilbert

## Overview

Produces a delayed direct signal and its Hilbert quadrature component.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio In | [Audio](../../user/en/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/en/rates.md#audio) | Always |
| Filter length | [Editor](../../user/en/reference-tables.md) | 61, 129, 257, 513, 1025, 2049 | — | 257 | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Direct | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Quadrature | [Audio](../../user/en/data-types.md#audio) | ±∞ | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

A Hamming-windowed FIR with selectable length N approximates a −90° phase shift for positive frequencies. Both outputs have a group delay of `(N - 1) / 2` samples (2.67 ms for N = 257 at 48 kHz). Start-up history is zero; channel and voice states are independent.

The approximation loses accuracy near DC and Nyquist; Quadrature rejects DC and the Nyquist component. For N = 257 at 48 kHz, leave approximately 400 Hz of transition bandwidth at both ends for accurate quadrature; these limits scale with sample rate. Longer filters narrow the transition regions but increase CPU cost and latency. Changing length prepares a new empty filter outside the audio thread. Length has no signal input. Outputs are not amplitude-limited.

For an upward frequency shift, compute `Direct * cos(phase) - Quadrature * sin(phase)` with a shared carrier phase. Addition selects the downward shift. Mute forwards the input to Direct without delay and silences Quadrature.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Audio In | Direct |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Audio In | Direct |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Supported |

[Node index](index.md) · [Deutsch](../de/hilbert.md)
