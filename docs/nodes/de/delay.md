# Verzögerung (Delay)
Verzögert ein Signal und sichert Feedbackschleifen ab.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Eingang (In) | ±∞; Standard: 0 | Audio; ±∞; Audio |
| Zeit (Time) | 0–10 s; Standard: 0.25 s; s, ms, smp | Wert; 0–10 s; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Audio; ±∞ |

## Verhalten
Time wird einmal pro Block gelesen. Bruchteile von Samples werden linear interpoliert. Null reicht außerhalb von Feedback durch; innerhalb wird mindestens ein Sample erzwungen. Der Ausführungsplan wählt je nach Schleifenverzögerung blockgroße oder kürzere Schritte. Die Zeitmodulation muss außerhalb dieser Schleife entstehen. Mute entfernt die Verzögerungsgrenze; eine dadurch ungesicherte Verbindung wird ignoriert statt als unmittelbares Feedback berechnet.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/delay.md)
