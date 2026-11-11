# Triggerzusammenführung (Trigger Merge)
Führt mehrere Triggereingänge zu einem Strom zusammen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Eingänge (Inputs) | 2–16, ganzzahlig; Standard: 2 | Wert |
| Eingang 1…N (Input 1…N) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Trigger | Trigger |

## Verhalten
Ereignisse an unterschiedlichen Samples behalten ihre Position. Am selben Sample ankommende Ereignisse werden zu einem Ausgangstrigger zusammengefasst. Weniger Inputs entfernen hintere Ports und Verbindungen; Undo kann die Änderung wiederherstellen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang 1 (Input 1) → Trigger |
| Einfügen | Eingang 1 (Input 1) → Trigger |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-merge.md)
