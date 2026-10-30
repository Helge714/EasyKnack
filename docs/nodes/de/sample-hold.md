# Abtasten und Halten (Sample & Hold)

## Übersicht

Erfasst und hält einen Wert bei einem Trigger.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Eingang (In) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Jeder Trigger erfasst den Eingang an seiner Sampleposition; das Ergebnis bleibt bis zum nächsten Trigger erhalten. Der anfängliche Haltewert ist 0. Auto wählt bei verbundenem Trigger Audio, damit Ereignisse innerhalb des Blocks sichtbar bleiben. Erzwungenes Control meldet den Blockanfangswert; spätere Erfassungen wirken auf den folgenden Ausgang. Manuelle Erfassung erfolgt zum nächsten Block.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Eingang (In) | Ausgang (Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Eingang (In) | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/sample-hold.md)
