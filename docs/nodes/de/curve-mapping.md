# Kurvenabbildung (Curve Mapping)

## Übersicht

Bildet Werte durch eine editierbare Kurve ab.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Kurve (Curve) | [Editor](../../user/de/reference-tables.md) | Kurveneditor | — | Identität | — | — | Immer |
| Außerhalb des Bereichs (Outside range) | [Editor](../../user/de/reference-tables.md) | Begrenzen, Linear fortsetzen | — | Begrenzen | — | — | Immer |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Eingangsminimum (Input min) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | — | 0 | — | — | Immer |
| Eingangsmaximum (Input max) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | — | 1 | — | — | Immer |
| Ausgangsminimum (Output min) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | — | 0 | — | — | Immer |
| Ausgangsmaximum (Output max) | [Wert](../../user/de/data-types.md#value) | ±1 000 000 | — | 1 | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Wert](../../user/de/data-types.md#value) | ±1e12 | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Normiert den Eingang anhand Input min/max, liest die Kurve und skaliert auf Output min/max. Clamp hält außerhalb des Quellbereichs die Kurvenendwerte; Linear extension setzt die Randsteigungen fort. Ein Quellbereich der Breite null liefert Output min. Unterstützt Bézier-Kontrollpunkte und die direkte Verwendung in Spectral Groups.

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

[Node-Verzeichnis](index.md) · [English](../en/curve-mapping.md)

Kontrollpunkte und Bézier-Griffe erscheinen beim Hover oder im aktiven Graph-Editor. Ein Klick außerhalb blendet sie aus; die Kurve bleibt sichtbar.

Die Punktform zeigt den folgenden Abschnitt: Kreis = Bézier, Quadrat = Linear, Raute = Step. Der letzte Punkt verwendet den vorherigen Abschnitt.
