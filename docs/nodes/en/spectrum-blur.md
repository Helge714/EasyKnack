# Spectrum Blur
Spreads spectral content across neighboring frequencies.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Kernel | Gaussian / Box / Maximum; Default: Gaussian | Editor |
| Synthesis | Noise / Continuous / Preserve; Default: Noise | Editor |
| Normalize power | Off / On; Default: Off | Editor |
| Width | ≥ 0 oct / bins; Default: 0 oct; oct / cent / st / bins | Value; ≥ 0 oct / bins; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum resource; Complex FFT bins; Control |

## Behavior
Width uses the unit selected from its slider or port context menu. Octaves, cent and semitones define the same logarithmic radius: width b in octaves covers `f·2^(−b)` to `f·2^b`; 1 oct = 1200 cent = 12 st. Bins selects a linear bin radius. Switching between Bins and pitch units retains the stored numeric radius and changes its interpretation; there is no universal conversion between them. DC is unchanged in logarithmic modes. Gaussian uses a three-standard-deviation radius; Box weights the neighborhood uniformly; Maximum takes its largest magnitude. Noise synthesizes continuous noise across frequency regions, Continuous uses bin-centered tones, and Preserve retains coefficient phase. Zero width is an exact bypass. At very small noise widths, phase deviation is limited for a continuous transition. Normalize power preserves total spectral power, in Noise mode as an expectation rather than a per-frame limiter. Controls are evaluated per incoming frame.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Spectrum → Spectrum |
| Insertion | Spectrum → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectrum-blur.md)
