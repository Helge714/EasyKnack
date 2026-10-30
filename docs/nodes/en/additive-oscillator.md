# Additive Oscillator

## Overview

The Additive Oscillator generates sound from up to 144 partials.

## Inputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Editor range](../../user/en/reference-tables.md#editor-range) | [Signal range](../../user/en/reference-tables.md#signal-range) | [Default](../../user/en/reference-tables.md#default) | [Unit](../../user/en/units.md) | [Rate](../../user/en/rates.md) | [Visibility](../../user/en/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Waveform | [Wavetable resource](../../user/en/data-types.md) | Sine, Triangle, Sawtooth, Square | — | Sine | — | [Control](../../user/en/rates.md) | Always |
| Partials | [Value](../../user/en/data-types.md) | 1–144, integer | 1–144, rounded down | 1 | — | [Control](../../user/en/rates.md) | Always |
| Falloff | [Value](../../user/en/data-types.md) | 0–4 | 0–4 | 1 | — | [Auto](../../user/en/rates.md) | Partials > 1 or Partials connected |
| Stretch | [Value](../../user/en/data-types.md) | 0–4 | 0–4 | 1 | — | [Auto](../../user/en/rates.md) | Partials > 1 or Partials connected |
| Frequency | [Value](../../user/en/data-types.md) | 1e-5–20 000 Hz | 0–20 000 Hz | 440 Hz | Hz, kHz | [Auto](../../user/en/rates.md) | Always |
| Phase | [Value](../../user/en/data-types.md) | 0–1 turn | ±∞ turn | 0 turn | turn, °, rad | [Auto](../../user/en/rates.md) | Always |
| Reset | [Trigger](../../user/en/data-types.md) | Button | — | — | — | [Trigger](../../user/en/rates.md) | Always |

## Outputs

| [Name](../../user/en/reference-tables.md#name) | [Data type](../../user/en/data-types.md) | [Range](../../user/en/reference-tables.md#range) | [Rate](../../user/en/rates.md) | [Channel](../../user/en/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio Out | [Audio](../../user/en/data-types.md) | ±1 | [Audio](../../user/en/rates.md) | [Auto](../../user/en/data-types.md#channels) |

## Behavior

### Partials = 1

Equivalent to the ordinary [Oscillator](oscillator.md).

### Partials > 1

In steady state:

```text
I = { n ∈ {1, …, N} : f · n^p < fs / 2 }
θn[k + 1] = wrap(θn[k] + f[k] · n^p / fs)
y[k] = Σ(n ∈ I) n^(−a) W(θn[k] + φ[k] n^p) / Σ(n ∈ I) n^(−a)
```

N: partial count including the fundamental; f: frequency in Hz; fs: sample rate;
p: Stretch; a: Falloff; φ: phase in turns; W: selected periodic waveform;
`wrap(x) = x − floor(x)`. An empty I gives zero.

- Stretch sets partial frequencies through `f × n^p`; p = 1 is the harmonic series.
- Falloff sets weights through `n^(−a)`; the result is normalized by the included weights.
- Changes in ratios and weights are smoothed internally.

Other functions follow the ordinary [Oscillator](oscillator.md).


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

[Node index](index.md) · [Deutsch](../de/additive-oscillator.md)
