# Partial Analysis

## Overview

Separates a spectrum into tracked partials and a complex residual spectrum.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| [Spectrum](../../user/en/data-types.md#spectrum) | [Spectrum](../../user/en/data-types.md#spectrum) | — | — | — | — | [Control](../../user/en/rates.md#control) | Always |
| Threshold | [Editor](../../user/en/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Always |
| Minimum sinusoidality | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0.8 | — | — | Always |
| Minimum track duration | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Always |
| Maximum partials | [Editor](../../user/en/reference-tables.md) | 1–32768 | — | 64 | — | — | Always |
| Harmonic only | [Editor](../../user/en/reference-tables.md) | Off / On | — | Off | — | — | Always |
| Fundamental | [Value](../../user/en/data-types.md#value) | 0–20000 Hz | 0–∞ Hz | 0 Hz | Hz, kHz | [Control](../../user/en/rates.md#control) | Harmonic only |
| Minimum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz | — | Inspector |
| Maximum frequency | [Editor](../../user/en/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz | — | Inspector |
| Tracking tolerance | [Editor](../../user/en/reference-tables.md) | 0–2400 cent | — | 100 cent | cent | — | Inspector |
| Peak prominence | [Editor](../../user/en/reference-tables.md) | 0–120 dB | — | 3 dB | dB | — | Inspector |
| Assessment time | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 30 ms | ms, s, smpl | — | Inspector |
| Minimum tonality | [Editor](../../user/en/reference-tables.md) | 0–1 | — | 0.7 | — | — | Inspector |
| Tracking gap | [Editor](../../user/en/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Inspector |
| Harmonic tolerance | [Editor](../../user/en/reference-tables.md) | 0–200 cent | — | 30 cent | cent | — | Inspector; Harmonic only |
| Minimum fundamental | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 40 Hz | Hz, kHz | — | Inspector; Harmonic only |
| Maximum fundamental | [Editor](../../user/en/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | Hz, kHz | — | Inspector; Harmonic only |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Partials | [Partials](../../user/en/data-types.md#partials) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Residual | [Spectrum](../../user/en/data-types.md#spectrum) | — | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Local FFT peaks are interpolated between bins. Sinusoidality is normalized squared complex agreement with a sinusoidal window lobe; peak prominence compares the peak against the surrounding median outside the main lobe. Candidates are fitted in descending amplitude order. Matching strong components are removed from a temporary spectrum before weaker candidates are evaluated, reducing interference between neighbouring partials. This temporary subtraction does not remove unconfirmed tracks from the residual. Assignment considers predicted frequency, amplitude change and phase continuity at the window centre.

Assessment time is the time constant of an exponentially smoothed tonality score, not an additional FFT size or rectangular average. Minimum track duration must elapse before acceptance. Tracking gap tolerates missed observations beyond the next expected frame; no partial is invented during a gap. New or unconfirmed components remain entirely in the residual. Tonality is a model score, not a calibrated noise probability.

Harmonic only restricts selection to integer multiples of the fundamental within the cent tolerance. 0 Hz selects a monophonic spectral estimate from the peaks; positive or connected values supply the fundamental. Automatic estimation is not polyphonic pitch detection and can make octave errors. Disable it for inharmonic sounds.

Maximum partials is prepared tracking capacity, including unconfirmed and briefly missing tracks. Additional candidates remain in the residual when capacity is full. Groups, synthesis, nested instances and exports inherit the largest required preparation budget. No memory is allocated for these resources on the audio thread. Large capacities multiply memory requirements per channel, voice and group processor.

Accepted window lobes are subtracted as complex coefficients. Unmodified partials plus the exact residual still reconstruct the source spectrum. [Residual Model](residual-model.md) is a separate lossy stochastic model; the analysis residual itself is neither denoised nor smoothed. FFT window size and resolution remain significant. Longer assessment and minimum durations delay detection, without retrospectively buffering and reclassifying earlier frames.

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
