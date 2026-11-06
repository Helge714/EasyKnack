# Teilton-Gruppe

## Übersicht

Bearbeitet jeden erkannten Teilton einmal pro Analyseframe.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/rates.md#auto) |

## Verhalten

Standardaufbau: `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Mix → IFFT`; den Rest der Analyse mit dem zweiten Mix-Eingang verbinden. Im Inneren `Partial Context → Math / Formula Group → Partial Output` verwenden. Frequenz, Amplitude und Phase bleiben unabhängige Werte. Zustände sind pro ID getrennt und werden bei einer neuen ID zurückgesetzt. Maximal 64 numerische Prozessoren; gewöhnliche verschachtelte Gruppen und numerische Formula Groups werden aufgelöst. Keine Audio-Oszillatorbank, Buffer oder Triggerprogramme. Externe numerische Werte werden am Fensterende aus dem aktuellen Veröffentlichungsblock abgetastet; asynchrone historische Modulationswerte sind nicht gepuffert.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Teiltonanalyse](partial-analysis.md) · [Teiltöne zu Spektrum](partials-to-spectrum.md) · [Spektrummischung](spectrum-mix.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md)

[English](../en/partial-group.md)
