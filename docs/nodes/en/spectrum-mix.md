# Spectrum Mix
Crossfades two temporally aligned complex spectra.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Linear, Equal Power; Default: Linear | Editor |
| Factor | 0–1; Default: 0.5; Factor, % | Value; 0–1; Control |
| A | — | Spectrum; Control |
| B | — | Spectrum; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum; Control |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Linear computes `A * (1 − f) + B * f`. Equal Power uses cosine and sine weights, `cos(πf/2)` and `sin(πf/2)`. Factor 0 selects A, factor 1 selects B. Factor is clamped to 0–1 and applied per FFT frame using the current block Control value. Equal Power preserves expected power for uncorrelated signals; identical signals gain approximately 3 dB at the midpoint. Opposite-phase coefficients can cancel. FFT size, window, hop, sample rate and frame positions must match; incompatible batches produce an empty spectrum. A disconnected input contributes zero. For partials plus residual use Spectrum Math with Add instead.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | A → Spectrum |
| Insertion | A → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Spectrum Math](spectrum-math.md)
