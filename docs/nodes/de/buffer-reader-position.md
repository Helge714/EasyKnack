# Buffer-Reader (Position) (Buffer Reader (Position))

## Übersicht

Liest einen Buffer an einer extern vorgegebenen Position.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wrap | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |
| Buffer | [Buffer-Ressource](../../user/de/data-types.md#buffer) | — | Buffer-Ressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Position | [Wert](../../user/de/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Versatz (Offset) | [Wert](../../user/de/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Abstand (Distance) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Leseposition (Read position) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Position ist ein absoluter Anteil der Bufferlänge; Offset wird abgezogen. Verbinde die Leseposition eines anderen Readers und setze Offset = 0.5 für einen synchronen zweiten Lesekopf. Dieser Reader hat keinen eigenen Wiedergabetakt.

Wrap repeats at the buffer ends; with Wrap off, the last reachable sample is held. Unwritten samples are silent. Reading interpolates between samples without an automatic crossfade. Distance is twice the shortest circular distance to the write head divided by buffer length: 0 at the head, 1 opposite it. File buffers use the end of the loaded region as their write head.~Wrap wiederholt an den Buffergrenzen; ohne Wrap bleibt das letzte erreichbare Sample stehen. Ungeschriebene Samples sind still. Zwischen Samples wird interpoliert, ohne automatischen Crossfade. Abstand ist der doppelte kürzeste Kreisabstand zum Schreibkopf geteilt durch die Bufferlänge: 0 am Kopf, 1 gegenüber. Dateibuffer verwenden das Ende des geladenen Abschnitts als Schreibkopf.

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

[Node-Verzeichnis](index.md) · [English](../en/buffer-reader-position.md)
