# Vektorlänge (Vector Length)
Berechnet die euklidische Vektorlänge.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Vektor (Vector) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vektor; ±∞ je Komponente; Auto |
| Größe (Quantity) | Länge, Winkel, Skalierung, Einheitenlos; Standard: Länge | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Länge (Length) | Wert; 0–∞ |

## Verhalten
`length = sqrt(X² + Y² + Z²)`. Der Ausgang verwendet die Einheitendarstellung der gewählten Größe. Winkelkomponenten werden als Zahlenkoordinaten behandelt, nicht als kürzeste gewrappte Winkelabstände.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-length.md)
