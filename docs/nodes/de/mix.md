# Mischung (Mix)

## Übersicht

Überblendet zwischen zwei numerischen Signalen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Linear, Equal Power | — | Linear | — | — | Immer |
| Faktor (Factor) | [Wert](../../user/de/data-types.md#value) | 0–1 × | 0–1 × | 0.5 × | [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| A | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| B | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Linear: `y = (1−t)A + tB`. Equal Power: `y = cos(πt/2)A + sin(πt/2)B`, mit t auf 0–1 begrenzt. Equal Power erhält die erwartete Leistung unkorrelierter Signale; gleiche Eingänge können in der Mitte etwa 3 dB gewinnen. Linear erhält den Pegel gleicher Eingänge.

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

[Node-Verzeichnis](index.md) · [English](../en/mix.md)
