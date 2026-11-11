# Bin Context
Exposes the current bin of a Spectral Group.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Real | Value; ±∞; Audio (bin/frame) |
| Imaginary | Value; ±∞; Audio (bin/frame) |
| Magnitude | Value; 0–∞; Audio (bin/frame) |
| Phase | Value; ±π rad; Audio (bin/frame) |
| Frequency | Value; 0–fs/2 Hz; Audio (bin/frame) |
| Bin index | Value; 0–N/2; Audio (bin/frame) |
| Position | Value; 0–1; Audio (bin/frame) |
| Frame time | Value; ±∞ s; Audio (bin/frame) |

## Behavior
Only valid inside a [Spectral Group](spectral-group.md). For FFT size N, bin k has frequency `k × fs/N` and Position `k/(N/2)`. Real/Imaginary and Magnitude are raw complex FFT coefficients, not normalized amplitudes. Frame time is the window start, which can initially be negative due to zero padding. The displayed Audio rate denotes a bin vector evaluated per frame here, not temporal audio.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-context.md)
