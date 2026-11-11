# Audioaufnahme (Audio Recorder)
Nimmt triggergesteuerte Audiodurchläufe als WAV- oder MP3-Dateien auf.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Aufnahmedatei (Recording file) | WAV- / MP3-Ziel; Standard: Keines | Editor |
| Aufnahmestatus (Recording status) | Bereit / Aufnahme / Gespeichert / Fehler; Standard: Bereit; s | Editor |
| Kanäle (Channels) | Mono / Stereo; Standard: Stereo | Editor |
| Überschreiben (Overwrite) | Aus / Ein; Standard: Aus | Editor |
| Indexstellen (Index digits) | 1–12; Standard: 4 | Editor; Inspektor; bei Überschreiben deaktiviert |
| Aufnahme speichern unter (Save recording as) | Speicherdialog; Standard: Deaktiviert | Editor; Inspektor; abgeschlossene Aufnahme erforderlich |
| Audioeingang (Audio In) | Standard: 0 | Audio; ±∞; Audio |
| Start (Start) | Button | Trigger; Trigger; Trigger |
| Stopp (Stop) | Button | Trigger; Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
Start nimmt das auslösende Sample bereits auf; Stopp beendet die Aufnahme vor diesem Sample. Weitere Start-Trigger während der Aufnahme werden ignoriert. Bei gleichzeitigen Triggern hat Stopp Vorrang. Der Audiohost muss laufen. Host-Stopp und Entfernen des Nodes schließen die Aufnahme ab.

WAV speichert 32-Bit-Fließkommawerte mit der Host-Samplerate. Stereo erhält beide Kanäle, Mono mittelt links und rechts. Polyphone Stimmen werden addiert. Ohne Überschreiben erzeugt `Take.wav` die Datei `Take_0001.wav`, danach den nächsten freien Index. Indexstellen legt nur die Mindestbreite fest; größere Indizes bleiben möglich. Überschreiben ersetzt bei jeder neuen Aufnahme die gewählte Datei.

Ein Hintergrundthread schreibt aus einer begrenzten Warteschlange. Ist der Datenträger zu langsam, stoppt die Aufnahme mit Fehler; bereits übernommenes Audio wird als Teilaufnahme abgeschlossen. Der Status zeigt die Dauer und im Tooltip den Dateipfad. Datei-, Pufferüberlauf-, Schreib- und WAV-Größenfehler werden unterschieden. Es gilt die WAV/RIFF-Grenze von ungefähr 4 GiB pro WAV-Aufnahme; MP3 benötigt einen verfügbaren Windows-Medienencoder. Für MP3 werden Eingangswerte auf ±1 begrenzt; das Format enthält Codec-Verzögerung und Padding. Für samplegenaue Zeitmessungen WAV verwenden. Das Format wird im Dateidialog oder über die Dateiendung gewählt; Standard ist WAV.

Aufnahme speichern unter exportiert eine abgeschlossene Aufnahme im aufgenommenen Format. Das automatische Aufnahmeziel bleibt erhalten. Ohne fertiggestellte Audiodaten ist der Button deaktiviert. Änderungen an Aufnahmeziel, Format oder Kanälen schließen den aktuellen Durchlauf ab.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/audio-recorder.md)
