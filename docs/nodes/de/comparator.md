# Vergleicher (Comparator)

## Übersicht

Wandelt einen Zahlenvergleich in 0 oder 1 um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/de/reference-tables.md) | <, <=, >, >=, ==, != | — | < | — | — | Immer |
| Rate (Rate) | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| A | [Wert](../../user/de/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md) | Immer |
| B | [Wert](../../user/de/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md) | 0–1 | [Auto](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Gibt bei erfülltem Vergleich 1 aus, sonst 0. Gleichheit vergleicht die Zahlen direkt und ist kein Toleranztest. Der Ausgang ist ein numerisches Gate, kein Trigger. Für Ereignisse dient [Threshold Trigger](threshold-trigger.md).

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

[Node-Verzeichnis](index.md) · [English](../en/comparator.md)
