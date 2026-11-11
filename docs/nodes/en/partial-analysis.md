# Partial Analysis

## Overview

Separates a spectrum into tracked partials and a complex residual spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Resolution | [Editor](../../user/en/reference-tables.md) | 0–20000 Hz | — | 0 Hz | Hz, kHz | — | Always |
| Threshold | [Editor](../../user/en/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Always |
| Minimum track duration | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Always |
| Maximum partials | [Editor](../../user/en/reference-tables.md) | 1–32768 | — | 64 | — | — | Always |
| Minimum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz, kHz | — | Inspector |
| Maximum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz, kHz | — | Inspector |
| Frequency drift | [Editor](../../user/en/reference-tables.md) | 0–20000 Hz | — | 0 Hz | Hz, kHz | — | Inspector |
| Bandwidth model | [Editor](../../user/en/reference-tables.md) | None / Residue / Convergence | — | Convergence | — | — | Inspector |
| Convergence tolerance | [Editor](../../user/en/reference-tables.md) | 0.001–1 | — | 0.1 | — | — | Inspector; Convergence |
| Bandwidth region | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | Hz, kHz | — | Inspector; Residue |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Residual | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |


## Behavior

Uses Loris' Kaiser-window reassigned spectrum, peak selection, frequency-resolution thinning and partial tracking. The input FFT determines the available window duration and frame cadence. Resolution is the minimum peak separation in Hz; 0 selects `2 × sampleRate / windowSamples`. Frequency drift is the permitted change between consecutive track points; 0 selects half the resolution. Components do not have to share a fundamental.

Bandwidth model determines how Loris estimates the noisy fraction of a partial:

- **None:** bandwidth is zero; the entire detected amplitude is tonal.
- **Residue:** associates rejected spectral energy within Bandwidth region.
- **Convergence:** uses Loris' mixed phase-derivative indicator; Convergence tolerance sets its saturation point.

The emitted sinusoidal amplitude is `LorisAmplitude × sqrt(1 − bandwidth)`. [Partial Context](partial-context.md) exposes `1 − bandwidth` as Tonality. Energy not represented by these sinusoids remains in the exact residual. This is a sinusoidal/noisy model, not an instrument, percussion or semantic classifier.

Minimum track duration is causal: a track is emitted only after the elapsed time, without rewriting earlier frames. Maximum partials limits simultaneous tracks; stronger accepted peaks take precedence. Tracks end when no peak can be linked in the next frame. Resolution, duration and other analysis changes restart tracking. IDs stay unique within the running analyzer.

The original allocating Loris stages run on a prepared helper thread. Rendering waits for each frame, so their computation still counts against the real-time deadline. No offline whole-file pass, retrospective phase correction or additional frame queue is used. Long FFT windows and large capacities cost more time and memory.

Unmodified [Partials to Spectrum](partials-to-spectrum.md) output plus Residual reconstructs the original complex spectrum, including estimation errors. [Residual Model](residual-model.md) is a separate lossy stochastic model. Short windows cannot resolve close components, and no setting guarantees perfect tonal/noise separation.

## Mute

| [Mutable](../../user/en/muting.md) | [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- | --- |
| No | — | — |

## Insertion

| [Input](../../user/en/reference-tables.md#input-output) | [Output](../../user/en/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sample-wise feedback](../../user/en/reference-tables.md#feedback) |
| --- |
| Not supported |

[Partials to Spectrum](partials-to-spectrum.md) · [Spectrum Math](spectrum-math.md) · [Partial Context](partial-context.md) · [Partial Output](partial-output.md) · [Partial Group](partial-group.md)

[Deutsch](../de/partial-analysis.md)
