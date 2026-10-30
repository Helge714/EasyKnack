# Taktgeber (Clock)

## Übersicht

Erzeugt regelmäßig aufeinanderfolgende Trigger.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 0.001–20 000 Hz | 0–20 000 Hz | 1 Hz | [Hz](../../user/de/units.md#frequency), [BPM](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Synchronisieren (Sync) | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Das Intervall beträgt `1/f`; 60 BPM entsprechen 1 Hz. Sync startet den Takt neu und erzeugt am Synchronisationssample einen Trigger. Verbundene Frequenzmodulation folgt der eingestellten Rate; Frequenz 0 stoppt periodische Ereignisse.

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

[Node-Verzeichnis](index.md) · [English](../en/clock.md)
