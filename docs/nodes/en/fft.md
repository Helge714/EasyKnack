# FFT
Transforms windowed audio into complex spectral frames.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Auto |
| Synchronous | Off / On; Default: On | Editor |
| Minimum frame interval | 0–60000 ms; Default: 0 ms; ms | Editor; Inspector; asynchronous |
| Window size | 64 / 128 / 256 / 512 / 1024 / 2048 / 4096 / 8192 / 16384 / 32768 / 65536 smp; Default: 2048 smp; smp | Editor |
| Window | Hann / Hamming / Blackman / Rectangular; Default: Hann | Editor |
| Zero padding | None / Linear correlation (2x); Default: None | Editor |
| Remove DC | Off / On; Default: Off | Editor |
| Overlap | 0–1; Default: 0.75; Factor, % | Editor |
| Channel | All / Left / Right / Mix; Default: All | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum resource; Complex FFT bins; Control |

## Behavior

[Guide](../../user/en/spectral-synthesis.md)
Each frame contains complex coefficients `X[k] = Σ x[n]·w[n]·exp(−i·2πkn/N)` with its source sample position. Bin frequency is `k·sampleRate/N`; zero padding increases N without adding measured information. Remove DC subtracts the window mean before windowing. Hop = rounded `windowSize·(1 − overlap)`, with a synchronous minimum of 16 samples for a 64-sample window, 32 for 128 samples and 64 for larger windows; asynchronously the minimum is 1 sample. All preserves stereo and voices; Left/Right/Mix sum voices, with Mix = `(L+R)/2`. Synchronous processing is intended for [IFFT](ifft.md). Asynchronous analysis can skip or deliver late frames; Minimum frame interval additionally limits its request rate. For linear [Autocorrelation](autocorrelation.md), choose Rectangular and 2x padding.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/fft.md)
