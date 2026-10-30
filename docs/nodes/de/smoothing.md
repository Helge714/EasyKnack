# Glättung (Smoothing)

## Übersicht

Glättet steigende und fallende Werte exponentiell.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate (Rate) | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Audio | — | — | Inspektor |
| Eingang (In) | [Wert](../../user/de/data-types.md) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md) | Immer |
| Anstieg (Rise) | [Wert](../../user/de/data-types.md) | 0–60 s | 0–60 s | 0.03 s | s, ms, smp | [Auto](../../user/de/rates.md) | Immer |
| Abfall (Fall) | [Wert](../../user/de/data-types.md) | 0–60 s | 0–60 s | 0.03 s | s, ms, smp | [Auto](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md) | ±∞ | [Auto](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Für die Zeitkonstante τ gilt `y += (1 − exp(−dt/τ)) × (x − y)`, bei Anstieg mit Rise, bei Abfall mit Fall. Eine τ erreicht etwa 63% eines Sprungs, drei τ etwa 95%. Null folgt sofort. Der erste gültige Wert initialisiert direkt. Audio glättet Control-Sprünge sampleweise; Control aktualisiert blockweise. Dies ist keine lineare Rampe fester Dauer.

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

[Node-Verzeichnis](index.md) · [English](../en/smoothing.md)
