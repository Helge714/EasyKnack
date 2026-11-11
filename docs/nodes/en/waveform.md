# Waveform
Creates an editable periodic Wavetable resource.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Waveform | Curve / Resource editor; Default: Sine | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Waveform | Wavetable resource; Control |

## Behavior
Curve mode defines a periodic waveform using control points. Resource mode edits sampled waveform values directly, without Bézier segments. Switching modes can discard the previous representation. Captured Wavetables can be pasted as sample resources. Connect the output to an oscillator's Waveform input; this resource does not play by itself.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/waveform.md)
