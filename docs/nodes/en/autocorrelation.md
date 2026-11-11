# Autocorrelation
Computes autocorrelation from an existing spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Normalization | Biased / Unbiased; Default: Biased | Editor |
| Smoothing factor | 0–1; Default: 1 | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Correlation | Analysis resource; ±1; Control |

## Behavior
`r = IFFT(|X|²)`, normalized to lag zero; silence returns zero. FFT padding 2x gives linear correlation, without padding it is circular. Unbiased compensates the decreasing sample overlap at larger linear lags, then limits values to ±1; window tapering is not undone. For untapered, DC-centered correlation choose Rectangular, 2x padding and Remove DC in [FFT](fft.md). Each frame updates `held = old·(1 − factor) + new·factor`, initialized from the first frame. Factor 0 holds that frame; 1 updates immediately. [Correlation Features](correlation-features.md) reduces the result to scalar descriptors.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/autocorrelation.md)
