# Tonhöhenerkennung (Pitch Detection)

## Übersicht

Schätzt eine Grundfrequenz aus Autokorrelation.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Autokorrelation (Autocorrelation) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | — | Korrelationsressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Minimum | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 40 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | — | Immer |
| Maximum | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequenz [Hz] (Frequency [Hz]) | [Wert](../../user/de/data-types.md#value) | 0–20000 Hz | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Konfidenz (Confidence) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Überspringt die anfängliche Lag-0-Kuppe und wählt das früheste lokale Maximum mit mindestens 90% des stärksten Maximums im Suchbereich, sofern dieses 0.1 erreicht. Parabolische Interpolation verfeinert den Lag. Frequenz = 1/Lag; Konfidenz ist die normierte Korrelationshöhe. Ohne geeigneten Peak wird null ausgegeben. Verwende Rectangular-FFT, 2x Padding und mehrere Perioden der tiefsten erwarteten Frequenz. Minimum muss unter Maximum liegen. [Correlation Features](correlation-features.md) liefert zusätzlich Gültigkeit und HNR.

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
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/pitch-detection.md)
