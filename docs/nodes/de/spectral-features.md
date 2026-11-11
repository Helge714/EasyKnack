# Spektrale Merkmale (Spectral Features)

## Übersicht

Misst die Frequenzverteilung und ihre Änderung zwischen Frames.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | — | Spektrum- oder Analyseressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Frequenzskala (Frequency scale) | [Editor](../../user/de/reference-tables.md) | Linear / Logarithmisch | — | Linear | — | — | Inspektor |
| Gewichtung (Weighting) | [Editor](../../user/de/reference-tables.md) | Power / Magnitude | — | Power | — | — | Immer |
| Roll-off-Anteil (Roll-off fraction) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0.85 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | — | Immer |
| Flux normieren (Normalize flux) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Zentrum (Centroid) | [Wert](../../user/de/data-types.md#value) | 0–Nyquist Hz | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Streuung (Spread) | [Wert](../../user/de/data-types.md#value) | ≥ 0 Hz / oct | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Schiefe (Skewness) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Kurtosis (Kurtosis) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Flux | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Roll-off (Rolloff) | [Wert](../../user/de/data-types.md#value) | 0–Nyquist Hz | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Neigung (Slope) | [Wert](../../user/de/data-types.md#value) | ±∞ / Hz | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Flachheit (Flatness) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Sei A[k] = abs(X[k])/Σw[n] und m[k] = 1 bei DC/Nyquist, sonst 2. Die Gewichtung ist W[k] = m[k]·A[k]² für Power oder m[k]·A[k] für Magnitude. Setze p[k] = W[k]/ΣW. Lineare Momente verwenden z[k] = f[k]; logarithmische Momente verwenden z[k] = log2(f[k]), ohne DC und mit neu normierten verbleibenden Gewichten.

- Zentrum: μ = Σp·z; logarithmischer Ausgang ist 2^μ Hz.
- Streuung: σ = sqrt(Σp·(z−μ)²), in Hz oder Oktaven.
- Schiefe: Σp·(z−μ)³/σ³.
- Kurtosis: Σp·(z−μ)⁴/σ⁴; Gauß = 3.
- Flux: sqrt(Σ(current−previous)²), normiert mit p, sonst mit W. Erster Frame = 0.
- Roll-off: erste Frequenz, deren kumuliertes Gewicht den gewählten Anteil erreicht.
- Neigung: Steigung der linearen Ausgleichsgeraden von p über f, je Hz.
- Flachheit: geometrisches Mittel der nach unten begrenzten W geteilt durch ihr arithmetisches Mittel; Untergrenze = max(1e−300, mean(W)·1e−12).

Die logarithmische Skala verändert nur die vier Momente. Stille und undefinierte Momente bei Streuung null ergeben null. Ausgänge halten zwischen Frames. Die mittlere Flachheit einer einzelnen Rausch-FFT liegt unter 1; vorherige Leistungsmittelung verringert die Schätzstreuung.

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

[Node-Verzeichnis](index.md) · [English](../en/spectral-features.md)
