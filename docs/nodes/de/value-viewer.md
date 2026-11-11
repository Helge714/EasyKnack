# Werteanzeige (Value Viewer)
Zeigt Zahleneingänge als Werte oder Zeitverläufe.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Werte (Values) | Graph / Werte; Standard: Graph | Editor |
| Eingänge (Inputs) | 1–8; Standard: 1 | Editor |
| Zeitfenster (Time window) | 0.1–600 s; Standard: 10 s; s, ms, smp | Editor |
| Jedes Sample aufzeichnen (Record every sample) | Aus / Ein; Standard: Aus | Editor; Inspektor |
| Kanal (Channel) | Links / Rechts / Mix; Standard: Links | Editor; Inspektor |
| Werte: Namen, Einheiten, Reihenfolge (Values: names, units, order) | Einstellungen je Eingang; Standard: Wert 1…8; keine Einheit | Editor; Inspektor |
| Wert 1…8 (Value 1…8) | — | Wert; ±∞; Auto; Entsprechend Eingänge |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
Eingänge bestimmt Ports und dargestellte Reihen. Die Inspektorzeilen ändern Namen, Einheitenbeschriftungen und Reihenfolge gemeinsam für Ports, Anzeige und Export; Einheitenbeschriftungen rechnen Werte nicht um. Verlauf zeigt getrennte Graphen oder eine Überlagerung; Werte zeigt einzelne Zahlen. Anhalten hält einen Schnappschuss. Vergrößerte Ansichten erlauben Zoom, Verschieben und Messcursor; Bilder und Tabellen können den gesamten Verlauf oder sichtbaren Ausschnitt exportieren.

Ohne Jedes Sample aufzeichnen bleiben je 10-ms-Intervall Minimum, Maximum und letzter Wert erhalten. Aktiviert wird jedes Sample gespeichert. Die Historie ist auf eine Million Eingangsdatensätze begrenzt; die effektive Dauer kann kürzer als Zeitfenster sein. Transportlücken bleiben sichtbare Lücken. Controlwerte halten über ihren Block; Mix beobachtet (L+R)/2.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/value-viewer.md)
