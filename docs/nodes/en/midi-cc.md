# MIDI CC
Converts a MIDI controller into a held numeric value.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI events; MIDI |
| Channel (0 = all) | 0–16; Default: 0 | Editor |
| Controller | 0–127; Default: 1 | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Value | Value; 0–1; Audio |

## Behavior
Value = `CC/127`; changes retain their sample position and the last value is held. Controller 1 is modulation, 7 volume and 64 sustain. Channel 0 accepts every MIDI channel.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/midi-cc.md)
