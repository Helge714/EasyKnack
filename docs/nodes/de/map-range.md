# Bereichsabbildung (Map Range)

## Übersicht

Bildet einen Zahlenbereich linear auf einen anderen ab.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Begrenzen ±1 (Clamp ±1) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Quellminimum (From Min) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Quellmaximum (From Max) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Zielminimum (To Min) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Zielmaximum (To Max) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

`t = (x − From Min) / (From Max − From Min)`; `y = To Min + t × (To Max − To Min)`. Clamp begrenzt t auf 0–1, trotz der Beschriftung nicht den Ausgang auf ±1. Ohne Clamp wird außerhalb des Quellbereichs extrapoliert. Ein entarteter Quellbereich liefert To Min. Umgekehrte Intervalle kehren die Abbildung um.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Wert (Value) | Ausgang (Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Wert (Value) | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/map-range.md)
