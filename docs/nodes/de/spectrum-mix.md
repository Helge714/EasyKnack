# Spektrummischung

## Übersicht

Überblendet zwei zeitlich passende komplexe Spektren.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Linear, Equal Power | — | Linear | — | — | Immer |
| Faktor (Factor) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0.5 | Factor, % | [Control](../../user/de/rates.md#control) | Immer |
| A | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| B | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Linear berechnet `A * (1 − f) + B * f`. Equal Power verwendet `cos(πf/2)` und `sin(πf/2)` als Gewichte. Faktor 0 liefert A, Faktor 1 liefert B; der Faktor wird auf 0–1 begrenzt und pro FFT-Frame mit dem Control-Wert des aktuellen Blocks angewendet. Equal Power erhält die erwartete Leistung unkorrelierter Signale; gleiche Signale werden in der Mitte um etwa 3 dB verstärkt. Gegenphasige Koeffizienten können sich auslöschen. FFT-Größe, Fenster, Hop, Samplerate und Frame-Positionen müssen übereinstimmen; nicht passende Batches erzeugen ein leeres Spektrum. Ein unverbundener Eingang ist 0. Für Teiltöne plus Rest stattdessen Spectrum Math mit Add verwenden.

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

[Spectrum Math](spectrum-math.md)
