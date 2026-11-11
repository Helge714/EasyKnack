# Dateibuffer (File Buffer)
Lädt einen gewählten Audiodateiabschnitt als Buffer-Ressource.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Datei (File) | WAV / MP3 | Editor |
| An Nulldurchgängen einrasten (Snap to zero) | Aus / Ein; Standard: Aus | Editor |
| Wellenform (Waveform) | Abschnittsanzeige | Editor |
| Anfang (Start) | 0–Ende − 1 smp; Standard: 0 s; s, ms, smp | Editor |
| Ende (End) | Anfang + 1 smp–Dateidauer; Standard: Dateidauer; s, ms, smp | Editor |
| Speicher (Memory) | Speicheranzeige | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Buffer | Buffer-Ressource; Geladene Samples; Control |
| Dauer (s) (Duration (s)) | Wert; > 0 s; Control |
| Samples | Wert; ≥ 1; Control |

## Verhalten
Nur der gewählte Abschnitt bleibt als abspielbares Audio erhalten. Anfang/Ende sind begrenzte Schieberegler mit mindestens einem Sample Abstand. Die Wellenformgriffe bearbeiten denselben Abschnitt; optionales Einrasten an Nulldurchgängen erleichtert die Grenzwahl. Dauer und Samples beschreiben den geladenen Abschnitt; Samples zählt Frames je Kanal. Der Buffer ist unveränderlich, sein Ende dient als Schreibkopf. [Speed](buffer-reader-speed.md)- und [Position](buffer-reader-position.md)-Reader können ihn abspielen. Abschnittsänderungen ersetzen den Buffer und setzen Reader zurück.

Beim MP3-Import werden vorhandene Xing/LAME-Angaben zu Encoder-Verzögerung und End-Padding für lückenlose Wiedergabe berücksichtigt. Ohne Metadaten werden leise Samples erhalten statt automatisch entfernt. Alte MP3-Caches werden beim Wiederherstellen ihres Datei-Editors im Hintergrund neu importiert; liegt der Node in einer ungeöffneten Gruppe, diese öffnen oder die Quelldatei erneut auswählen. Dauer und Samplezahl können dadurch kleiner werden.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/file-buffer.md)
