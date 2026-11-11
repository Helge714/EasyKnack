# Werteingang (Value Input)

## Übersicht

Stellt einen editierbaren Zahlenwert bereit.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | Eingestellte Grenzen | — | 0 | — | — | Immer |
| Reglertyp (Slider type) | [Editor](../../user/de/reference-tables.md) | Buttons, Balken | — | Buttons | — | — | Inspektor |
| Minimum aktivieren (Enable minimum) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Inspektor |
| Minimum (Minimum) | [Wert](../../user/de/data-types.md#value) | ±∞ | — | 0 | — | — | Inspektor |
| Maximum aktivieren (Enable maximum) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Inspektor |
| Maximum (Maximum) | [Wert](../../user/de/data-types.md#value) | ±∞ | — | 1 | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wertausgang (Value Out) | [Wert](../../user/de/data-types.md#value) | Eingestellte Grenzen | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Buttons erlauben optionale Grenzen. Ein Balken benötigt beide Grenzen. Geänderte Grenzen begrenzen auch den Wert; ein ungültiges Maximum wird über das Minimum angehoben. Der Ausgang aktualisiert einmal pro Block, auch bei interpolierter Reglerbewegung.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/value-input.md)
