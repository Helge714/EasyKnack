# Tonhöhe halten (Pitch Hold)

## Übersicht

Hält die letzte ausreichend sichere Frequenz.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 0–20000 Hz | ≥ 0 Hz | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Konfidenz (Confidence) | [Wert](../../user/de/data-types.md#value) | 0–1 | ±∞ | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Minimale Konfidenz (Minimum confidence) | [Wert](../../user/de/data-types.md#value) | 0–1 | ±∞ | 0.8 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto / Audio / Control | — | Auto | — | — | Inspektor |
| Anfangsfrequenz (Initial frequency) | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequenz [Hz] (Frequency [Hz]) | [Wert](../../user/de/data-types.md#value) | > 0 Hz | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |
| Gültig (Valid) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Akzeptiert positive endliche Frequenz bei Konfidenz ≥ minimale Konfidenz. Sonst bleibt der zuletzt akzeptierte Wert stehen; davor gilt Anfangsfrequenz. Gültig beschreibt den aktuellen Eingang, auch während der Ausgang hält. Es gibt keine automatische Überblendung oder Glättung.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Frequenz (Frequency) | Frequenz [Hz] (Frequency [Hz]) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Frequenz (Frequency) | Frequenz [Hz] (Frequency [Hz]) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/pitch-hold.md)
