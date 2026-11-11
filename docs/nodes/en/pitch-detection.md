# Pitch Detection
Estimates a fundamental from autocorrelation.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Autocorrelation | — | Analysis resource; Correlation resource; Control |
| Minimum | 1–20000 Hz; Default: 40 Hz; Hz, kHz | Editor |
| Maximum | 1–20000 Hz; Default: 2000 Hz; Hz, kHz | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Frequency [Hz] | Value; 0–20000 Hz; Control |
| Confidence | Value; 0–1; Control |

## Behavior
Ignores the initial zero-lag lobe and selects the earliest local peak reaching 90% of the strongest peak in range, provided that peak reaches 0.1. Parabolic interpolation refines its lag. Frequency = 1/lag; Confidence is its normalized correlation height. No suitable peak gives zero. Use Rectangular FFT, 2x padding and several periods of the lowest expected frequency. Minimum must be below Maximum. [Correlation Features](correlation-features.md) additionally provides validity and HNR.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/pitch-detection.md)
