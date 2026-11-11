# Vektorabstand (Vector Distance)
Berechnet den euklidischen Abstand zweier Vektoren.
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
| Abstand (Distance) | Wert; 0–∞ |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

`distance = length(A − B)`. Liefert einen Abstand pro Kanal mit eingangsabhängiger Rate. Quantity wählt die Einheitendarstellung, keine andere Abstandsmetrik.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-distance.md)
