# MIDI-Transposition (MIDI Transpose)

## Übersicht

MIDI-Transposition verarbeitet MIDI-Ereignisse.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | — | MIDI-Ereignisse | — | — | [MIDI](../../user/de/rates.md) | Immer |
| Transposition (Transpose) | [Editor](../../user/de/reference-tables.md) | −48–48 st | — | 0 st | [cent](../../user/de/units.md), [st](../../user/de/units.md), [oct](../../user/de/units.md), [Faktor](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | MIDI-Ereignisse | [MIDI](../../user/de/rates.md) | — |

## Verhalten

Versetzt neue Noten einschließlich gebrochener Halbtöne. Bereits gehaltene Noten behalten ihren ursprünglichen Versatz; die Zuordnung der Note-off-Ereignisse bleibt erhalten.

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

[Node-Verzeichnis](index.md) · [English](../en/midi-transpose.md)
