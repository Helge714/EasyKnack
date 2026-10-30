# Messwertrekorder (Measurement Recorder)

## Übersicht

Fügt getriggerte Zahlenschnappschüsse einer Dokumenttabelle hinzu.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Messungen (Measurements) | [Editor](../../user/de/reference-tables.md) | Tabelle / CSV / Leeren | — | Leer | — | — | Immer |
| Aufzeichnen (Record) | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Eingänge (Inputs) | [Editor](../../user/de/reference-tables.md) | 1–8 | — | 1 | — | — | Immer |
| Werte: Namen, Einheiten, Reihenfolge (Values: names, units, order) | [Editor](../../user/de/reference-tables.md) | Einstellungen je Eingang | — | Wert 1…8; keine Einheit | — | — | Inspektor |
| Wert 1…8 (Value 1…8) | [Wert](../../user/de/data-types.md#value) | — | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Entsprechend Eingänge |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Jeder Aufzeichnen-Trigger ergänzt eine Zeile mit allen Eingängen an dessen Sampleposition. Für die Aufnahme muss der Audiohost laufen. Die Tabelle mit bis zu 4096 Zeilen wird im .knack-Dokument gespeichert. Öffne sie, um die Testbezeichnung nach dem Zeilenindex zu bearbeiten. Inspektorzeilen steuern Namen, Einheitenbeschriftungen und gemeinsame Port-/Spaltenreihenfolge; Beschriftungen rechnen Werte nicht um. CSV bietet Standard- und deutsches Excel-Format. Leeren entfernt gespeicherte Zeilen.

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

[Node-Verzeichnis](index.md) · [English](../en/measurement-recorder.md)
