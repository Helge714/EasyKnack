# MIDI-Verzögerung (MIDI Delay)

## Übersicht

MIDI-Verzögerung verarbeitet MIDI-Ereignisse.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | — | MIDI-Ereignisse | — | — | [MIDI](../../user/de/rates.md) | Immer |
| Verzögerung (Delay) | [Editor](../../user/de/reference-tables.md) | 0–10000 ms | — | 100 ms | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | MIDI-Ereignisse | [MIDI](../../user/de/rates.md) | — |

## Verhalten

Verzögert Noten und Controller-Nachrichten gemeinsam. Eine Änderung gilt für neu eintreffende Ereignisse; wartende Ereignisse behalten ihren Termin. Alle Noten aus leert die Warteschlange.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | MIDI | MIDI |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| MIDI | MIDI |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/midi-delay.md)
