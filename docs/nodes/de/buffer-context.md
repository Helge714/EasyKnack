# Buffer-Kontext (Buffer Context)
Liest Größe und Schreibposition einer Buffer-Ressource aus.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Buffer | — | Buffer-Ressource; Buffer-Ressource; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Größe (Size) | Wert; 0–+∞ smp; Control |
| Schreibposition (Write position) | Wert; 0–1; Audio |

## Verhalten

[Anleitung](../../user/de/buffers.md)
Größe ist die allokierte Sampleanzahl pro Kanal, einschließlich noch unbeschriebener Aufnahmekapazität. Bei Dateibuffern entspricht sie dem geladenen Start-/Endausschnitt. Standardeinheit ist smp; Sekunden und Millisekunden stehen im Einheitenmenü des Ausgangs bereit.

Schreibposition ist die normierte nächste Schreibstelle. Aufnahmebuffer laufen sampleweise vorwärts und springen am Ende auf 0; die Referenz entspricht den Buffer-Readern. Dateibuffer liefern 1 (Dateiende). Leere oder unverbundene Ressourcen liefern an beiden Ausgängen 0.

Beim Laden werden bisherige Schreibpositionsverbindungen von Aufnahmebuffern automatisch auf Buffer-Kontext umgestellt, auch bei veröffentlichten Gruppenausgängen. Ein Hinweis informiert darüber; Speichern erhält die neue Verdrahtung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/buffer-context.md)
