# Kanäle verbinden (Channel Combine)

## Übersicht

Erstellt ein Stereosignal aus zwei Eingängen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Links (Left) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |
| Rechts (Right) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Stereoausgang (Stereo Out) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | Stereo |

## Verhalten

Left liefert den linken, Right den rechten Ausgang. Von einer Stereoquelle an Left wird nur links verwendet, von einer Stereoquelle an Right nur rechts. Unverbundene Seiten bleiben stumm.

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

[Node-Verzeichnis](index.md) · [English](../en/channel-combine.md)
