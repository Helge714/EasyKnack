# Panorama (Pan)

## Übersicht

Verteilt ein Audiosignal auf links und rechts.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |
| Panorama (Pan) | [Wert](../../user/de/data-types.md) | ±1 | ±1 | 0 | — | [Audio](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Stereoausgang (Stereo Out) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | Stereo |

## Verhalten

Für `a = (pan+1)π/4` beträgt die linke Verstärkung `√2 cos(a)`, die rechte `√2 sin(a)`. In der Mitte bleiben beide Verstärkungen 1; ein Extrem verstärkt die gewählte Seite mit √2 und schaltet die andere stumm. Mono wird positioniert; Stereokanäle werden einzeln skaliert. Vor Voice Merge wirkt dies pro Stimme, danach auf das gemeinsame Signal.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Audioeingang (Audio In) | Stereoausgang (Stereo Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Audioeingang (Audio In) | Stereoausgang (Stereo Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/pan.md)
