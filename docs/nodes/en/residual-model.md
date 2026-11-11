# Residual Model
Models a residual spectral envelope and synthesizes a stochastic spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Residual | — | Spectrum; Control |
| Envelope width | 0–20000 Hz; Default: 200 Hz; Hz, kHz | Value; 0–∞ Hz; Control |
| Envelope smoothing | 0–10000 ms; Default: 0 ms; ms, s, smpl | Editor; Inspector |
| Seed | 0–2147483647; Default: 0 | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum; Control |

## Behavior
Averages neighboring bin powers within the envelope width (full width in Hz), then assigns fresh reproducible random phases to each frame. At 0 Hz individual bin powers are retained before windowing. Envelope smoothing is an exponential power average with the specified time constant. Generated noise receives the source window; complex conjugate symmetry and removed DC are preserved. Frame power follows the smoothed envelope, not perceived loudness. Seed and frame position determine the random sequence.

Modelling is deliberately lossy and is not classification. Use the original residual for exact reconstruction. FFT size, hop and frame time remain unchanged, so the result can be added to the matching partial spectrum. For an independent shorter residual resolution, first resynthesize through IFFT and analyze with a separate FFT. Mix such branches after their IFFTs and compensate additional latency. Window size is not changed automatically.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Residual → Spectrum |
| Insertion | Residual → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Spectrum Math](spectrum-math.md)
