# Bitcrusher (Bitcrusher)

## Übersicht

Verringert Amplitudenauflösung und Aktualisierungsrate.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Eingang (In) | [Audio](../../user/de/data-types.md#audio) | ±1 | ±1 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Bits | [Wert](../../user/de/data-types.md#value) | 1–24, ganzzahlig | 1–24, gerundet | 8 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Unterabtastung (Downsample) | [Wert](../../user/de/data-types.md#value) | 1–1024, ganzzahlig | 1–1024, gerundet | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Quantisiert den begrenzten Eingang auf die gewählte Bittiefe und hält einen Wert über Downsample-Auswertungen. Downsample 1 aktualisiert bei jeder Auswertung. Vor dieser absichtlichen Ratenreduktion findet keine Anti-Aliasing-Tiefpassfilterung statt.

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

[Node-Verzeichnis](index.md) · [English](../en/bitcrusher.md)
