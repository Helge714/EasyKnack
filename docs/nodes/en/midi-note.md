# MIDI Note
Converts MIDI notes into per-voice frequency, gate, velocity and triggers.
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
| Voices | 1–32; Default: 1 | Editor |
| Pitch bend | 0–48 st; Default: 2 st; cent, st, oct, Factor | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Frequency | Value; > 0 Hz; Audio |
| Gate | Value; 0–1; Audio |
| Trigger | Trigger |
| Velocity | Value; 0–1; Audio |

## Behavior
Frequency = `440 · 2^((note − 69 + transpose + bend)/12)`. Gate is 1 while a note is held, including sustain. Velocity is note-on velocity/127 and becomes 0 on release. Frequency retains the last pitch on release. Each note-on emits a Trigger. One voice uses last-note priority with held-note fallback; polyphony assigns separate voices and steals the oldest held voice when full. Use [Voice Merge](voice-merge.md) to combine them before shared effects; an Audio Output also merges voices.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/midi-note.md)
