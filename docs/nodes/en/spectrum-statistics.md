# Spectrum Statistics
Reduces displayed spectral values to one scalar.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Data | Magnitude / Phase / Power; Default: Magnitude | Editor |
| Statistic | Peak / Mean / RMS / Integral; Default: Peak | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Value | Value; Statistic-dependent; Control |

## Behavior
Magnitude is one-sided amplitude, Power its square and Phase the angle in radians. For selected bin values v[k]: Peak = max(abs(v)), Mean = Σv/K, RMS = sqrt(Σv²/K), Integral = Σv·Δf. Spectral-bin RMS is not time-domain audio RMS. The result holds until the next frame.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectrum-statistics.md)
