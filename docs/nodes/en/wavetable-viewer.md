# Wavetable Viewer
Displays the actual samples of a wavetable resource.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Waveform | — | Wavetable resource; Wavetable resource; Control |
| Samples | Sample graph | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
The graph shows stored resource samples across one period, not an editable Bézier curve. Connect [Waveform](waveform.md) or [Spectrum to Waveform](spectrum-to-waveform.md) to inspect the table actually supplied to oscillators. Resizing the node vertically enlarges the sample graph. Hover shows sample index and value. Right-click to copy the resource; pasting creates a Waveform node in Resource mode.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/wavetable-viewer.md)
