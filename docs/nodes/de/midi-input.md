# MIDI-Eingang (MIDI Input)
Empfängt MIDI von einem Gerät oder der Computertastatur.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Gerät (Device) | Verfügbare MIDI-Geräte; Standard: Tastatur | Editor |
| Transposition (Transpose) | −48–48 st; Standard: 0 st; cent, st, oct, Faktor | Editor; Tastatur gewählt |
| Tastatur (Keyboard) | Tastaturfokus; Standard: Inaktiv | Editor; Tastatur gewählt |
| Alle Noten aus (All notes off) | Schaltfläche | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| MIDI | MIDI; MIDI-Ereignisse |

## Verhalten

[Anleitung](../../user/de/midi.md)
Wähle einen Hardware-Eingang oder Keyboard. Ein Klick auf die Tastatursteuerung aktiviert die musikalischen Tasten; ein Klick außerhalb gibt den Fokus frei. Ohne Transposition spielt A den Ton C4. Alle Noten aus beendet aktive Noten und leert wartende MIDI-Verzögerungen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/midi-input.md)
