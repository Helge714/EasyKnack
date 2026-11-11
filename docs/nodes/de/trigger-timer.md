# Triggerzeitmesser (Trigger Timer)
Misst vergangene Zeit und Triggerabstände.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Glättung (Smoothing) | 0–1; Standard: 0 | Wert; 0–1; Auto |
| Einheit (Unit) | s, ms, smp; Standard: s | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Vergangen (Elapsed) | Wert; 0–∞, gewählte Einheit; Audio |
| Intervall (Interval) | Wert; 0–∞, gewählte Einheit; Audio |

## Verhalten
Elapsed beginnt beim ersten Trigger mit 0 und wird bei jedem weiteren zurückgesetzt. Interval bleibt bis zum zweiten Trigger 0 und hält anschließend den Abstand. Glättung s erhält das bisherige Intervall: `I = s × Ialt + (1−s) × Ineu`; das erste gemessene Intervall initialisiert direkt. Beide Ausgänge teilen die gewählte Einheit.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-timer.md)
