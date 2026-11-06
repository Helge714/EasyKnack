# Teiltöne zu Spektrum

## Übersicht

Erzeugt aus Teiltönen ein komplexes Spektrum für IFFT.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/rates.md#auto) |

## Verhalten

Rekonstruiert positive und negative Frequenzanteile mit der Fensterfunktion der Analyse. Frequenzänderungen werden zwischen Frames in eine kontinuierliche Phasenkorrektur integriert. Frequenzen außerhalb 0–Nyquist und nichtpositive Amplituden werden verworfen. Unveränderte Teiltöne und der zugehörige Rest ergeben wieder das ursprüngliche Spektrum. Die Keulenberechnung ist auf acht Fenster-Bins je Seite begrenzt; verbleibende Originalausläufer liegen im Rest.

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

[Teiltonanalyse](partial-analysis.md) · [Spektrummischung](spectrum-mix.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/partials-to-spectrum.md)
