# Wavetable-Anzeige (Wavetable Viewer)

## Übersicht

Zeigt die tatsächlichen Samples einer Wavetable-Ressource.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Wavetable-Ressource](../../user/de/data-types.md#wavetable) | — | Wavetable-Ressource | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Samples | [Editor](../../user/de/reference-tables.md) | Samplegraph | — | — | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Der Graph zeigt gespeicherte Ressourcensamples über eine Periode, keine editierbare Bézierkurve. Verbinde [Waveform](waveform.md) oder [Spectrum to Waveform](spectrum-to-waveform.md), um die tatsächlich an Oszillatoren gelieferte Tabelle zu prüfen. Vertikales Vergrößern des Nodes vergrößert den Samplegraphen. Hover zeigt Sampleindex und Wert. Das Kontextmenü kopiert die Ressource; Einfügen erzeugt einen Waveform-Node im Ressourcenmodus.

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

[Node-Verzeichnis](index.md) · [English](../en/wavetable-viewer.md)
