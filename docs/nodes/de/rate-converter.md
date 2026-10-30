# Ratenkonverter (Rate Converter)

## Übersicht

Wandelt numerische Signale zwischen Audio und Control um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Eingang (In) | [Wert](../../user/de/data-types.md) | ±∞ | ±∞ | 0 | — | [Audio](../../user/de/rates.md) | Immer |
| Ausgangsrate (Output Rate) | [Editor](../../user/de/reference-tables.md) | Control, Audio | — | Control | — | — | Immer |
| Reduktion (Reduction) | [Editor](../../user/de/reference-tables.md) | Erstes, Letztes, Mittelwert, RMS, Minimum, Maximum, Absolutspitze | — | Erstes | — | — | Control-Ausgang |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md) | ±∞ | [Audio / Control](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Audio erhält die Samples. Control reduziert jeden tatsächlichen Block mit der gewählten Methode und hält das Ergebnis. Mean ist vorzeichenbehaftet; RMS und Absolutspitze sind nichtnegativ. Beim Hochsetzen wird Control gehalten; es entstehen keine Zwischenwerte. Trigger werden mit eigenen Nodes konvertiert.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Eingang (In) | Ausgang (Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Eingang (In) | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/rate-converter.md)
