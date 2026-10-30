# Triggerteiler (Trigger Divider)

## Übersicht

Gibt jeden n-ten Eingangstrigger weiter.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md) | Button | — | — | — | [Trigger](../../user/de/rates.md) | Immer |
| Teilung (Division) | [Wert](../../user/de/data-types.md) | 1–1024, ganzzahlig | 1–1024, gerundet | 2 | — | [Control](../../user/de/rates.md) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md) | Button | — | — | — | [Trigger](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md) | — | [Trigger](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Teilung n gibt beim n-ten Ereignis aus und beginnt erneut zu zählen. Reset löscht den Zähler; ein gleichzeitiger Eingang wird danach gezählt. Teilung 1 gibt jedes Ereignis weiter. Eine geänderte Teilung erzeugt selbst keinen Trigger.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Trigger | Trigger |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Trigger | Trigger |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/trigger-divider.md)
