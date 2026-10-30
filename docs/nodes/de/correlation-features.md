# Korrelationsmerkmale (Correlation Features)

## Übersicht

Reduziert Autokorrelation auf Periodizitäts-, Perioden- und Rauschkennwerte.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Autokorrelation (Autocorrelation) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | — | Korrelationsressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Minimale Frequenz (Minimum frequency) | [Editor](../../user/de/reference-tables.md) | ≥ 0.001 Hz | — | 20 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | — | Inspektor |
| Maximale Frequenz (Maximum frequency) | [Editor](../../user/de/reference-tables.md) | ≥ 0.001 Hz | — | 20000 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | — | Inspektor |
| Periodizitätsschwelle (Periodicity threshold) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0.3 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | — | Inspektor |
| Stille-Schwelle (Silence threshold) | [Editor](../../user/de/reference-tables.md) | ≥ 0 | — | 0.000001 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Periodizität (Periodicity) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Periode (Period) | [Wert](../../user/de/data-types.md#value) | ≥ 0 s | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | ≥ 0 Hz | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| HNR | [Wert](../../user/de/data-types.md#value) | ±120 dB | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Gültig (Valid) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Sucht positive lokale Korrelationsmaxima innerhalb der Frequenzgrenzen mit Subsample-Peakinterpolation. Mindestens drei Perioden müssen in das Analysefenster passen. Periodizität p ist das gewählte normierte Korrelationsmaximum, keine Wahrscheinlichkeit. Periode ist dessen Lag in Sekunden; Frequenz = 1/Periode. HNR = `10·log10(p/(1−p))`, begrenzt auf ±120 dB: 0 dB bedeutet gleich große geschätzte periodische und verbleibende Leistung, positive Werte bevorzugen periodische Leistung. Der Wert hängt von vorgeschaltetem Fenster und Normierung ab. Gültig = 1 nur bei erfüllter Peak- und Quell-RMS-Schwelle; sonst sind Periode/Frequenz null. Werte halten zwischen Frames. Verwende [Autokorrelation](autocorrelation.md) oder deren Mittelwert statt eines rohen Spektrums.

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

[Node-Verzeichnis](index.md) · [English](../en/correlation-features.md)
