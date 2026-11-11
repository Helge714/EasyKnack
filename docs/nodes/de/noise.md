# Rauschen (Noise)
Erzeugt Rauschen mit wählbarer spektraler Färbung und Startwert.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Startwert (Seed) | −1–16 777 215; Standard: −1 | Wert; −1–16 777 215, gerundet; Control |
| Rauschen (Noise) | Weiß, Rosa, Braun, Blau; Standard: Weiß | Editor |
| Rate | Audio, Control; Standard: Audio | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Audio; ±1; Audio / Control |

## Verhalten
Weiß verteilt Leistung gleichmäßig pro Hz; Rosa betont tiefe Frequenzen, Braun integriert eine weiße Folge und Blau betont Änderungen. Seed −1 verwendet den automatischen Startwert des logischen Nodes. Kanal- und Instanzkopien teilen ihn; ein Kontextsignal am Seed erzeugt unabhängige Folgen. Eine Seed-Änderung startet die Folge neu. Control liefert einen gehaltenen Zufallswert pro Block.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/noise.md)
