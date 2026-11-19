# Audioaufnahme (Audio Recorder)

## Übersicht

Nimmt triggergesteuerte Audiodurchläufe als WAV- oder MP3-Dateien auf.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Aufnahmedatei (Recording file) | [Editor](../../user/de/reference-tables.md) | WAV-/MP3-Name + Ordner | — | `Recordings/Recording.wav` | — | — | Immer |
| Aufnahmestatus (Recording status) | [Editor](../../user/de/reference-tables.md) | Bereit / Aufnahme / Gespeichert / Fehler | — | Bereit | s | — | Immer |
| Kanäle (Channels) | [Editor](../../user/de/reference-tables.md) | Mono / Stereo | — | Stereo | — | — | Immer |
| Überschreiben (Overwrite) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Indexstellen (Index digits) | [Editor](../../user/de/reference-tables.md) | 1–12 | — | 4 | — | — | Inspektor; bei Überschreiben deaktiviert |
| Aufnahme speichern unter (Save recording as) | [Editor](../../user/de/reference-tables.md) | Speicherdialog | — | Deaktiviert | — | — | Inspektor; abgeschlossene Aufnahme erforderlich |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | 0 | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Start (Start) | [Trigger](../../user/de/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Stopp (Stop) | [Trigger](../../user/de/data-types.md#trigger) | Button | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Start nimmt das auslösende Sample bereits auf; Stopp beendet die Aufnahme vor diesem Sample. Weitere Start-Trigger während der Aufnahme werden ignoriert. Bei gleichzeitigen Triggern hat Stopp Vorrang. Der Audiohost muss laufen. Host-Stopp und Entfernen des Nodes schließen die Aufnahme ab.

WAV speichert 32-Bit-Fließkommawerte mit der Host-Samplerate. Stereo erhält beide Kanäle, Mono mittelt links und rechts. Polyphone Stimmen werden addiert. Ohne Überschreiben verwendet die erste Aufnahme direkt `Take.wav`. Bei der nächsten Namenskollision wird die bestehende Datei zu `Take_0001.wav` und die neue Aufnahme zu `Take_0002.wav`. Sobald eine nummerierte Reihe existiert, folgt die nächste Aufnahme auf den höchsten vorhandenen Index, auch nach einem Neustart des Recorders. Bereits nummerierte Dateien werden nicht ersetzt. Eine noch laufende unnummerierte Aufnahme wird erst nach dem Schließen ihres Schreibers umbenannt. Indexstellen legt nur die Mindestbreite fest; größere Indizes bleiben möglich. Überschreiben ersetzt bei jeder neuen Aufnahme die gewählte Datei.

Ein Hintergrundthread schreibt aus einer begrenzten Warteschlange. Ist der Datenträger zu langsam, stoppt die Aufnahme mit Fehler; bereits übernommenes Audio wird als Teilaufnahme abgeschlossen. Der Status zeigt die Dauer und im Tooltip den Dateipfad. Datei-, Pufferüberlauf-, Schreib- und WAV-Größenfehler werden unterschieden. Es gilt die WAV/RIFF-Grenze von ungefähr 4 GiB pro WAV-Aufnahme; MP3 verwendet unter Windows den Windows-Medienencoder und unter Linux den mitgelieferten LAME-Encoder (128 kbit/s Mono, 192 kbit/s Stereo). Linux-Aufnahmen enthalten Metadaten für Codec-Verzögerung und Padding; die Wiedergabe hängt vom Player ab. Für MP3 werden Eingangswerte auf ±1 begrenzt; das Format enthält Codec-Verzögerung und Padding. Für samplegenaue Zeitmessungen WAV verwenden. Durchsuchen öffnet einen Speicherdialog für Zielordner und Dateiname. Im benachbarten Textfeld lässt sich der Dateiname ebenfalls direkt bearbeiten. Die Zielauswahl legt noch keine Datei an; die Aufnahme beginnt mit Start. Enter übernimmt den Namen und verlässt das Feld. Die Endung bestimmt das Format. Fehlt `.wav` oder `.mp3`, wird `.wav` angehängt. Bestehende Dokumente behalten ihre gespeicherten Zielpfade. Standard ist WAV.

Aufnahme speichern unter exportiert eine abgeschlossene Aufnahme im aufgenommenen Format. Das automatische Aufnahmeziel bleibt erhalten. Ohne fertiggestellte Audiodaten ist der Button deaktiviert. Änderungen an Aufnahmeziel, Format oder Kanälen schließen den aktuellen Durchlauf ab.

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

[Node-Verzeichnis](index.md) · [English](../en/audio-recorder.md)

Neue Recorder verwenden standardmäßig `Dokumente/EasyKnack/Recordings/Recording.wav` im Dokumenteordner des Systems. Bestehende Zielpfade bleiben erhalten.
