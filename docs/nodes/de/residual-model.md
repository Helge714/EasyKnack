# Residualmodell

## Übersicht

Modelliert die spektrale Hüllkurve eines Residuals und synthetisiert daraus ein stochastisches Spektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rest (Residual) | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Hüllkurvenbreite (Envelope width) | [Value](../../user/de/data-types.md#value) | 0–20000 Hz | 0–∞ Hz | 200 Hz | Hz, kHz | [Control](../../user/de/rates.md#control) | Immer |
| Hüllkurvenglättung (Envelope smoothing) | [Editor](../../user/de/reference-tables.md) | 0–10000 ms | — | 0 ms | ms, s, smpl | — | Inspektor |
| Seed | [Editor](../../user/de/reference-tables.md) | 0–2147483647 | — | 0 | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Mittelt die Leistung benachbarter Bins innerhalb der Hüllkurvenbreite (volle Breite in Hz) und erzeugt für jeden Frame neue, reproduzierbare Zufallsphasen. 0 Hz erhält die spektralen Einzelleistungen vor der Fensterung. Hüllkurvenglättung ist eine exponentielle Leistungsglättung mit der angegebenen Zeitkonstante. Das generierte Signal erhält die Fensterung der Quelle; die komplexe Konjugiertsymmetrie und ein entfernter Gleichanteil bleiben erhalten. Die Frame-Leistung wird an die geglättete Hüllkurve angepasst, nicht an wahrgenommene Lautheit. Seed und Frame-Position bestimmen die Zufallsfolge.

Die Modellierung ist absichtlich verlustbehaftet und keine Klassifikation. Für genaue Rekonstruktion das ursprüngliche Residual verwenden. FFT-Größe, Hop und Frame-Zeit bleiben unverändert; deshalb lässt sich das Ergebnis mit dem zugehörigen Teiltonspektrum addieren. Für eine unabhängige, kürzere Residualauflösung zuerst über IFFT in Audio zurückwandeln und mit einer eigenen FFT analysieren. Solche Zweige erst nach ihren IFFTs mischen und zusätzliche Latenzen ausgleichen. Die Fenstergröße wird nicht automatisch geändert.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Rest (Residual) | Spektrum (Spectrum) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Rest (Residual) | Spektrum (Spectrum) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Teiltonanalyse](partial-analysis.md) · [Teiltöne zu Spektrum](partials-to-spectrum.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/residual-model.md)

[Spectrum Math](spectrum-math.md)
