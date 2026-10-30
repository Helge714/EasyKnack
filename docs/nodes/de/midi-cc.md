# MIDI CC

## Übersicht

Wandelt einen MIDI-Controller in einen gehaltenen Zahlenwert um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | — | MIDI-Ereignisse | — | — | [MIDI](../../user/de/rates.md) | Immer |
| Kanal (0 = alle) (Channel (0 = all)) | [Editor](../../user/de/reference-tables.md) | 0–16 | — | 0 | — | — | Immer |
| Controller | [Editor](../../user/de/reference-tables.md) | 0–127 | — | 1 | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Wert = `CC/127`; Änderungen behalten ihre Sampleposition, der letzte Wert bleibt stehen. Controller 1 ist Modulation, 7 Lautstärke und 64 Haltepedal. Kanal 0 akzeptiert alle MIDI-Kanäle.

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

[Node-Verzeichnis](index.md) · [English](../en/midi-cc.md)
