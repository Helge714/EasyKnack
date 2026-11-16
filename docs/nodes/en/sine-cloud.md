# Sine Cloud

## Overview

Generates a research stimulus of up to ten million equal-amplitude, uniformly or Gaussian-distributed sinusoids with optional random initial phases.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Count | [Value](../../user/en/data-types.md#value) | 1–10,000,000 | 1–10,000,000, rounded | 10,000 | — | [Control](../../user/en/rates.md#control) | Always |
| Min Frequency | [Value](../../user/en/data-types.md#value) | 0.00001 Hz–half the DSP sample rate | 0.00001 Hz–half the sample rate | 20 Hz | [Hz, kHz](../../user/en/units.md#frequency) | [Control](../../user/en/rates.md#control) | Always |
| Max Frequency | [Value](../../user/en/data-types.md#value) | 0.00001 Hz–half the DSP sample rate | 0.00001 Hz–half the sample rate | 20,000 Hz | [Hz, kHz](../../user/en/units.md#frequency) | [Control](../../user/en/rates.md#control) | Always |
| Seed | [Value](../../user/en/data-types.md#value) | 0–16,777,215 | 0–16,777,215, rounded | 0 | — | [Control](../../user/en/rates.md#control) | Always |
| Gaussian | [Editor](../../user/en/reference-tables.md) | Off, On | — | Off | — | — | Always |
| Randomize phases | [Editor](../../user/en/reference-tables.md) | Off, On | — | On | — | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Actual count | [Value](../../user/en/data-types.md#value) | 0–10,000,000 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Period | [Value](../../user/en/data-types.md#value) | 0 or N / (sample rate × bin GCD) | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

With Randomize phases on (default), initial phases are drawn once from Seed and retained in a periodic table. With it off, all sine components start at phase zero; Seed has no effect and changing it does not rebuild the table. This choice is independent of Gaussian frequency density. During unchanged playback, the table is looped exactly: no new draws, loop crossfade, decorrelation or suppression of beating and periodicity is applied. Identical parameters and sample rate produce the same table. Every included sinusoid has the same amplitude; the whole table is normalized to a peak magnitude of 1. This is not RMS normalization: average level depends on the seed and tone count. Channel copies with identical parameters play the same stimulus.

With Gaussian off, frequencies are distributed as evenly as possible over distinct positive IFFT bins. With Gaussian on, deterministic quantiles follow a normal distribution in linear Hz, centered between Min/Max, with standard deviation (Max − Min) / 6 and truncated at ±3σ. All sinusoids retain equal amplitude; the density of frequencies increases toward the center. Seed controls phases, not these frequency positions. Gaussian reserves a finer grid to keep tones distinct; insufficient resolution reduces Actual count rather than merging tones. Tables contain 65,536–67,108,864 samples, increasing with the required frequency resolution. The grid spacing is sample rate / table size. Requested frequencies are therefore approximated; adjacent spacings can differ by one bin. The frequency editors use half the application DSP sample rate as their upper bound (currently 24,000 Hz at 48 kHz). The native generator also clamps connected values to its actual Nyquist limit. DC and the exact Nyquist bin are excluded; the highest generated tone is on the last interior bin below Nyquist. Reversed frequency bounds are sorted. Equal bounds produce one tone at the nearest representable bin. If a very narrow interval contains no bin, the nearest bin to its midpoint is used instead.

Insufficient grid positions reduce the actual count. Actual count reports the newly active table's tone count, initially 0. At 48 kHz, 20–20,000 Hz accommodates ten million distinct tones in both distributions. Uniform uses 33,554,432 samples for this setting; Gaussian uses 67,108,864. Narrower bands may not accommodate that count. The longest table repeats after approximately 1,398.1 seconds (23.3 minutes) at 48 kHz.

Period outputs the shortest common waveform period of the active tones in seconds: N / (sample rate × greatest common divisor of the occupied bin indices). For one tone this is 1 / its actual, grid-quantized frequency; for coprime bin indices it equals the full table duration. Random phases do not change this period. This waveform period may span a noninteger number of samples; the stored sample sequence still loops after N samples. The unit menu also offers milliseconds and samples. The output is 0 before a table is available, retains the old period while a replacement builds, and switches to the new period when crossfading starts.

Inputs are sampled at the start of each processing call. Changes build a new table in the background; rapid changes coalesce. The previous table continues until the replacement is ready; initial startup is silent. Changes restart the new table at its beginning and crossfade linearly for 20 ms. During the transition, equal component amplitudes apply within each table separately. Failed builds retain the previous table. Construction uses CPU time and temporary memory: the largest table occupies 256 MiB plus 1 GiB of complex IFFT scratch. Each node retains at most three tables.

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
| Unsupported |

[Node index](index.md) · [Deutsch](../de/sine-cloud.md)
