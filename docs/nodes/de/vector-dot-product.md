# Skalarprodukt (Vector Dot Product)
Berechnet das Skalarprodukt zweier Vektoren.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| A | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vektor; ±∞ je Komponente; Auto |
| B | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vektor; ±∞ je Komponente; Auto |
| Größe (Quantity) | Länge, Winkel, Skalierung, Einheitenlos; Standard: Länge | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Skalarprodukt (Dot Product) | Wert; ±∞ |

## Verhalten
`dot = Ax Bx + Ay By + Az Bz`. Bei normierten Vektoren ist dies der Kosinus ihres Winkels. Der Ausgang ist eine rohe Zahl; multiplizierte Längenkomponenten ergeben mathematisch quadrierte Längeneinheiten und keinen neuen Ortsvektor.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-dot-product.md)
