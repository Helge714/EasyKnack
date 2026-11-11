# Rauschen (Noise)

## Übersicht

Erzeugt Rauschen mit wählbarer spektraler Färbung und Startwert.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Startwert (Seed) | [Wert](../../user/de/data-types.md#value) | −1–16 777 215 | −1–16 777 215, gerundet | −1 | — | [Control](../../user/de/rates.md#control) | Immer |
| Rauschen (Noise) | [Editor](../../user/de/reference-tables.md) | Weiß, Rosa, Braun, Blau | — | Weiß | — | — | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Audio, Control | — | Audio | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio / Control](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Weiß verteilt Leistung gleichmäßig pro Hz; Rosa betont tiefe Frequenzen, Braun integriert eine weiße Folge und Blau betont Änderungen. Seed −1 verwendet den automatischen Startwert des logischen Nodes. Kanal- und Instanzkopien teilen ihn; ein Kontextsignal am Seed erzeugt unabhängige Folgen. Eine Seed-Änderung startet die Folge neu. Control liefert einen gehaltenen Zufallswert pro Block.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/noise.md)
