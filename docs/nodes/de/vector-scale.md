# Vektor skalieren (Vector Scale)
Skaliert jede Vektorachse um einen Ursprung.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Vektor (Vector) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Skalierung (Scale) | ±∞ je Komponente; Standard: (1, 1, 1) ×; ×, % | Vektor; ±∞ je Komponente; Auto |
| Ursprung (Origin) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Vektor (Vector) | Vektor; ±∞ je Komponente |

## Verhalten
`vout = origin + scale ⊙ (v − origin)`, wobei ⊙ komponentenweise multipliziert. Skalierung 1 erhält eine Achse, 0 reduziert sie auf den Ursprung, negative Werte spiegeln sie. Dieser Node dreht nicht.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Einfügen | Vektor (Vector) → Vektor (Vector) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-scale.md)
