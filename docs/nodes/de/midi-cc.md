# MIDI CC
Wandelt einen MIDI-Controller in einen gehaltenen Zahlenwert um.
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
| Controller | 0–127; Standard: 1 | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wert (Value) | Wert; 0–1; Audio |

## Verhalten
Wert = `CC/127`; Änderungen behalten ihre Sampleposition, der letzte Wert bleibt stehen. Controller 1 ist Modulation, 7 Lautstärke und 64 Haltepedal. Kanal 0 akzeptiert alle MIDI-Kanäle.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/midi-cc.md)
