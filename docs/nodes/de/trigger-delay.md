# Triggerverzögerung (Trigger Delay)
Verzögert Trigger und kann Trigger-Feedbackschleifen absichern.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Zeit (Time) | 1 smp–10 s; Standard: 0.25 s; s, ms, smp | Wert; 1 smp–10 s; Control |
| Zurücksetzen (Reset) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Trigger | Trigger |

## Verhalten
Time wird auf Samples gerundet und auf mindestens ein Sample begrenzt. Bereits geplante Ereignisse behalten bei späteren Time-Änderungen ihren Fälligkeitstermin. Reset löscht wartende Ereignisse. Mehrere am selben Sample fällige Ereignisse werden zu einem Trigger zusammengefasst. Der Delay ist eine Ausführungsgrenze für Feedback; seine Zeit muss von außerhalb der Schleife gesteuert werden.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Trigger → Trigger |
| Einfügen | Trigger → Trigger |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-delay.md)
