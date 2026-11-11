# Level Detector
Measures audio peak and trailing RMS levels.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Audio |
| Channel | Left / Right / Mix; Default: Left | Editor |
| RMS window | 0.1–2000 ms; Default: 50 ms; s, ms, smp | Editor |
| Smoothing | Off / On; Default: Off | Editor |
| Attack | 0–2000 ms; Default: 10 ms; s, ms, smp | Editor; Always; enabled with Smoothing |
| Release | 0–2000 ms; Default: 100 ms; s, ms, smp | Editor; Always; enabled with Smoothing |

## Outputs
| Output | Signal |
| --- | --- |
| Peak | Value; ≥ 0; Control |
| RMS | Value; ≥ 0; Control |

## Behavior
Peak = max(abs(x)) over the current block; RMS = sqrt(mean(x²)) over the trailing window. At startup RMS uses only received samples. Polyphonic voices are summed first; Mix = (L+R)/2. Optional independent attack/release smoothing follows both measurements, with one time constant reaching about 63% of a step. Zero time responds immediately. Output units are Factor, % or dB; full-scale sine RMS is approximately 0.707. There is no frequency weighting.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/level-detector.md)
