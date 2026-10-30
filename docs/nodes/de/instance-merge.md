# Instanzenzusammenführung (Instance Merge)

## Übersicht

Reduziert Instanzbeiträge zu einem gemeinsamen Signal.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Zusammenführung (Combine) | [Editor](../../user/de/reference-tables.md) | Summe, Mittelwert, Gewichteter Mittelwert | — | Mittelwert | — | — | Immer |
| Instanzsignal (Per-instance signal) | [Wert](../../user/de/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md) | Immer |
| Instanzgewicht (Per-instance weight) | [Wert](../../user/de/data-types.md) | 0–1 | 0–∞ | 1 | — | [Auto](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Zusammengeführt (Combined) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Wird innerhalb von [Instance Groups](instance-group.md) und deren spektraler Entsprechung verwendet. Sum ist `Σ(wᵢxᵢ)`, Mean teilt dies durch die aktive Anzahl, Weighted Mean durch `Σwᵢ`. Negative Gewichte werden null; Gesamtgewicht null ergibt Stille. Nodes nach Merge bilden gemeinsame Verarbeitung. Mehrere Merge-Zweige dürfen danach gemischt werden.

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

[Node-Verzeichnis](index.md) · [English](../en/instance-merge.md)
