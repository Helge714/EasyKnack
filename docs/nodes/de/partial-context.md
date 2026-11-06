# Teilton-Kontext

## Übersicht

Liefert die Werte des aktuellen Teiltons innerhalb einer Teilton-Gruppe.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Value](../../user/de/data-types.md#value) | 0–Nyquist Hz | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| Amplitude | [Value](../../user/de/data-types.md#value) | 0–∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| Phase | [Value](../../user/de/data-types.md#value) | ±∞ turn | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| ID | [Value](../../user/de/data-types.md#value) | 1–∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| Alter (Age) | [Value](../../user/de/data-types.md#value) | 0–∞ s | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| Index | [Value](../../user/de/data-types.md#value) | 0–255 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |
| Frame-Zeit (Frame time) | [Value](../../user/de/data-types.md#value) | 0–∞ s | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/rates.md#auto) |

## Verhalten

Die Werte gelten für den aktuellen Teilton im aktuellen Frame. Phase wird in Umdrehungen angegeben; Index bezeichnet die aktuelle Lautstärkesortierung, ID die verfolgte Identität. Alter und Frame-Zeit sind Sekunden. Zustände der Teilton-Gruppe werden anhand der ID zugeordnet.

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

[Teiltonanalyse](partial-analysis.md) · [Teiltöne zu Spektrum](partials-to-spectrum.md) · [Spektrummischung](spectrum-mix.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/partial-context.md)
