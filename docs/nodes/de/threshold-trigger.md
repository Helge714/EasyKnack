# Schwellwerttrigger (Threshold Trigger)
Erzeugt Trigger beim Überschreiten einer Schwelle.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Flanke (Edge) | Steigend, Fallend, Beide; Standard: Steigend | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Eingang (In) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Schwelle (Threshold) | ±1 000 000; Standard: 0.5 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Trigger | Trigger |

## Verhalten
Vergleicht `Eingang > Schwelle`. Rising löst bei falsch→wahr aus, Falling bei wahr→falsch und Both bei beiden Änderungen. Der erste Wert initialisiert ohne Ereignis. Audio prüft jedes Sample, Control die Blockwerte. Auch eine geänderte Schwelle kann eine Kreuzung auslösen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/threshold-trigger.md)
