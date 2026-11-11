# MIDI-Verzögerung (MIDI Delay)
MIDI-Verzögerung verarbeitet MIDI-Ereignisse.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI-Ereignisse; MIDI |
| Verzögerung (Delay) | 0–10000 ms; Standard: 100 ms; s, ms, smp | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| MIDI | MIDI; MIDI-Ereignisse |

## Verhalten
Verzögert Noten und Controller-Nachrichten gemeinsam. Eine Änderung gilt für neu eintreffende Ereignisse; wartende Ereignisse behalten ihren Termin. Alle Noten aus leert die Warteschlange.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | MIDI → MIDI |
| Einfügen | MIDI → MIDI |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/midi-delay.md)
