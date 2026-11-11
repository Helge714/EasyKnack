# Panorama (Pan)
Verteilt ein Audiosignal auf links und rechts.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audioeingang (Audio In) | — | Audio; ±∞; Audio |
| Panorama (Pan) | ±1; Standard: 0 | Wert; ±1; Audio |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Stereoausgang (Stereo Out) | Audio; ±∞; Stereo |

## Verhalten
Für `a = (pan+1)π/4` beträgt die linke Verstärkung `√2 cos(a)`, die rechte `√2 sin(a)`. In der Mitte bleiben beide Verstärkungen 1; ein Extrem verstärkt die gewählte Seite mit √2 und schaltet die andere stumm. Mono wird positioniert; Stereokanäle werden einzeln skaliert. Vor Voice Merge wirkt dies pro Stimme, danach auf das gemeinsame Signal.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Audioeingang (Audio In) → Stereoausgang (Stereo Out) |
| Einfügen | Audioeingang (Audio In) → Stereoausgang (Stereo Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/pan.md)
