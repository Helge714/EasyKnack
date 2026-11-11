# Vektor drehen (Vector Rotate)
Dreht einen Vektor um einen Ursprung.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Vektor (Vector) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Rotation | ±∞ je Komponente; Standard: (0, 0, 0)°; °, rad, turn | Vektor; ±∞ je Komponente; Auto |
| Ursprung (Origin) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Vektor (Vector) | Vektor; ±∞ je Komponente |

## Verhalten
`vout = origin + Rz Ry Rx (v − origin)`. Verwendet rechtshändige Eulerrotationen: zuerst X, dann Y, dann Z. Winkel sind periodisch; eine laufende Drehung muss nicht manuell bei 360° gewrappt werden. Die Reihenfolge ist relevant. Origin und Vector verwenden Längenkomponenten, Rotation Winkel.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Einfügen | Vektor (Vector) → Vektor (Vector) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-rotate.md)
