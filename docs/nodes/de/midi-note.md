# MIDI-Note (MIDI Note)
Wandelt MIDI-Noten in Frequenz, Gate, Anschlagstärke und Trigger je Stimme um.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI-Ereignisse; MIDI |
| Kanal (0 = alle) (Channel (0 = all)) | 0–16; Standard: 0 | Editor |
| Stimmen (Voices) | 1–32; Standard: 1 | Editor |
| Pitch-Bend (Pitch bend) | 0–48 st; Standard: 2 st; cent, st, oct, Faktor | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Frequenz (Frequency) | Wert; > 0 Hz; Audio |
| Gate | Wert; 0–1; Audio |
| Trigger | Trigger |
| Anschlagstärke (Velocity) | Wert; 0–1; Audio |

## Verhalten
Frequenz = `440 · 2^((note − 69 + transpose + bend)/12)`. Gate ist 1, solange eine Note gehalten wird, einschließlich Haltepedal. Anschlagstärke ist Note-on-Velocity/127 und wird beim Loslassen 0. Die Frequenz behält den letzten Ton. Jedes Note-on erzeugt einen Trigger. Eine Stimme bevorzugt die zuletzt gespielte Note und fällt auf gehaltene Noten zurück; bei Polyphonie entstehen getrennte Stimmen, bei Überbelegung wird die älteste gehaltene Stimme ersetzt. [Voice Merge](voice-merge.md) fasst sie vor gemeinsamen Effekten zusammen; ein Audio-Ausgang mischt Stimmen ebenfalls.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/midi-note.md)
