# Einheitenkonverter (Unit Converter)

## Übersicht

Rechnet eine Zahl zwischen kompatiblen Einheiten um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Von (From) | [Editor](../../user/de/reference-tables.md) | Unterstützte Einheiten | — | Faktor | — | — | Immer |
| Nach (To) | [Editor](../../user/de/reference-tables.md) | Kompatible Zieleinheiten | — | Prozent | — | — | Immer |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | Gewählte Quelleinheit | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±1e30 | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Rechnet über die gemeinsame Bezugsgröße der Einheiten. Zeit und Frequenz verwenden zusätzlich die reziproke Periodenumrechnung `T = 1/f`; null wird zu null statt unendlich. Die Zielauswahl folgt From. Dieser Node verändert Zahlen ausdrücklich; gewöhnliche Leitungen gleichen Einheiten nicht automatisch an. Siehe [Einheiten](../../user/de/units.md).

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Wert (Value) | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/unit-converter.md)
