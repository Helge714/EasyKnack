# Aufnahmebuffer (Recording Buffer)

## Übersicht

Zeichnet Audio in einem Ringbuffer auf.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Größe (Size) | [Editor](../../user/de/reference-tables.md) | 256 smp–30 s | — | 2 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Buffer | [Buffer-Ressource](../../user/de/data-types.md#buffer) | Aufgezeichnete Samples | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Größe legt die Speicherdauer bei DSP-Samplerate fest. Größenänderungen erzeugen einen leeren Buffer und setzen Reader zurück. Reader sehen einen samplegenauen Schreibkopf einschließlich früherer Samples desselben Blocks, niemals zukünftiger Samples. Lies die Ressource mit [Buffer Reader (Speed)](buffer-reader-speed.md) oder [Buffer Reader (Position)](buffer-reader-position.md).

[Buffer-Kontext](buffer-context.md) liefert Größe und samplegenaue Schreibposition.

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

[Node-Verzeichnis](index.md) · [English](../en/recording-buffer.md)
