# Instanzkontext (Instance Context)

## Übersicht

Stellt Index und Anzahl der aktuellen Instanzen bereit.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — | — | — |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Index | [Wert](../../user/de/data-types.md#value) | 0–Count−1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | 0–Maximum | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Position | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Startwert (Seed) | [Wert](../../user/de/data-types.md#value) | 0–16 777 215 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Innerhalb einer [Instance Group](instance-group.md) ist Index nullbasiert und Count die aktive Anzahl. Position ist bei mehreren Instanzen `Index/(Count−1)`, sonst 0. Seed ist je Instanz stabil. Ein verbundener Index oder Seed macht sonst gleiche Generatoren instanzabhängig.

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

[Node-Verzeichnis](index.md) · [English](../en/instance-context.md)
