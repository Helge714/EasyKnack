# Triggerfilter (Trigger Filter)
Gibt Trigger nur bei positiver numerischer Bedingung weiter.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Bedingung (Condition) | 0–1; Standard: 1 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Trigger | Trigger |

## Verhalten
Am Triggersample erlaubt eine endliche Condition größer als 0 das Ereignis; andernfalls wird es verworfen. Eine geänderte Condition ohne Trigger erzeugt kein Ereignis. Ein [Comparator](comparator.md) ermöglicht indexabhängige Auswahl.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Trigger → Trigger |
| Einfügen | Trigger → Trigger |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-filter.md)
