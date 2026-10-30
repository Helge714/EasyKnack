# Spectrum Blur

## Overview

Spreads spectral content across neighboring frequencies.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Distribution | [Editor](../../user/en/reference-tables.md) | Bins / Octaves / Factor (legacy) | — | Octaves | — | — | Always |
| Kernel | [Editor](../../user/en/reference-tables.md) | Gaussian / Box / Maximum | — | Gaussian | — | — | Always |
| Synthesis | [Editor](../../user/en/reference-tables.md) | Noise / Continuous / Preserve | — | Noise | — | — | Always |
| Normalize power | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Octaves | [Value](../../user/en/data-types.md#value) | ≥ 0 oct | ≥ 0 oct | 0 oct | [oct](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Octaves |
| Factor | [Value](../../user/en/data-types.md#value) | ≥ 1 | ≥ 1 | 1 | — | [Control](../../user/en/rates.md#control) | Factor (legacy) |
| Width (bins) | [Value](../../user/en/data-types.md#value) | 0–8192 | 0–8192 | 0 | bins | [Control](../../user/en/rates.md#control) | Bins |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | Complex FFT bins | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Octaves defines a logarithmic radius: width b covers `f·2^(−b)` to `f·2^b`; Factor q covers f/q to f·q. DC is unchanged in logarithmic modes. Bins uses a linear bin radius. Gaussian uses a three-standard-deviation radius; Box weights the neighborhood uniformly; Maximum takes its largest magnitude. Noise synthesizes continuous noise across frequency regions, Continuous uses bin-centered tones, and Preserve retains coefficient phase. Zero width is an exact bypass. At very small noise widths, phase deviation is limited for a continuous transition. Normalize power preserves total spectral power, in Noise mode as an expectation rather than a per-frame limiter. Controls are evaluated per incoming frame.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| Yes | Spectrum | Spectrum |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| Spectrum | Spectrum |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Node index](index.md) · [Deutsch](../de/spectrum-blur.md)
