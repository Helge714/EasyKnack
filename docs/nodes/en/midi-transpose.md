# MIDI Transpose
MIDI Transpose processes MIDI events.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI events; MIDI |
| Transpose | −48–48 st; Default: 0 st; cent, st, oct, Factor | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| MIDI | MIDI; MIDI events |

## Behavior
Adds a pitch offset to new notes, including fractional semitones. Existing held notes retain their original offset; note-off matching is preserved.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | MIDI → MIDI |
| Insertion | MIDI → MIDI |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/midi-transpose.md)
