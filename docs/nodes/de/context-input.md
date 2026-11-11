# Kontexteingang (Context Input)
Stellt Kanal-, Zeit- und Blockkontext bereit.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| — | — | — |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Kanal (Channel) | Wert; ±1; Control |
| Sample-Rate (Sample Rate) | Wert; 48 000 Hz; Control; Mono |
| Blockgröße (Block Size) | Wert; 1–256 smp; Control; Mono |
| Zeit (Time) | Wert; 0–∞ s; Audio; Mono |

## Verhalten
Channel ist links −1 und rechts +1. Sample Rate bezeichnet die DSP-Rate des Hosts; Block Size die tatsächlichen Frames pro Kanal, nicht die gesamte Routinglatenz. Time steigt pro berechnetem Sample, solange der Node aktiv ist, und bleibt bei Graphänderungen erhalten. Channel ermöglicht ausdrücklich unterschiedliche Kanalpfade.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/context-input.md)
