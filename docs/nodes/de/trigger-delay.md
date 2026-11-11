# Triggerverzögerung (Trigger Delay)

## Übersicht

Verzögert Trigger und kann Trigger-Feedbackschleifen absichern.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Zeit (Time) | [Wert](../../user/de/data-types.md#value) | 1 smp–10 s | 1 smp–10 s | 0.25 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Time wird auf Samples gerundet und auf mindestens ein Sample begrenzt. Bereits geplante Ereignisse behalten bei späteren Time-Änderungen ihren Fälligkeitstermin. Reset löscht wartende Ereignisse. Mehrere am selben Sample fällige Ereignisse werden zu einem Trigger zusammengefasst. Der Delay ist eine Ausführungsgrenze für Feedback; seine Zeit muss von außerhalb der Schleife gesteuert werden.

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

[Node-Verzeichnis](index.md) · [English](../en/trigger-delay.md)
