# Buffer-Reader (Geschwindigkeit) (Buffer Reader (Speed))
Liest einen Buffer mit eigenem Wiedergabetakt.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wrap | Aus / Ein; Standard: Ein | Editor |
| Buffer | — | Buffer-Ressource; Buffer-Ressource; Control |
| Geschwindigkeit (Speed) | ±8; Standard: 1 | Wert; ±8; Auto |
| Versatz (Offset) | 0–1; Standard: 0 | Wert; 0–1; Auto |
| Zurücksetzen (Reset) | Schaltfläche | Trigger; Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Audio; ±∞ |
| Abstand (Distance) | Wert; 0–1; Audio |
| Leseposition (Read position) | Wert; 0–1; Audio |

## Verhalten
Geschwindigkeit 1 spielt normal, 0 hält an, negative Werte spielen rückwärts. Offset zieht einen Anteil der Bufferlänge von der Cursorposition ab. Reset setzt bei Aufnahmebuffern auf die aktuelle Schreibposition, bei Dateibuffern auf den Dateianfang, jeweils vor Anwendung des Offsets. Die Leseposition kann einen [Position-Reader](buffer-reader-position.md) steuern.

Wrap repeats at the buffer ends; with Wrap off, the last reachable sample is held. Unwritten samples are silent. Reading interpolates between samples without an automatic crossfade. Distance is twice the shortest circular distance to the write head divided by buffer length: 0 at the head, 1 opposite it. File buffers use the end of the loaded region as their write head.~Wrap wiederholt an den Buffergrenzen; ohne Wrap bleibt das letzte erreichbare Sample stehen. Ungeschriebene Samples sind still. Zwischen Samples wird interpoliert, ohne automatischen Crossfade. Abstand ist der doppelte kürzeste Kreisabstand zum Schreibkopf geteilt durch die Bufferlänge: 0 am Kopf, 1 gegenüber. Dateibuffer verwenden das Ende des geladenen Abschnitts als Schreibkopf.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/buffer-reader-speed.md)
