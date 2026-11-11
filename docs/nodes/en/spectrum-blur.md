# Spectrum Blur

## Overview

Spreads spectral content across neighboring frequencies.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | — | Complex FFT bins | — | — | [Control](../../user/en/rates.md#control) | Always |
| Kernel | [Editor](../../user/en/reference-tables.md) | Gaussian / Box / Maximum | — | Gaussian | — | — | Always |
| Synthesis | [Editor](../../user/en/reference-tables.md) | Noise / Continuous / Preserve | — | Noise | — | — | Always |
| Normalize power | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Width | [Value](../../user/en/data-types.md#value) | ≥ 0 oct / bins | ≥ 0 oct / bins | 0 oct | [oct / cent / st / bins](../../user/en/units.md) | [Control](../../user/en/rates.md#control) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spectrum | [Spectrum resource](../../user/en/data-types.md#spectrum) | Complex FFT bins | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Width uses the unit selected from its slider or port context menu. Octaves, cent and semitones define the same logarithmic radius: width b in octaves covers `f·2^(−b)` to `f·2^b`; 1 oct = 1200 cent = 12 st. Bins selects a linear bin radius. Switching between Bins and pitch units retains the stored numeric radius and changes its interpretation; there is no universal conversion between them. DC is unchanged in logarithmic modes. Gaussian uses a three-standard-deviation radius; Box weights the neighborhood uniformly; Maximum takes its largest magnitude. Noise synthesizes continuous noise across frequency regions, Continuous uses bin-centered tones, and Preserve retains coefficient phase. Zero width is an exact bypass. At very small noise widths, phase deviation is limited for a continuous transition. Normalize power preserves total spectral power, in Noise mode as an expectation rather than a per-frame limiter. Controls are evaluated per incoming frame.

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
