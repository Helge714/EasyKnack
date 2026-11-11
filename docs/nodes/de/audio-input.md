# Audio-Eingang (Audio Input)
Erfasst Audio von einer gewählten Quelle.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Quelle (Source) | Verfügbare Aufnahmequellen; Standard: Systemmix | Editor |
| Modus (Mode) | Kopieren; Standard: Kopieren | Editor; Systemmix oder Anwendung |
| Kanäle (Channels) | Mono / Stereo; Standard: Stereo | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audio-Ausgang (Audio Out) | Audio; ±1 |

## Verhalten
Die Aufnahme kann ein Gerät, den Systemmix oder eine Anwendung verwenden. Kopieren erfasst das Signal, ohne die ursprüngliche Wiedergabe zu unterdrücken. Für eine ausschließliche Weiterleitung wähle ein virtuelles Routinggerät und leite die Quellanwendung in Windows dorthin. Mono fasst die Kanäle zusammen; Stereo erhält links und rechts.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/audio-input.md)
