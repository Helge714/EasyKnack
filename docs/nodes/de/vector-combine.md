# Vektor zusammensetzen (Vector Combine)

## Übersicht

Setzt X, Y und Z zu einem Vektor zusammen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| X | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Y | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Z | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md); [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle); [×](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Größe (Quantity) | [Editor](../../user/de/reference-tables.md) | Länge, Winkel, Skalierung, Einheitenlos | — | Länge | — | — | Inspektor |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Vektor (Vector) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Der Ausgangsvektor ist `(X,Y,Z)`. Jeder Zahleneingang kann unabhängig Audio oder Control führen. Quantity wählt Editoren für Länge, Winkel, Skalierung oder einheitenlose Komponenten. Für einen gleichmäßig veränderten Ort muss Audio im nachfolgenden Vektorpfad erhalten bleiben.

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

[Node-Verzeichnis](index.md) · [English](../en/vector-combine.md)
