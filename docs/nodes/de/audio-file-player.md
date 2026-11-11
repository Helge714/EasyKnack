# Audiodatei-Player (Audio File Player)
Spielt eine Audiodatei mit Transport- und Schleifensteuerung ab.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Datei (File) | WAV / MP3 | Editor |
| Wiedergabe / Pause (Play / Pause) | Aus / Ein; Standard: 0 | Wert; 0–1; Control |
| Zurücksetzen (Reset) | Schaltfläche | Trigger; Trigger; Trigger |
| Abspielposition (Seek position) | 0–1; Standard: 0; Faktor, s, ms, smp | Wert; 0–1; Control |
| Geschwindigkeit (Speed) | 0–4; Standard: 1 | Wert; 0–4; Control |
| Schleife (Loop) | Aus / Ein; Standard: Aus | Editor |
| Schleifenanfang (Loop start) | 0–1; Standard: 0; Faktor, s, ms, smp | Editor |
| Schleifenende (Loop end) | 0–1; Standard: 1; Faktor, s, ms, smp | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audio-Ausgang (Audio Out) | Audio; ±∞ |
| Position | Wert; 0–1; Control |
| Dauer (s) (Duration (s)) | Wert; ≥ 0 s; Control |
| Bufferunterläufe (Buffer underruns) | Wert; ≥ 0; Control |

## Verhalten
Ein Hintergrundstream liefert decodiertes Audio. Geschwindigkeit verändert Tonhöhe und Dauer; 0 hält die Position. Play ≥ 0.5 aktiviert die Wiedergabe. Reset startet bei aktiver Schleife am Schleifenanfang, sonst am Dateianfang. Abspielpositionen sind Anteile der gesamten Datei; Sekunden und Samples sind alternative Darstellungen. Der unverbundene Positionsregler folgt der Wiedergabe und erlaubt Positionssprünge. Ohne Schleife wird die Wiedergabe am Ende still. Bufferunterläufe zählt Blöcke mit fehlenden decodierten Daten. Für wiederverwendbares Audio im Arbeitsspeicher verwende [File Buffer](file-buffer.md).

Beim MP3-Import werden vorhandene Xing/LAME-Angaben zu Encoder-Verzögerung und End-Padding für lückenlose Wiedergabe berücksichtigt. Ohne Metadaten werden leise Samples erhalten statt automatisch entfernt. Alte MP3-Caches werden beim Wiederherstellen ihres Datei-Editors im Hintergrund neu importiert; liegt der Node in einer ungeöffneten Gruppe, diese öffnen oder die Quelldatei erneut auswählen. Dauer und Samplezahl können dadurch kleiner werden.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/audio-file-player.md)
