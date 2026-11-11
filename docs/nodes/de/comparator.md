# Vergleicher (Comparator)
Wandelt einen Zahlenvergleich in 0 oder 1 um.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Operation | <, <=, >, >=, ==, !=; Standard: < | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| A | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| B | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; 0–1 |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Gibt bei erfülltem Vergleich 1 aus, sonst 0. Gleichheit vergleicht die Zahlen direkt und ist kein Toleranztest. Der Ausgang ist ein numerisches Gate, kein Trigger. Für Ereignisse dient [Threshold Trigger](threshold-trigger.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | A → Ausgang (Out) |
| Einfügen | A → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/comparator.md)
