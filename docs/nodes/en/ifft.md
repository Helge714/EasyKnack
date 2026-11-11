# IFFT
Reconstructs audio from complex Spectrum frames.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Control |
| Stability | 0–1; Default: 0 | Value |

## Outputs
| Output | Signal |
| --- | --- |
| Audio | Audio; ±∞ |

## Behavior

[Guide](../../user/en/spectral-synthesis.md)
Applies inverse FFT and normalized overlap-add using source frame timing and window metadata. A synchronous path has one-window reconstruction latency. Asynchronous FFT is accepted, but late/missing frames can leave gaps. Source identity changes reset overlap state. Stability applies bounded RMS correction with short/long tracking; 0 is unchanged, 1 full correction. It adds no lookahead latency but can soften intentional dynamics and broaden spectral content. See [Spectral Group](spectral-group.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/ifft.md)
