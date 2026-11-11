# Spectral Group
Processes each bin of an incoming spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Control |
| Frame inputs | Interface-defined; Default: Interface-defined | Value; Audio / Control / Trigger; Auto |
| Skip empty bins | Off / On; Default: Off | Editor; Inspector |
| Parallel bins | Off / On; Default: On | Editor; Inspector |
| General / IO / Editors | Group-defined | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Spectrum resource; Control |

## Behavior

[Guide](../../user/en/spectral-group.md)
One Spectrum sets bin count and frame timing. [Bin Context](bin-context.md) exposes complex components, frequency and index; [Bin Reader](bin-reader.md) reads other bins. [Bin Output](bin-output.md) writes independent complex outputs; the default wiring is unchanged complex passthrough. External frame values are sampled at the FFT window end; asynchronous paths retain their time alignment. Frame triggers apply to the corresponding frame. Frame-only processing evaluates once per bin/frame, not per audio sample. Oscillators/noise create persistent audio per bin, reduced through Instance Merge; Bin Output cannot accept that audio. Bin Discard is scheduled before bin work and affects all Spectrum outputs. Parallel workers preserve independent bin state. [IFFT](ifft.md) reconstructs complex spectra; an oscillator bank is not phase-equivalent IFFT.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Spectrum → Out |
| Insertion | Spectrum → Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectral-group.md)
