# Audiodatei-Player (Audio File Player)

## Übersicht

Spielt eine Audiodatei mit Transport- und Schleifensteuerung ab.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Datei (File) | [Editor](../../user/de/reference-tables.md) | WAV / MP3 | — | — | — | — | Immer |
| Wiedergabe / Pause (Play / Pause) | [Wert](../../user/de/data-types.md#value) | Aus / Ein | 0–1 | 0 | — | [Control](../../user/de/rates.md#control) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Abspielposition (Seek position) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0 | [Faktor](../../user/de/units.md), [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Immer |
| Geschwindigkeit (Speed) | [Wert](../../user/de/data-types.md#value) | 0–4 | 0–4 | 1 | — | [Control](../../user/de/rates.md#control) | Immer |
| Schleife (Loop) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Schleifenanfang (Loop start) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0 | [Faktor](../../user/de/units.md), [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |
| Schleifenende (Loop end) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 1 | [Faktor](../../user/de/units.md), [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio-Ausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Position | [Wert](../../user/de/data-types.md#value) | 0–1 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Dauer (s) (Duration (s)) | [Wert](../../user/de/data-types.md#value) | ≥ 0 s | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Bufferunterläufe (Buffer underruns) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Ein Hintergrundstream liefert decodiertes Audio. Geschwindigkeit verändert Tonhöhe und Dauer; 0 hält die Position. Play ≥ 0.5 aktiviert die Wiedergabe. Reset startet bei aktiver Schleife am Schleifenanfang, sonst am Dateianfang. Abspielpositionen sind Anteile der gesamten Datei; Sekunden und Samples sind alternative Darstellungen. Der unverbundene Positionsregler folgt der Wiedergabe und erlaubt Positionssprünge. Ohne Schleife wird die Wiedergabe am Ende still. Bufferunterläufe zählt Blöcke mit fehlenden decodierten Daten. Für wiederverwendbares Audio im Arbeitsspeicher verwende [File Buffer](file-buffer.md).

Beim MP3-Import werden vorhandene Xing/LAME-Angaben zu Encoder-Verzögerung und End-Padding für lückenlose Wiedergabe berücksichtigt. Ohne Metadaten werden leise Samples erhalten statt automatisch entfernt. Alte MP3-Caches werden beim Wiederherstellen ihres Datei-Editors im Hintergrund neu importiert; liegt der Node in einer ungeöffneten Gruppe, diese öffnen oder die Quelldatei erneut auswählen. Dauer und Samplezahl können dadurch kleiner werden.

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

[Node-Verzeichnis](index.md) · [English](../en/audio-file-player.md)
