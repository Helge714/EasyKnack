# Kontexteingang (Context Input)

## Übersicht

Stellt Kanal-, Zeit- und Blockkontext bereit.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — | — | — |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Kanal (Channel) | [Wert](../../user/de/data-types.md#value) | ±1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Sample-Rate (Sample Rate) | [Wert](../../user/de/data-types.md#value) | 48 000 Hz | [Control](../../user/de/rates.md#control) | Mono |
| Blockgröße (Block Size) | [Wert](../../user/de/data-types.md#value) | 1–256 smp | [Control](../../user/de/rates.md#control) | Mono |
| Zeit (Time) | [Wert](../../user/de/data-types.md#value) | 0–∞ s | [Audio](../../user/de/rates.md#audio) | Mono |

## Verhalten

Channel ist links −1 und rechts +1. Sample Rate bezeichnet die DSP-Rate des Hosts; Block Size die tatsächlichen Frames pro Kanal, nicht die gesamte Routinglatenz. Time steigt pro berechnetem Sample, solange der Node aktiv ist, und bleibt bei Graphänderungen erhalten. Channel ermöglicht ausdrücklich unterschiedliche Kanalpfade.

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

[Node-Verzeichnis](index.md) · [English](../en/context-input.md)
