# Teiltonanalyse

## Übersicht

Zerlegt ein Spektrum in verfolgte Teiltöne und ein komplexes Restspektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Schwelle (Threshold) | [Editor](../../user/de/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Inspektor |
| Maximale Teiltöne (Maximum partials) | [Editor](../../user/de/reference-tables.md) | 1–256 | — | 64 | — | — | Inspektor |
| Minimale Frequenz (Minimum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz | — | Inspektor |
| Maximale Frequenz (Maximum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz | — | Inspektor |
| Zuordnungstoleranz (Tracking tolerance) | [Editor](../../user/de/reference-tables.md) | 0–2400 cent | — | 100 cent | cent | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Rest (Residual) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Lokale FFT-Maxima werden logarithmisch interpoliert. Die Zuordnung erfolgt nach Frequenzabstand in Cent; neue Teiltöne erhalten eine neue ID. Alterswerte steigen mit der Hop-Dauer. Die stärksten Kandidaten werden bis zur eingestellten Grenze übernommen. Bei Kreuzungen oder verlorenen Peaks ist die Identität nicht garantiert. Die Analyse subtrahiert rekonstruierte komplexe Fensterkeulen vom Original; schwache Anteile und nicht modellierte Ausläufer bleiben im Rest. Negative Startframes bleiben vollständig im Rest.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Nein | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Teiltöne zu Spektrum](partials-to-spectrum.md) · [Spektrummathematik](spectrum-math.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/partial-analysis.md)
