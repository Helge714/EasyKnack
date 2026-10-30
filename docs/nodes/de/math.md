# Mathematik (Math)

## Übersicht

Wendet eine wählbare mathematische Operation an.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/de/reference-tables.md) | Add, Subtract, Multiply, Divide, Power, Remainder, Modulo, Minimum, Maximum, Floor, Ceil, Round, Fract, Sqrt, Root, Exp, Log, Log Base, Absolute, Negate | — | Multiply | — | — | Immer |
| Begrenzen ±1 (Clamp ±1) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| A | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| B | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/de/rates.md#auto) | Nur binäre Operation |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Binäre Operationen verknüpfen A und B; unäre blenden B aus. Operandennamen und automatische Standardwerte folgen der Operation. Remainder folgt dem Vorzeichen des Dividenden; Modulo verwendet einen floor-basierten Rest. Fract ist `x − floor(x)`. Division durch nahezu null und ungültige Definitionsbereiche liefern abgesicherte Werte; nicht endliche Ergebnisse werden null. Clamp begrenzt das Ergebnis auf ±1. Verbundene Eingänge behalten ihre Rate. Dieselben Operationen sind in [Spectral Groups](spectral-group.md) verfügbar.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | A | Ausgang (Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| A | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/math.md)
