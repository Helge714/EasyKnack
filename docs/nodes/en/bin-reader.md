# Bin Reader
Reads an indexed bin from the original input spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Control |
| Bin index | ±∞; Default: 0 | Value; Boundary-dependent; Auto |
| Outside spectrum | Zero, Clamp, Wrap; Default: Zero | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Real | Value; ±∞; Audio (bin/frame) |
| Imaginary | Value; ±∞; Audio (bin/frame) |
| Magnitude | Value; 0–∞; Audio (bin/frame) |
| Phase | Value; ±π rad; Audio (bin/frame) |
| Amplitude | Value; 0–∞; Audio (bin/frame) |
| Frequency | Value; 0–fs/2 Hz; Audio (bin/frame) |

## Behavior
Inside a [Spectral Group](spectral-group.md), fractional indices interpolate neighboring complex bins. Reads always use the original source frame, independent of other bin writes. Zero returns zero outside the one-sided spectrum, Clamp holds its edge, Wrap repeats 0…N/2. Unconnected Spectrum produces zero. Amplitude divides raw magnitude by the analysis-window sum and doubles non-DC/non-Nyquist bins; it is suitable for oscillator weights. Phase is in radians.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-reader.md)
