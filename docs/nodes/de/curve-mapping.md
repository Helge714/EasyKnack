# Kurvenabbildung (Curve Mapping)
Bildet Werte durch eine editierbare Kurve ab.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Kurve (Curve) | Kurveneditor; Standard: Identität | Editor |
| Außerhalb des Bereichs (Outside range) | Begrenzen, Linear fortsetzen; Standard: Begrenzen | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Wert (Value) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Eingangsminimum (Input min) | ±1 000 000; Standard: 0 | Wert |
| Eingangsmaximum (Input max) | ±1 000 000; Standard: 1 | Wert |
| Ausgangsminimum (Output min) | ±1 000 000; Standard: 0 | Wert |
| Ausgangsmaximum (Output max) | ±1 000 000; Standard: 1 | Wert |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±1e12 |

## Verhalten
Normiert den Eingang anhand Input min/max, liest die Kurve und skaliert auf Output min/max. Clamp hält außerhalb des Quellbereichs die Kurvenendwerte; Linear extension setzt die Randsteigungen fort. Ein Quellbereich der Breite null liefert Output min. Unterstützt Bézier-Kontrollpunkte und die direkte Verwendung in Spectral Groups.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Wert (Value) → Ausgang (Out) |
| Einfügen | Wert (Value) → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/curve-mapping.md)
