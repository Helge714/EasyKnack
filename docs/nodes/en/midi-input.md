# MIDI Input
Receives MIDI from a device or the computer keyboard.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Device | Available MIDI devices; Default: Keyboard | Editor |
| Transpose | −48–48 st; Default: 0 st; cent, st, oct, Factor | Editor; Keyboard selected |
| Keyboard | Keyboard focus; Default: Inactive | Editor; Keyboard selected |
| All notes off | Button | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| MIDI | MIDI; MIDI events |

## Behavior

[Guide](../../user/en/midi.md)
Select a hardware input or Keyboard. Click the keyboard control to capture musical keys; clicking elsewhere releases that focus. At zero transposition, A plays C4. All notes off releases active notes and clears pending MIDI delays.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/midi-input.md)
