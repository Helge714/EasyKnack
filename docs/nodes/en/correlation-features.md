# Correlation Features
Reduces autocorrelation to periodicity, period and noise descriptors.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Autocorrelation | — | Analysis resource; Correlation resource; Control |
| Minimum frequency | ≥ 0.001 Hz; Default: 20 Hz; Hz, kHz | Editor; Inspector |
| Maximum frequency | ≥ 0.001 Hz; Default: 20000 Hz; Hz, kHz | Editor; Inspector |
| Periodicity threshold | 0–1; Default: 0.3; Factor, % | Editor; Inspector |
| Silence threshold | ≥ 0; Default: 0.000001; Factor, % | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Periodicity | Value; 0–1; Control |
| Period | Value; ≥ 0 s; Control |
| Frequency | Value; ≥ 0 Hz; Control |
| HNR | Value; ±120 dB; Control |
| Valid | Value; 0–1; Control |

## Behavior
Searches positive local correlation peaks within the frequency bounds, with sub-sample peak interpolation. At least three periods must fit the analyzed window. Periodicity p is the selected normalized correlation peak, not a probability. Period is its lag in seconds; Frequency = 1/Period. HNR = `10·log10(p/(1−p))`, limited to ±120 dB: 0 dB means equal estimated periodic and residual power, positive values favor periodic power. It depends on the upstream window and normalization. Valid = 1 only when peak and source RMS pass both thresholds; otherwise Period/Frequency are zero. Values hold between frames. Feed [Autocorrelation](autocorrelation.md) or its averaged result, not a raw spectrum.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/correlation-features.md)
