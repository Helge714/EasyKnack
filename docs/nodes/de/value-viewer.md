# Werteanzeige (Value Viewer)

## Übersicht

Zeigt Zahleneingänge als Werte oder Zeitverläufe.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Werte (Values) | [Editor](../../user/de/reference-tables.md) | Graph / Werte | — | Graph | — | — | Immer |
| Eingänge (Inputs) | [Editor](../../user/de/reference-tables.md) | 1–8 | — | 1 | — | — | Immer |
| Zeitfenster (Time window) | [Editor](../../user/de/reference-tables.md) | 0.1–600 s | — | 10 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |
| Jedes Sample aufzeichnen (Record every sample) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Inspektor |
| Kanal (Channel) | [Editor](../../user/de/reference-tables.md) | Links / Rechts / Mix | — | Links | — | — | Inspektor |
| Werte: Namen, Einheiten, Reihenfolge (Values: names, units, order) | [Editor](../../user/de/reference-tables.md) | Einstellungen je Eingang | — | Wert 1…8; keine Einheit | — | — | Inspektor |
| Wert 1…8 (Value 1…8) | [Wert](../../user/de/data-types.md#value) | — | ±∞ | — | — | [Auto](../../user/de/rates.md#auto) | Entsprechend Eingänge |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Eingänge bestimmt Ports und dargestellte Reihen. Die Inspektorzeilen ändern Namen, Einheitenbeschriftungen und Reihenfolge gemeinsam für Ports, Anzeige und Export; Einheitenbeschriftungen rechnen Werte nicht um. Verlauf zeigt getrennte Graphen oder eine Überlagerung; Werte zeigt einzelne Zahlen. Anhalten hält einen Schnappschuss. Vergrößerte Ansichten erlauben Zoom, Verschieben und Messcursor; Bilder und Tabellen können den gesamten Verlauf oder sichtbaren Ausschnitt exportieren.

Ohne Jedes Sample aufzeichnen bleiben je 10-ms-Intervall Minimum, Maximum und letzter Wert erhalten. Aktiviert wird jedes Sample gespeichert. Die Historie ist auf eine Million Eingangsdatensätze begrenzt; die effektive Dauer kann kürzer als Zeitfenster sein. Transportlücken bleiben sichtbare Lücken. Controlwerte halten über ihren Block; Mix beobachtet (L+R)/2.

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

[Node-Verzeichnis](index.md) · [English](../en/value-viewer.md)
