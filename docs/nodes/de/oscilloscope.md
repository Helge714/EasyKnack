# Oszilloskop (Oscilloscope)
Zeigt eine getriggerte Audio-Wellenform.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Oszilloskop (Oscilloscope) | Graph | Editor |
| Audio-Eingang (Audio In) | — | Audio; ±∞; Audio |
| Kanal (Channel) | Links / Rechts / Mix; Standard: Links | Editor |
| Vertikaler Bereich (Vertical range) | ≥ 0.001; Standard: 1 | Editor |
| Untere Triggerschwelle (Trigger low) | ±1; Standard: −0.1 | Editor |
| Obere Triggerschwelle (Trigger high) | ±1; Standard: 0.1 | Editor |
| Fenster (Window) | 6–200 ms; Standard: 20 ms; s, ms, smp | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
Der Trigger wird unterhalb der unteren Schwelle scharf und erfasst beim folgenden Überschreiten der oberen Schwelle. Ohne Übergang erfolgt die Erfassung automatisch. Das Graph-Kontextmenü blendet die Schwellenlinien ein/aus. Polyphone Stimmen werden vor der Messung summiert; Mix = (L+R)/2. Vertikaler Bereich r zeigt −r–r und verändert keine Signalwerte.

Das Graph-Kontextmenü bietet Anhalten/Fortsetzen und die vergrößerte Ansicht; auch Doppelklick vergrößert. Dort gibt es Mausradzoom, vertikalen Zoom mit Umschalt+Mausrad, Verschieben mit mittlerer Maustaste und zwei ziehbare Messcursor. Kontextmenüs löschen Cursor einzeln oder setzen die Ansicht zurück. Der Export bietet ein Bild des gesamten Graphen oder des sichtbaren Ausschnitts sowie eine CSV-Tabelle. Anhalten hält die Anzeige, nicht die Audioverarbeitung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/oscilloscope.md)
