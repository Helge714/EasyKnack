# Autokorrelation (Autocorrelation)

## Übersicht

Berechnet die Autokorrelation aus einem vorhandenen Spektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Normierung (Normalization) | [Editor](../../user/de/reference-tables.md) | Biased / Unbiased | — | Biased | — | — | Immer |
| Glättungsfaktor (Smoothing factor) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 1 | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Korrelation (Correlation) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | ±1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

`r = IFFT(|X|²)`, auf Lag 0 normiert; Stille ergibt null. FFT-Padding 2x liefert lineare, ohne Padding zirkuläre Korrelation. Unbiased kompensiert die abnehmende Sampleüberlappung bei größeren linearen Lags und begrenzt auf ±1; die Fensterabschwächung wird nicht rückgängig gemacht. Für ungefensterte, gleichanteilbereinigte Korrelation wähle Rectangular, 2x Padding und Remove DC im [FFT](fft.md). Je Frame gilt `held = old·(1 − factor) + new·factor`, initialisiert mit dem ersten Frame. Faktor 0 hält diesen Frame, 1 aktualisiert sofort. [Correlation Features](correlation-features.md) reduziert das Ergebnis auf skalare Kennwerte.

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

[Node-Verzeichnis](index.md) · [English](../en/autocorrelation.md)
