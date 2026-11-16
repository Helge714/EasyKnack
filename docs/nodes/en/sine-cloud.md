# Sine Cloud

## Overview

Generates a research stimulus of up to ten million equal-amplitude, uniformly distributed sinusoids with optional random initial phases.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Count | [Value](../../user/en/data-types.md#value) | 1–10,000,000 | 1–10,000,000, rounded | 10,000 | — | [Control](../../user/en/rates.md#control) | Always |
| Min Frequency | [Value](../../user/en/data-types.md#value) | 0.00001 Hz–half the DSP sample rate | 0.00001 Hz–half the sample rate | 20 Hz | [Hz, kHz](../../user/en/units.md#frequency) | [Control](../../user/en/rates.md#control) | Always |
| Max Frequency | [Value](../../user/en/data-types.md#value) | 0.00001 Hz–half the DSP sample rate | 0.00001 Hz–half the sample rate | 20,000 Hz | [Hz, kHz](../../user/en/units.md#frequency) | [Control](../../user/en/rates.md#control) | Always |
| Seed | [Value](../../user/en/data-types.md#value) | 0–16,777,215 | 0–16,777,215, rounded | 0 | — | [Control](../../user/en/rates.md#control) | Always |
| Randomize phases | [Editor](../../user/en/reference-tables.md) | Off, On | — | On | — | — | Always |
| Max Bin Spacing | [Editor](../../user/en/reference-tables.md) | 0.001–1,000 Hz | — | 0.01 Hz | [Hz, kHz](../../user/en/units.md#frequency) | — | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md#audio) | ±1 | [Audio](../../user/en/rates.md#audio) | [Auto](../../user/en/data-types.md#channels) |
| Actual count | [Value](../../user/en/data-types.md#value) | 0–10,000,000 | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |
| Period | [Value](../../user/en/data-types.md#value) | 0 or N / (sample rate × bin GCD) | [Control](../../user/en/rates.md#control) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

Max Bin Spacing sets the frequency grid independently of Count and the frequency interval. The generator chooses the smallest supported table with N ≥ sample rate / spacing, at least 65,536 and at most 67,108,864 samples. Supported lengths have only factors 2, 3 and 5; they need not be powers of two. This selection grid has spacing sample rate / N and never exceeds the requested maximum. At 48 kHz, 0.01 Hz uses exactly 4,800,000 samples (100 seconds, 19.2 MB of mono float audio); 0.001 Hz uses 48,000,000 samples. A narrower frequency interval does not enlarge the selection grid. After selecting the occupied bins, the generator divides N and all bin indices by their common divisor before synthesis. This removes exact sample repetitions without changing any selected frequency, initial phase or waveform; the stored table can therefore be much shorter than the selection-grid duration. For Count = 2 in 0–24,000 Hz at 48 kHz, the tones are 8,000 and 16,000 Hz and need only six samples (0.125 ms). Some combinations remain irreducible and still need the full table.

Count requests up to ten million equal-amplitude sinusoids. Actual count is limited to the available distinct positive bins in the selected interval. Tone targets lie inside the interval at minimum + (i + 1) × (maximum − minimum) / (actual count + 1), for i starting at zero. Targets are rounded to the grid, reserving distinct bins for remaining tones when capacity is tight. At full capacity all available bins are used, including edge bins; adjacent spacings may differ by one bin. At 48 kHz and 0.01 Hz spacing, 20–20,000 Hz fits 1,998,001 tones. An interval from 100 to 100.025 Hz fits three bins. One tone uses the bin nearest the interval midpoint. Two tones in 0–1,320 Hz give 440 and 880 Hz, a useful audible check. Reversed bounds are sorted. If an interval contains no bin, the nearest interior bin to its midpoint is used. Control values use the audio engine's float precision; rounding at a bin boundary can affect whether an edge bin is included. DC and the exact Nyquist bin are excluded. Frequency editors stop at half the application DSP sample rate; the native generator uses its actual sample rate.

Randomize phases draws each initial phase once from Seed. With it off, all sine components start at phase zero and Seed changes do not rebuild the table. The stored samples match the additive sum at the selected bin frequencies, up to numerical error, then the whole table is normalized to a peak magnitude of 1. This is not RMS normalization. No new random draws, boundary crossfades or suppression of beating occur during unchanged playback. Identical parameters and sample rate reproduce the same stimulus.

Period reports the shortest common waveform period of the active tones: N / (sample rate × greatest common divisor of occupied bin indices). A single tone reports 1 / its actual frequency. This can differ from the full stored table duration and can be a noninteger sample count. The stored sample sequence loops after N samples. Period and Actual count are initially 0, keep the active table's values during construction and switch when a replacement begins its 20 ms crossfade.

Changes build a replacement in the background; rapid changes coalesce. The previous table continues until it is ready. Initial startup is silent. Construction time depends on CPU and table size, not its playback duration. A failed build or spacing requiring more than 67,108,864 samples retains the previous table (initially silence). The worker retains at most three tables; construction additionally needs two complex-double arrays, about 153.6 MB for a 4.8-million-sample transform, plus sample tables. Audio playback performs no allocation or transform.

Compatibility: Gaussian frequency density has been removed. Existing Gaussian settings are ignored and older documents without Max Bin Spacing use 0.01 Hz. Tone placement also uses interior subdivisions rather than the old endpoint-inclusive distribution. Their generated frequencies, actual count, loop length and sound can therefore change; existing documents remain readable.

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
