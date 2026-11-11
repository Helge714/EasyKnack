# Partials to Spectrum
Reconstructs a complex spectrum from partials for IFFT.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Partials | — | Partials; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum; Control |

## Behavior
Reconstructs positive and negative frequency contributions with the analysis window. Frequency changes integrate into a continuous phase correction between frames. Frequencies outside 0–Nyquist and nonpositive amplitudes are discarded. Unmodified partials plus their associated residual reproduce the original spectrum. Lobe evaluation is limited to eight window bins on either side; remaining original tails reside in the residual.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Deutsch](../de/partials-to-spectrum.md)
