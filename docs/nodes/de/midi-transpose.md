# MIDI-Transposition (MIDI Transpose)
MIDI-Transposition verarbeitet MIDI-Ereignisse.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| MIDI | — | MIDI; MIDI-Ereignisse; MIDI |
| Transposition (Transpose) | −48–48 st; Standard: 0 st; cent, st, oct, Faktor | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| MIDI | MIDI; MIDI-Ereignisse |

## Verhalten
Versetzt neue Noten einschließlich gebrochener Halbtöne. Bereits gehaltene Noten behalten ihren ursprünglichen Versatz; die Zuordnung der Note-off-Ereignisse bleibt erhalten.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | MIDI → MIDI |
| Einfügen | MIDI → MIDI |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/midi-transpose.md)
