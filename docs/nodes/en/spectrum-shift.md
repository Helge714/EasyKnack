# Spectrum Shift
Translates a spectrum by a bin offset.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Shift (bins) | ±8192; Default: 0; bins | Value; ±∞; Auto |
| Continuous phase | Off / On; Default: On | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum resource; Complex FFT bins; Control |

## Behavior
Positive Shift moves content upward by `shift·sampleRate/FFTSize` Hz. Fractional shifts interpolate complex coefficients; content outside the valid range is discarded. Continuous phase compensates frame-time phase for a constant shift. This is frequency translation rather than proportional musical transposition; fast modulation can introduce artifacts.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Spectrum → Spectrum |
| Insertion | Spectrum → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectrum-shift.md)
