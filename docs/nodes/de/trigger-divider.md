# Triggerteiler (Trigger Divider)
Gibt jeden n-ten Eingangstrigger weiter.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Teilung (Division) | 1–1024, ganzzahlig; Standard: 2 | Wert; 1–1024, gerundet; Control |
| Zurücksetzen (Reset) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Trigger | Trigger |

## Verhalten
Teilung n gibt beim n-ten Ereignis aus und beginnt erneut zu zählen. Reset löscht den Zähler; ein gleichzeitiger Eingang wird danach gezählt. Teilung 1 gibt jedes Ereignis weiter. Eine geänderte Teilung erzeugt selbst keinen Trigger.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Trigger → Trigger |
| Einfügen | Trigger → Trigger |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-divider.md)
