# Aufnahmebuffer (Recording Buffer)
Zeichnet Audio in einem Ringbuffer auf.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audio-Eingang (Audio In) | — | Audio; ±∞; Audio |
| Größe (Size) | 256 smp–30 s; Standard: 2 s; s, ms, smp | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Buffer | Buffer-Ressource; Aufgezeichnete Samples; Control |

## Verhalten

[Anleitung](../../user/de/buffers.md)
Größe legt die Speicherdauer bei DSP-Samplerate fest. Größenänderungen erzeugen einen leeren Buffer und setzen Reader zurück. Reader sehen einen samplegenauen Schreibkopf einschließlich früherer Samples desselben Blocks, niemals zukünftiger Samples. Lies die Ressource mit [Buffer Reader (Speed)](buffer-reader-speed.md) oder [Buffer Reader (Position)](buffer-reader-position.md).

[Buffer-Kontext](buffer-context.md) liefert Größe und samplegenaue Schreibposition.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/recording-buffer.md)
