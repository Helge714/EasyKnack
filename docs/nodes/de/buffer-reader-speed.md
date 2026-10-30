# Buffer-Reader (Geschwindigkeit) (Buffer Reader (Speed))

## Übersicht

Liest einen Buffer mit eigenem Wiedergabetakt.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wrap | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |
| Buffer | [Buffer-Ressource](../../user/de/data-types.md#buffer) | — | Buffer-Ressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Geschwindigkeit (Speed) | [Wert](../../user/de/data-types.md#value) | ±8 | ±8 | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Versatz (Offset) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Abstand (Distance) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Leseposition (Read position) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Geschwindigkeit 1 spielt normal, 0 hält an, negative Werte spielen rückwärts. Offset zieht einen Anteil der Bufferlänge von der Cursorposition ab. Reset setzt bei Aufnahmebuffern auf die aktuelle Schreibposition, bei Dateibuffern auf den Dateianfang, jeweils vor Anwendung des Offsets. Die Leseposition kann einen [Position-Reader](buffer-reader-position.md) steuern.

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

[Node-Verzeichnis](index.md) · [English](../en/buffer-reader-speed.md)
