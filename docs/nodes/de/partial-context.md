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
| Frequenz (Frequency) | [Value](../../user/de/data-types.md#value) | 0–Nyquist Hz | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Amplitude | [Value](../../user/de/data-types.md#value) | 0–∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Phase | [Value](../../user/de/data-types.md#value) | ±∞ turn | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| ID | [Value](../../user/de/data-types.md#value) | 1–∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Alter (Age) | [Value](../../user/de/data-types.md#value) | 0–∞ s | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Index | [Value](../../user/de/data-types.md#value) | 0–32767 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Frame-Zeit (Frame time) | [Value](../../user/de/data-types.md#value) | 0–∞ s | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Seed | [Value](../../user/de/data-types.md#value) | 0–16 777 215 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Klassifikationsabstand (Classification margin) | [Value](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die Werte gelten für den aktuellen Teilton im aktuellen Frame. Phase wird in Umdrehungen angegeben; Index bezeichnet die aktuelle Lautstärkesortierung, ID die verfolgte Identität. Alter und Frame-Zeit sind Sekunden. Zustände der Teilton-Gruppe werden anhand der ID zugeordnet.

ID bleibt die Teiltonkennung. Seed ist ein daraus abgeleiteter, exakt als Float darstellbarer 24-Bit-Wert für die Seed-Eingänge gewöhnlicher Noise-Nodes; er ist keine eindeutige Kennung. Partial Noise verwendet die Teilton-ID direkt und benötigt diese Verbindung nicht.

Klassifikationsabstand ist der kleinste normierte Abstand zu einer Klassifikationsgrenze: 0 an einer Grenze, Richtung 1 im Inneren des akzeptierten Bereichs. Er ist keine Wahrscheinlichkeit. Bei deaktivierter Klassifikation beträgt er 1.

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
