# Tonhöhenmonitor (Pitch Monitor)
Zeigt erkannte und angestrebte Tonhöhe und reicht das Ziel weiter.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Tonhöhe (Pitch) | Anzeige | Editor |
| Erkannt [Hz] (Detected [Hz]) | Hz | Wert; ≥ 0 Hz; Auto |
| Ziel [Hz] (Target [Hz]) | Hz | Wert; ≥ 0 Hz; Auto |
| Konfidenz (Confidence) | — | Wert; 0–1; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ziel [Hz] (Target [Hz]) | Wert; ≥ 0 Hz |

## Verhalten
Der Ausgang reicht Ziel unverändert weiter. Die Anzeige vergleicht erkannte Tonhöhe, Ziel und Konfidenz; sie erkennt oder quantisiert keine Tonhöhe. Platziere den Monitor im Zielpfad, damit er zum aktiven Graphen gehört.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Ziel [Hz] (Target [Hz]) → Ziel [Hz] (Target [Hz]) |
| Einfügen | Ziel [Hz] (Target [Hz]) → Ziel [Hz] (Target [Hz]) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/pitch-monitor.md)
