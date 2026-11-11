# Begrenzung (Clamp)
Begrenzt, faltet oder wrappt Werte um ±1.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Clip, Foldback, Wrap, Soft Clip; Standard: Clip | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Eingang (In) | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±1 |

## Verhalten
Clip begrenzt auf ±1. Foldback spiegelt Überschreitungen an den Grenzen. Wrap wiederholt ein Intervall der Breite 2. Soft Clip berechnet `tanh(x)` und nähert sich stetig ±1.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/clamp.md)
