# Instanzkontext (Instance Context)
Stellt Index und Anzahl der aktuellen Instanzen bereit.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| — | — | — |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Index | Wert; 0–Count−1; Control |
| Anzahl (Count) | Wert; 0–Maximum; Control |
| Position | Wert; 0–1; Control |
| Startwert (Seed) | Wert; 0–16 777 215; Control |

## Verhalten
Innerhalb einer [Instance Group](instance-group.md) ist Index nullbasiert und Count die aktive Anzahl. Position ist bei mehreren Instanzen `Index/(Count−1)`, sonst 0. Seed ist je Instanz stabil. Ein verbundener Index oder Seed macht sonst gleiche Generatoren instanzabhängig.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/instance-context.md)
