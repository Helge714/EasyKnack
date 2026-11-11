# Taktgeber (Clock)
Erzeugt regelmäßig aufeinanderfolgende Trigger.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Frequenz (Frequency) | 0.001–20 000 Hz; Standard: 1 Hz; Hz, BPM | Wert; 0–20 000 Hz; Auto |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Synchronisieren (Sync) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Trigger |

## Verhalten
Das Intervall beträgt `1/f`; 60 BPM entsprechen 1 Hz. Sync startet den Takt neu und erzeugt am Synchronisationssample einen Trigger. Verbundene Frequenzmodulation folgt der eingestellten Rate; Frequenz 0 stoppt periodische Ereignisse.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/clock.md)
