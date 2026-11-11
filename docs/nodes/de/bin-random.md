# Binzufall (Bin Random)
Liefert deterministische Zufallszahlen je Bin und Frame.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Startwert (Seed) | 0–2 147 483 647, ganzzahlig; Standard: 0 | Wert; 0–2 147 483 647; Auto |
| Je Bin variieren (Vary by bin) | Aus / An; Standard: An | Editor |
| Je Frame variieren (Vary by frame) | Aus / An; Standard: Aus | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Zufall (Random) | Wert; 0–1, ohne 1; Audio (Bin/Frame) |

## Verhalten
Gleichverteilte Werte hängen von Seed und den aktivierten Bin-/Frameschlüsseln ab. Nur Bin liefert einen festen Zufallswert je Frequenz. Nur Frame liefert einen gemeinsamen Zufallswert pro Fenster. Beide variieren pro Bin und Fenster; keiner liefert einen konstanten Zufallswert. Gleiche Schlüssel ergeben auch kanalübergreifend gleiche Werte. Frame meint ein FFT-Fenster, keinen Audioblock. Nur in [Spectral Groups](spectral-group.md) gültig.

Ein verbundener Seed wird je ausgewertetem Bin abgetastet, abgerundet und auf 0–2 147 483 647 begrenzt. Nicht endliche Werte verwenden den manuellen Seed. Ohne Verbindung bleibt der ganzzahlige Editorwert exakt erhalten. Ein Kontextindex oder Gruppeninput kann den Seed je Instanz verändern.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-random.md)
