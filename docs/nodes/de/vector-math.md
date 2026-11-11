# Vektormathematik (Vector Math)
Berechnet Operationen mit vektorwertigem Ergebnis.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide, Scale, Normalize, Negate, Absolute, Minimum, Maximum, Cross; Standard: Add | Editor |
| A | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vektor; ±∞ je Komponente; Auto |
| B | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vektor; ±∞ je Komponente; Auto; Binäre Operation |
| Faktor (Factor) | ±∞; Standard: 1 | Wert; ±∞; Auto; Scale-Operation |
| Größe (Quantity) | Länge, Winkel, Skalierung, Einheitenlos; Standard: Länge | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Vektor (Vector) | Vektor; ±∞ je Komponente |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Add/Subtract/Multiply/Divide/Minimum/Maximum arbeiten komponentenweise. Division durch eine Nullkomponente liefert dort null. Scale multipliziert A mit Factor. Normalize liefert `A/length(A)` oder bei Länge null den Nullvektor. Cross berechnet das rechtshändige Kreuzprodukt. Negate und Absolute wirken je Komponente. Skalare Ergebnisse liefern [Vector Length](vector-length.md), [Vector Distance](vector-distance.md) und [Vector Dot Product](vector-dot-product.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Einfügen | A → Vektor (Vector) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-math.md)
