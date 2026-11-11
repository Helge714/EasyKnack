# Partial Analysis
Separates a spectrum into tracked partials and a complex residual spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum; Control |
| Resolution | 0–20000 Hz; Default: 0 Hz; Hz, kHz | Editor |
| Threshold | −160–0 dB; Default: −60 dB; dB | Editor |
| Minimum track duration | 0–2000 ms; Default: 20 ms; ms, s, smpl | Editor |
| Maximum partials | 1–32768; Default: 64 | Editor |
| Minimum frequency | 0–∞ Hz; Default: 20 Hz; Hz, kHz | Editor; Inspector |
| Maximum frequency | 0–∞ Hz; Default: 20000 Hz; Hz, kHz | Editor; Inspector |
| Frequency drift | 0–20000 Hz; Default: 0 Hz; Hz, kHz | Editor; Inspector |
| Bandwidth model | None / Residue / Convergence; Default: Convergence | Editor; Inspector |
| Convergence tolerance | 0.001–1; Default: 0.1 | Editor; Inspector; Convergence |
| Bandwidth region | 1–20000 Hz; Default: 2000 Hz; Hz, kHz | Editor; Inspector; Residue |

## Outputs
| Output | Signal |
| --- | --- |
| Partials | Partials; Control |
| Residual | Spectrum; Control |

## Behavior
Uses Loris' Kaiser-window reassigned spectrum, peak selection, frequency-resolution thinning and partial tracking. The input FFT determines the available window duration and frame cadence. Resolution is the minimum peak separation in Hz; 0 selects `2 × sampleRate / windowSamples`. Frequency drift is the permitted change between consecutive track points; 0 selects half the resolution. Components do not have to share a fundamental.

Bandwidth model determines how Loris estimates the noisy fraction of a partial:

- **None:** bandwidth is zero; the entire detected amplitude is tonal.
- **Residue:** associates rejected spectral energy within Bandwidth region.
- **Convergence:** uses Loris' mixed phase-derivative indicator; Convergence tolerance sets its saturation point.

The emitted sinusoidal amplitude is `LorisAmplitude × sqrt(1 − bandwidth)`. [Partial Context](partial-context.md) exposes `1 − bandwidth` as Tonality. Energy not represented by these sinusoids remains in the exact residual. This is a sinusoidal/noisy model, not an instrument, percussion or semantic classifier.

Minimum track duration is causal: a track is emitted only after the elapsed time, without rewriting earlier frames. Maximum partials limits simultaneous tracks; stronger accepted peaks take precedence. Tracks end when no peak can be linked in the next frame. Resolution, duration and other analysis changes restart tracking. IDs stay unique within the running analyzer.

Analysis must finish each frame within the real-time deadline; long FFT windows and large capacities can overload playback.

Unmodified [Partials to Spectrum](partials-to-spectrum.md) output plus Residual reconstructs the original complex spectrum, including estimation errors. [Residual Model](residual-model.md) is a separate lossy stochastic model. Short windows cannot resolve close components, and no setting guarantees perfect tonal/noise separation.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Deutsch](../de/partial-analysis.md)
