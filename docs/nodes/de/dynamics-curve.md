# Dynamikkennlinie (Dynamics Curve)

## Übersicht

Wandelt einen Amplitudenpegel in einen Kompressor-Verstärkungsfaktor um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Pegel (Level) | [Value](../../user/de/data-types.md#value) | 0–∞ | 0–∞ | 0 | [Factor](../../user/de/units.md) / [%](../../user/de/units.md) / [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Schwelle (Threshold) | [Value](../../user/de/data-types.md#value) | −120–24 dB | −120–24 dB | −12 dB | [Factor](../../user/de/units.md) / [%](../../user/de/units.md) / [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Verhältnis (Ratio) | [Value](../../user/de/data-types.md#value) | 1–100 | 1–100 | 4 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Übergangsbreite (Knee) | [Value](../../user/de/data-types.md#value) | 0–48 dB | 0–48 dB | 6 dB | [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto / Audio / Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Verstärkung (Gain) | [Value](../../user/de/data-types.md#value) | 0–1 | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Mit `x = 20 log10(max(Pegel, 10^-12)) − Schwelle`, `s = 1 / Verhältnis − 1` und `W = Übergangsbreite` beträgt die Verstärkungsänderung in dB: null unterhalb `−W / 2`, `s × (x + W / 2)^2 / (2W)` innerhalb des Übergangs und `s × x` oberhalb `W / 2`. Breite 0 ergibt einen harten Übergang. Der Ausgang liefert `10^(Änderung / 20)`; Verhältnis 1 ergibt Verstärkung 1. Negative Eingangspegel gelten als null. Der Node glättet nicht zeitlich; davor kann ein Hüllkurvenfolger stehen und der Ausgang einen Gain-Node steuern.

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

[Node index](index.md) · [English](../en/dynamics-curve.md)
