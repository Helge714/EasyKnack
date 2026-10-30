# Schwellwerttrigger (Threshold Trigger)

## Übersicht

Erzeugt Trigger beim Überschreiten einer Schwelle.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Flanke (Edge) | [Editor](../../user/de/reference-tables.md) | Steigend, Fallend, Beide | — | Steigend | — | — | Immer |
| Rate (Rate) | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Eingang (In) | [Wert](../../user/de/data-types.md) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md) | Immer |
| Schwelle (Threshold) | [Wert](../../user/de/data-types.md) | ±1 000 000 | ±∞ | 0.5 | — | [Auto](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md) | — | [Trigger](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Vergleicht `Eingang > Schwelle`. Rising löst bei falsch→wahr aus, Falling bei wahr→falsch und Both bei beiden Änderungen. Der erste Wert initialisiert ohne Ereignis. Audio prüft jedes Sample, Control die Blockwerte. Auch eine geänderte Schwelle kann eine Kreuzung auslösen.

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

[Node-Verzeichnis](index.md) · [English](../en/threshold-trigger.md)
