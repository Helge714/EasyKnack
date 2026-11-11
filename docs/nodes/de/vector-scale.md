# Vektor skalieren (Vector Scale)

## Übersicht

Skaliert jede Vektorachse um einen Ursprung.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Skalierung (Scale) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (1, 1, 1) × | [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Ursprung (Origin) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

`vout = origin + scale ⊙ (v − origin)`, wobei ⊙ komponentenweise multipliziert. Skalierung 1 erhält eine Achse, 0 reduziert sie auf den Ursprung, negative Werte spiegeln sie. Dieser Node dreht nicht.

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

[Node-Verzeichnis](index.md) · [English](../en/vector-scale.md)
