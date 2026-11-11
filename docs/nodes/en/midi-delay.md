# MIDI Delay
MIDI Delay processes MIDI events.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI events; MIDI |
| Delay | 0–10000 ms; Default: 100 ms; s, ms, smp | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| MIDI | MIDI; MIDI events |

## Behavior
Delays notes and controller messages together. A delay change affects newly arriving events; queued events keep their due time. All notes off clears the queue.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | MIDI → MIDI |
| Insertion | MIDI → MIDI |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/midi-delay.md)
