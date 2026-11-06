# Spektrummischung

## Übersicht

Addiert zwei zeitlich passende komplexe Spektren.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A | Spektrum (Spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| B | Spektrum (Spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Verstärkung A (Gain A) | [Editor](../../user/de/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Immer |
| Verstärkung B (Gain B) | [Editor](../../user/de/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/rates.md#auto) |

## Verhalten

Berechnet `A * Gain A + B * Gain B` auf den komplexen Koeffizienten. Für Teiltonrekonstruktion beide Verstärkungen auf 1 setzen. FFT-Größe, Fenster, Hop, Samplerate und Frame-Position müssen übereinstimmen. Nicht passende Batches erzeugen ein leeres Spektrum; es erfolgt keine automatische Neuberechnung. Ein unverbundener Eingang ist null.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | A | Spektrum (Spectrum) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| A | Spektrum (Spectrum) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Teiltonanalyse](partial-analysis.md) · [Teiltöne zu Spektrum](partials-to-spectrum.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/spectrum-mix.md)
