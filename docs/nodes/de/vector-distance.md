# Vektorabstand (Vector Distance)

## Übersicht

Berechnet den euklidischen Abstand zweier Vektoren.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| B | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Größe (Quantity) | [Editor](../../user/de/reference-tables.md) | Länge, Winkel, Skalierung, Einheitenlos | — | Länge | — | — | Inspektor |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Abstand (Distance) | [Wert](../../user/de/data-types.md#value) | 0–∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

`distance = length(A − B)`. Liefert einen Abstand pro Kanal mit eingangsabhängiger Rate. Quantity wählt die Einheitendarstellung, keine andere Abstandsmetrik.

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

[Node-Verzeichnis](index.md) · [English](../en/vector-distance.md)
