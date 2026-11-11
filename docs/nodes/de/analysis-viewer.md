# Analyseanzeige (Analysis Viewer)
Zeigt eine Spektrum- oder Autokorrelationsressource.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Analyse (Analysis) | Graph | Editor |
| Analyse (Analysis) | — | Analysis-Ressource; Spektrum oder Korrelation; Control |
| Daten (Data) | Auto / Magnitude / Phase / Power / Correlation; Standard: Auto | Editor |
| Vertikaler Bereich (Vertical range) | ≥ 0.001; Standard: 1; Phase: π | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
Auto folgt dem Ressourcentyp. Magnitude zeigt einseitige Amplitude, Power deren Quadrat, Phase Radiant relativ zum FFT-Fensteranfang; Correlation zeigt Lagwerte. Nicht unterstützte Ansichten fallen auf den Ressourcenstandard zurück. Vertikaler Bereich verändert nur die Anzeigeskala. Das Spektrum-Kontextmenü wechselt zwischen linearer/logarithmischer Frequenzachse; Hover zeigt Bin, Frequenz und Wert. Der Viewer berechnet keine weitere FFT oder Autokorrelation.

Das Graph-Kontextmenü bietet Anhalten/Fortsetzen und die vergrößerte Ansicht; auch Doppelklick vergrößert. Dort gibt es Mausradzoom, vertikalen Zoom mit Umschalt+Mausrad, Verschieben mit mittlerer Maustaste und zwei ziehbare Messcursor. Kontextmenüs löschen Cursor einzeln oder setzen die Ansicht zurück. Der Export bietet ein Bild des gesamten Graphen oder des sichtbaren Ausschnitts sowie eine CSV-Tabelle. Anhalten hält die Anzeige, nicht die Audioverarbeitung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/analysis-viewer.md)
