# Instanzenzusammenführung (Instance Merge)
Reduziert Instanzbeiträge zu einem gemeinsamen Signal.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Zusammenführung (Combine) | Summe, Mittelwert, Gewichteter Mittelwert; Standard: Mittelwert | Editor |
| Instanzsignal (Per-instance signal) | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Instanzgewicht (Per-instance weight) | 0–1; Standard: 1 | Wert; 0–∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Zusammengeführt (Combined) | Audio; ±∞ |

## Verhalten
Wird innerhalb von [Instance Groups](instance-group.md) und deren spektraler Entsprechung verwendet. Sum ist `Σ(wᵢxᵢ)`, Mean teilt dies durch die aktive Anzahl, Weighted Mean durch `Σwᵢ`. Negative Gewichte werden null; Gesamtgewicht null ergibt Stille. Nodes nach Merge bilden gemeinsame Verarbeitung. Mehrere Merge-Zweige dürfen danach gemischt werden.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/instance-merge.md)
