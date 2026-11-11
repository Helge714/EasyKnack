# Buffer-Reader (Position) (Buffer Reader (Position))
Liest einen Buffer an einer extern vorgegebenen Position.
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
| Position | 0–1; Standard: 0 | Wert; ±∞; Auto |
| Versatz (Offset) | 0–1; Standard: 0 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Audio; ±∞ |
| Abstand (Distance) | Wert; 0–1; Audio |
| Leseposition (Read position) | Wert; 0–1; Audio |

## Verhalten
Position ist ein absoluter Anteil der Bufferlänge; Offset wird abgezogen. Verbinde die Leseposition eines anderen Readers und setze Offset = 0.5 für einen synchronen zweiten Lesekopf. Dieser Reader hat keinen eigenen Wiedergabetakt.

Wrap repeats at the buffer ends; with Wrap off, the last reachable sample is held. Unwritten samples are silent. Reading interpolates between samples without an automatic crossfade. Distance is twice the shortest circular distance to the write head divided by buffer length: 0 at the head, 1 opposite it. File buffers use the end of the loaded region as their write head.~Wrap wiederholt an den Buffergrenzen; ohne Wrap bleibt das letzte erreichbare Sample stehen. Ungeschriebene Samples sind still. Zwischen Samples wird interpoliert, ohne automatischen Crossfade. Abstand ist der doppelte kürzeste Kreisabstand zum Schreibkopf geteilt durch die Bufferlänge: 0 am Kopf, 1 gegenüber. Dateibuffer verwenden das Ende des geladenen Abschnitts als Schreibkopf.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/buffer-reader-position.md)
