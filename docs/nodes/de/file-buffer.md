# Dateibuffer (File Buffer)

## Übersicht

Lädt einen gewählten Audiodateiabschnitt als Buffer-Ressource.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Datei (File) | [Editor](../../user/de/reference-tables.md) | WAV / MP3 | — | — | — | — | Immer |
| An Nulldurchgängen einrasten (Snap to zero) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Wellenform (Waveform) | [Editor](../../user/de/reference-tables.md) | Abschnittsanzeige | — | — | — | — | Immer |
| Anfang (Start) | [Editor](../../user/de/reference-tables.md) | 0–Ende − 1 smp | — | 0 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |
| Ende (End) | [Editor](../../user/de/reference-tables.md) | Anfang + 1 smp–Dateidauer | — | Dateidauer | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |
| Speicher (Memory) | [Editor](../../user/de/reference-tables.md) | Speicheranzeige | — | — | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Buffer | [Buffer-Ressource](../../user/de/data-types.md#buffer) | Geladene Samples | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Dauer (s) (Duration (s)) | [Wert](../../user/de/data-types.md#value) | > 0 s | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Samples | [Wert](../../user/de/data-types.md#value) | ≥ 1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Nur der gewählte Abschnitt bleibt als abspielbares Audio erhalten. Anfang/Ende sind begrenzte Schieberegler mit mindestens einem Sample Abstand. Die Wellenformgriffe bearbeiten denselben Abschnitt; optionales Einrasten an Nulldurchgängen erleichtert die Grenzwahl. Dauer und Samples beschreiben den geladenen Abschnitt; Samples zählt Frames je Kanal. Der Buffer ist unveränderlich, sein Ende dient als Schreibkopf. [Speed](buffer-reader-speed.md)- und [Position](buffer-reader-position.md)-Reader können ihn abspielen. Abschnittsänderungen ersetzen den Buffer und setzen Reader zurück.

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

[Node-Verzeichnis](index.md) · [English](../en/file-buffer.md)
