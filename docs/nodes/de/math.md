# Mathematik (Math)
Wendet eine wählbare mathematische Operation an.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide, Power, Remainder, Modulo, Minimum, Maximum, Floor, Ceil, Round, Fract, Sqrt, Root, Exp, Log, Log Base, Absolute, Negate; Standard: Multiply | Editor |
| Begrenzen ±1 (Clamp ±1) | Aus / An; Standard: Aus | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| A | ±1 000 000; Standard: 0 | Wert; ±∞; Auto |
| B | ±1 000 000; Standard: 1 | Wert; ±∞; Auto; Nur binäre Operation |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞ |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Binäre Operationen verknüpfen A und B; unäre blenden B aus. Operandennamen und automatische Standardwerte folgen der Operation. Remainder folgt dem Vorzeichen des Dividenden; Modulo verwendet einen floor-basierten Rest. Fract ist `x − floor(x)`. Division durch nahezu null und ungültige Definitionsbereiche liefern abgesicherte Werte; nicht endliche Ergebnisse werden null. Clamp begrenzt das Ergebnis auf ±1. Verbundene Eingänge behalten ihre Rate. Dieselben Operationen sind in [Spectral Groups](spectral-group.md) verfügbar.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | A → Ausgang (Out) |
| Einfügen | A → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/math.md)
