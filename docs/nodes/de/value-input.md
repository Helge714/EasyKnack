# Werteingang (Value Input)
Stellt einen editierbaren Zahlenwert bereit.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wert (Value) | Eingestellte Grenzen; Standard: 0 | Wert |
| Reglertyp (Slider type) | Buttons, Balken; Standard: Buttons | Editor; Inspektor |
| Minimum aktivieren (Enable minimum) | Aus / An; Standard: Aus | Editor; Inspektor |
| Minimum (Minimum) | ±∞; Standard: 0 | Wert; Inspektor |
| Maximum aktivieren (Enable maximum) | Aus / An; Standard: Aus | Editor; Inspektor |
| Maximum (Maximum) | ±∞; Standard: 1 | Wert; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wertausgang (Value Out) | Wert; Eingestellte Grenzen; Control |

## Verhalten
Buttons erlauben optionale Grenzen. Ein Balken benötigt beide Grenzen. Geänderte Grenzen begrenzen auch den Wert; ein ungültiges Maximum wird über das Minimum angehoben. Der Ausgang aktualisiert einmal pro Block, auch bei interpolierter Reglerbewegung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/value-input.md)
