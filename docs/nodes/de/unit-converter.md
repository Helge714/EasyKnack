# Einheitenkonverter (Unit Converter)
Rechnet eine Zahl zwischen kompatiblen Einheiten um.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Von (From) | Unterstützte Einheiten; Standard: Faktor | Editor |
| Nach (To) | Kompatible Zieleinheiten; Standard: Prozent | Editor |
| Wert (Value) | ±∞; Standard: 0; Gewählte Quelleinheit | Wert; ±∞; Auto |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±1e30 |

## Verhalten
Rechnet über die gemeinsame Bezugsgröße der Einheiten. Zeit und Frequenz verwenden zusätzlich die reziproke Periodenumrechnung `T = 1/f`; null wird zu null statt unendlich. Die Zielauswahl folgt From. Dieser Node verändert Zahlen ausdrücklich; gewöhnliche Leitungen gleichen Einheiten nicht automatisch an. Siehe [Einheiten](../../user/de/units.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Einfügen | Wert (Value) → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/unit-converter.md)
