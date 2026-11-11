# Trigger zu Impuls (Trigger to Pulse)
Wandelt Triggerereignisse in numerische Sampleimpulse um.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | — | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Impuls (Pulse) | Wert; 0–∞; Audio |

## Verhalten
Der Ausgang ist außerhalb der Ereignissamples null. Jedes Ereignis addiert dort 1; gleichzeitige Ereignisse können daher Werte größer als 1 ergeben. Die numerischen Impulse lassen sich mit Math verarbeiten, bleiben aber keine Triggernachrichten.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-to-pulse.md)
