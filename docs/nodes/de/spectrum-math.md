# Spektrummathematik

## Übersicht

Verknüpft zwei zeitlich passende Spektren durch komplexe Grundrechenarten.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/de/reference-tables.md) | Add, Subtract, Multiply, Divide | — | Add | — | — | Immer |
| A | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| B | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Verstärkung A (Gain A) | [Editor](../../user/de/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Inspektor |
| Verstärkung B (Gain B) | [Editor](../../user/de/reference-tables.md) | 0–∞ | — | 1 | Factor, % | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Berechnet je komplexem FFT-Koeffizienten `op(A * Gain A, B * Gain B)`. Add und Subtract erhalten die Phasenbeziehungen. Multiply ist komplexe Multiplikation und entspricht keiner sampleweisen Multiplikation der Audiosignale. Division durch null sowie nicht endliche Ergebnisse liefern 0. Es gibt keine automatische Lautstärkennormierung. Ein unverbundener Eingang ist 0. Beide Spektren müssen FFT-Größe, Fenster, Hop, Samplerate und Frame-Positionen teilen; nicht passende Batches erzeugen ein leeres Spektrum. Für Teiltonrekonstruktion Add und beide Verstärkungen 1 verwenden. Alte gewichtete Spectrum-Mix-Nodes werden beim Laden mit unveränderten Verbindungen und Verstärkungen in diesen Node umgewandelt.

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

[English](../en/spectrum-math.md)
