# Bin abtasten und halten (Bin Sample & Hold)
Erfasst Werte getrennt für jeden Bin und Kanal.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wert (Value) | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Erfassen (Capture) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wert (Value) | Wert; ±∞; Audio (Bin/Frame) |

## Verhalten
Vor der Erfassung werden Eingangswerte durchgereicht. Capture speichert den Wert des nächsten vollständigen FFT-Frames je Bin/Kanal und hält ihn bis zur nächsten Erfassung. Nur den Betrag zu halten verwendet weiterhin eine laufende Phase, falls sie direkt verdrahtet ist; dies ist kein eigenständiges Einfrieren des gesamten komplexen Spektrums. Nur in [Spectral Groups](spectral-group.md) gültig.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Wert (Value) → Wert (Value) |
| Einfügen | Wert (Value) → Wert (Value) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-sample-hold.md)
