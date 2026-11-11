# Bereichsabbildung (Map Range)
Bildet einen Zahlenbereich linear auf einen anderen ab.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Begrenzen ±1 (Clamp ±1) | Aus / An; Standard: Aus | Editor |
| Wert (Value) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Quellminimum (From Min) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Quellmaximum (From Max) | ±1 000 000; Standard: 1 | Wert; ±∞; Auto |
| Zielminimum (To Min) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| Zielmaximum (To Max) | ±1 000 000; Standard: 1 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞ |

## Verhalten
`t = (x − From Min) / (From Max − From Min)`; `y = To Min + t × (To Max − To Min)`. Clamp begrenzt t auf 0–1, trotz der Beschriftung nicht den Ausgang auf ±1. Ohne Clamp wird außerhalb des Quellbereichs extrapoliert. Ein entarteter Quellbereich liefert To Min. Umgekehrte Intervalle kehren die Abbildung um.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Wert (Value) → Ausgang (Out) |
| Einfügen | Wert (Value) → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/map-range.md)
