# Audio-Eingang (Audio Input)

## Übersicht

Erfasst Audio von einer gewählten Quelle.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Quelle (Source) | [Editor](../../user/de/reference-tables.md) | Verfügbare Aufnahmequellen | — | Systemmix | — | — | Immer |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Kopieren | — | Kopieren | — | — | Systemmix oder Anwendung |
| Kanäle (Channels) | [Editor](../../user/de/reference-tables.md) | Mono / Stereo | — | Stereo | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio-Ausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die Aufnahme kann ein Gerät, den Systemmix oder eine Anwendung verwenden. Kopieren erfasst das Signal, ohne die ursprüngliche Wiedergabe zu unterdrücken. Für eine ausschließliche Weiterleitung wähle ein virtuelles Routinggerät und leite die Quellanwendung in Windows dorthin. Mono fasst die Kanäle zusammen; Stereo erhält links und rechts.

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

[Node-Verzeichnis](index.md) · [English](../en/audio-input.md)
