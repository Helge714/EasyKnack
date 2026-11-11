# Triggerzeitmesser (Trigger Timer)

## Übersicht

Misst vergangene Zeit und Triggerabstände.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Glättung (Smoothing) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Einheit (Unit) | [Editor](../../user/de/reference-tables.md) | s, ms, smp | — | s | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vergangen (Elapsed) | [Wert](../../user/de/data-types.md#value) | 0–∞, gewählte Einheit | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Intervall (Interval) | [Wert](../../user/de/data-types.md#value) | 0–∞, gewählte Einheit | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Elapsed beginnt beim ersten Trigger mit 0 und wird bei jedem weiteren zurückgesetzt. Interval bleibt bis zum zweiten Trigger 0 und hält anschließend den Abstand. Glättung s erhält das bisherige Intervall: `I = s × Ialt + (1−s) × Ineu`; das erste gemessene Intervall initialisiert direkt. Beide Ausgänge teilen die gewählte Einheit.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/trigger-timer.md)
