# Vektormathematik (Vector Math)

## Übersicht

Berechnet Operationen mit vektorwertigem Ergebnis.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Operation | [Editor](../../user/de/reference-tables.md) | Add, Subtract, Multiply, Divide, Scale, Normalize, Negate, Absolute, Minimum, Maximum, Cross | — | Add | — | — | Immer |
| A | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| B | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Binäre Operation |
| Faktor (Factor) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 1 | — | [Auto](../../user/de/rates.md#auto) | Scale-Operation |
| Größe (Quantity) | [Editor](../../user/de/reference-tables.md) | Länge, Winkel, Skalierung, Einheitenlos | — | Länge | — | — | Inspektor |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Add/Subtract/Multiply/Divide/Minimum/Maximum arbeiten komponentenweise. Division durch eine Nullkomponente liefert dort null. Scale multipliziert A mit Factor. Normalize liefert `A/length(A)` oder bei Länge null den Nullvektor. Cross berechnet das rechtshändige Kreuzprodukt. Negate und Absolute wirken je Komponente. Skalare Ergebnisse liefern [Vector Length](vector-length.md), [Vector Distance](vector-distance.md) und [Vector Dot Product](vector-dot-product.md).

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| A | Vektor (Vector) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/vector-math.md)
