# Additive Oscillator
The Additive Oscillator generates sound from up to 144 partials.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Waveform | Sine, Triangle, Sawtooth, Square; Default: Sine | Wavetable resource; Control |
| Partials | 1–144, integer; Default: 1 | Value; 1–144, rounded down; Control |
| Falloff | 0–4; Default: 1 | Value; 0–4; Auto; Partials > 1 or Partials connected |
| Stretch | 0–4; Default: 1 | Value; 0–4; Auto; Partials > 1 or Partials connected |
| Frequency | 1e-5–20 000 Hz; Default: 440 Hz; Hz, kHz | Value; 0–20 000 Hz; Auto |
| Phase | 0–1 turn; Default: 0 turn; turn, °, rad | Value; ±∞ turn; Auto |
| Reset | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±1 |

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

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/additive-oscillator.md)
