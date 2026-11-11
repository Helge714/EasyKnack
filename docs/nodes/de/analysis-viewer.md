# Analyseanzeige (Analysis Viewer)

## Übersicht

Zeigt eine Spektrum- oder Autokorrelationsressource.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Analyse (Analysis) | [Editor](../../user/de/reference-tables.md) | Graph | — | — | — | — | Immer |
| Analyse (Analysis) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | — | Spektrum oder Korrelation | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Daten (Data) | [Editor](../../user/de/reference-tables.md) | Auto / Magnitude / Phase / Power / Correlation | — | Auto | — | — | Immer |
| Vertikaler Bereich (Vertical range) | [Editor](../../user/de/reference-tables.md) | ≥ 0.001 | — | 1; Phase: π | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Auto folgt dem Ressourcentyp. Magnitude zeigt einseitige Amplitude, Power deren Quadrat, Phase Radiant relativ zum FFT-Fensteranfang; Correlation zeigt Lagwerte. Nicht unterstützte Ansichten fallen auf den Ressourcenstandard zurück. Vertikaler Bereich verändert nur die Anzeigeskala. Das Spektrum-Kontextmenü wechselt zwischen linearer/logarithmischer Frequenzachse; Hover zeigt Bin, Frequenz und Wert. Der Viewer berechnet keine weitere FFT oder Autokorrelation.

Das Graph-Kontextmenü bietet Anhalten/Fortsetzen und die vergrößerte Ansicht; auch Doppelklick vergrößert. Dort gibt es Mausradzoom, vertikalen Zoom mit Umschalt+Mausrad, Verschieben mit mittlerer Maustaste und zwei ziehbare Messcursor. Kontextmenüs löschen Cursor einzeln oder setzen die Ansicht zurück. Der Export bietet ein Bild des gesamten Graphen oder des sichtbaren Ausschnitts sowie eine CSV-Tabelle. Anhalten hält die Anzeige, nicht die Audioverarbeitung.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/analysis-viewer.md)
