# Tonhöhenmonitor (Pitch Monitor)

## Übersicht

Zeigt erkannte und angestrebte Tonhöhe und reicht das Ziel weiter.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Tonhöhe (Pitch) | [Editor](../../user/de/reference-tables.md) | Anzeige | — | — | — | — | Immer |
| Erkannt [Hz] (Detected [Hz]) | [Wert](../../user/de/data-types.md#value) | — | ≥ 0 Hz | — | [Hz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Ziel [Hz] (Target [Hz]) | [Wert](../../user/de/data-types.md#value) | — | ≥ 0 Hz | — | [Hz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Konfidenz (Confidence) | [Wert](../../user/de/data-types.md#value) | — | 0–1 | — | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ziel [Hz] (Target [Hz]) | [Wert](../../user/de/data-types.md#value) | ≥ 0 Hz | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Der Ausgang reicht Ziel unverändert weiter. Die Anzeige vergleicht erkannte Tonhöhe, Ziel und Konfidenz; sie erkennt oder quantisiert keine Tonhöhe. Platziere den Monitor im Zielpfad, damit er zum aktiven Graphen gehört.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Ziel [Hz] (Target [Hz]) | Ziel [Hz] (Target [Hz]) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Ziel [Hz] (Target [Hz]) | Ziel [Hz] (Target [Hz]) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/pitch-monitor.md)
