# Verzögerung (Delay)

## Übersicht

Verzögert ein Signal und sichert Feedbackschleifen ab.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Eingang (In) | [Audio](../../user/de/data-types.md) | ±∞ | ±∞ | 0 | — | [Audio](../../user/de/rates.md) | Immer |
| Zeit (Time) | [Wert](../../user/de/data-types.md) | 0–10 s | 0–10 s | 0.25 s | s, ms, smp | [Control](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Time wird einmal pro Block gelesen. Bruchteile von Samples werden linear interpoliert. Null reicht außerhalb von Feedback durch; innerhalb wird mindestens ein Sample erzwungen. Der Ausführungsplan wählt je nach Schleifenverzögerung blockgroße oder kürzere Schritte. Die Zeitmodulation muss außerhalb dieser Schleife entstehen. Mute entfernt die Verzögerungsgrenze; eine dadurch ungesicherte Verbindung wird ignoriert statt als unmittelbares Feedback berechnet.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/delay.md)
