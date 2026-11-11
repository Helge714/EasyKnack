# Spectrum to Waveform
Captures a periodic wavetable from a spectrum.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Spectrum resource; Complex FFT bins; Control |
| Fundamental | 1–20000 Hz; Default: 440 Hz; Hz, kHz | Value; 1–20000 Hz; Auto |
| Capture | Button | Trigger; Trigger; Trigger |
| Normalize | Off / On; Default: On | Editor |
| Copy captured resource | Button | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Waveform | Wavetable resource; 2048 samples; Control |

## Behavior
Capture requests a background reconstruction from a valid FFT frame using Fundamental as the harmonic reference. The output holds the captured resource until the next capture; it does not rebuild every block. Normalize scales the captured waveform. Copy captured resource places sample data on the application clipboard; paste creates a [Waveform](waveform.md) in Resource mode. Captured samples are saved with the document. Inspect them with [Wavetable Viewer](wavetable-viewer.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectrum-to-waveform.md)
