# Audio Output
Sends audio to an output device.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Audio |
| Device | Available output devices; Default: Default device | Editor |
| Device volume | 0–100 %; Default: Current Windows volume; % | Editor; Windows output |
| Link volume keys | Off / On; Default: Off | Editor; Windows output |
| Signal | Level display | Editor; Windows output |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
MIDI voices are summed automatically. Device volume controls the Windows endpoint volume and reflects external changes; it is not an additional signal gain. Linked volume keys change selected endpoints proportionally. Muting this node suppresses its EasyKnack signal without muting the Windows device.

Linked volume keys show a brief desktop overlay, including over other applications. For several linked outputs, the percentage is the highest output volume; their relative levels remain unchanged.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/audio-output.md)
