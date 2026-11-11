# Triggerzähler (Trigger Counter)
Zählt Eingangstrigger und gibt deren nullbasierten Index aus.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Zurücksetzen (Reset) | Button | Trigger; Trigger |
| Wrap | Aus / An; Standard: Aus | Editor |
| Anzahl (Count) | 1–16 777 216, ganzzahlig; Standard: 16 | Wert; 1–16 777 216, gerundet; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Index | Wert; 0–16 777 215; Audio |
| Trigger | Trigger |

## Verhalten
Der erste Trigger liefert Index 0; folgende erhöhen ihn. Zwischen Ereignissen bleibt der Index erhalten. Wrap durchläuft 0…Count−1; ohne Wrap sättigt der Zähler bei 16 777 215. Reset bereitet Index 0 vor; ein gleichzeitiger Trigger zählt weiterhin als erstes Ereignis. Der Trigger-Ausgang reicht die gezählten Ereignisse weiter.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Trigger → Trigger |
| Einfügen | Trigger → Trigger |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/trigger-counter.md)
