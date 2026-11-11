# Spectrum Prune
Retains the loudest fraction of spectral bins.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Keep | 0–1; Default: 1; Factor, % | Value; 0–1; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum resource; Complex FFT bins; Control |

## Behavior
The retained bin count is rounded from Keep times the nonnegative bin count, including DC and Nyquist. Ranking uses single-sided amplitude; ties favor lower indices. Retained coefficients and phases are unchanged, with no gain normalization. Keep 0 clears all bins; 1 bypasses processing. [Spectral Group](spectral-group.md) can skip zero bins when Skip empty bins is enabled.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Spectrum → Spectrum |
| Insertion | Spectrum → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectrum-prune.md)
