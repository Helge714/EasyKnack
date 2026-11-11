# Abtasten und Halten (Sample & Hold)
Erfasst und hält einen Wert bei einem Trigger.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Eingang (In) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Trigger | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞ |

## Verhalten
Jeder Trigger erfasst den Eingang an seiner Sampleposition; das Ergebnis bleibt bis zum nächsten Trigger erhalten. Der anfängliche Haltewert ist 0. Auto wählt bei verbundenem Trigger Audio, damit Ereignisse innerhalb des Blocks sichtbar bleiben. Erzwungenes Control meldet den Blockanfangswert; spätere Erfassungen wirken auf den folgenden Ausgang. Manuelle Erfassung erfolgt zum nächsten Block.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/sample-hold.md)
