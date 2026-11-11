# Vektor drehen (Vector Rotate)

## Übersicht

Dreht einen Vektor um einen Ursprung.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Rotation | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0)° | [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle) | [Auto](../../user/de/rates.md#auto) | Immer |
| Ursprung (Origin) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

`vout = origin + Rz Ry Rx (v − origin)`. Verwendet rechtshändige Eulerrotationen: zuerst X, dann Y, dann Z. Winkel sind periodisch; eine laufende Drehung muss nicht manuell bei 360° gewrappt werden. Die Reihenfolge ist relevant. Origin und Vector verwenden Längenkomponenten, Rotation Winkel.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Vektor (Vector) | Vektor (Vector) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/vector-rotate.md)
