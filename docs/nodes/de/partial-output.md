# Teilton-Ausgang

## Übersicht

Legt Frequenz, Amplitude und Phase des bearbeiteten Teiltons fest.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Value](../../user/de/data-types.md#value) | 0–∞ Hz | ±∞ Hz | 440 Hz | Hz | [Auto](../../user/de/data-types.md#channels) | Immer |
| Amplitude | [Value](../../user/de/data-types.md#value) | 0–∞ | ±∞ | 1 | — | [Auto](../../user/de/data-types.md#channels) | Immer |
| Phase | [Value](../../user/de/data-types.md#value) | ±∞ turn | ±∞ turn | 0 turn | turn, °, rad | [Auto](../../user/de/data-types.md#channels) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Frequenz, Amplitude und Phase bestimmen den Ausgangsteilton. Für unveränderten Durchlauf die drei entsprechenden Context-Ausgänge verbinden. Phase wird in Umdrehungen angegeben. Die ursprünglichen Analysedaten bleiben für die spätere kontinuierliche Synthese erhalten.

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

[Teiltonanalyse](partial-analysis.md) · [Teiltöne zu Spektrum](partials-to-spectrum.md) · [Spektrummischung](spectrum-mix.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/partial-output.md)
