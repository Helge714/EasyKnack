# Skalenquantisierer (Scale Quantizer)

## Übersicht

Rundet Frequenzen auf eine gleichstufige Tonleiter.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Skala, Fest | — | Skala | — | — | Immer |
| Grundton (Root) | [Editor](../../user/de/reference-tables.md) | C, C#, D, D#, E, F, F#, G, G#, A, A#, B | — | C | — | — | Immer |
| Skala (Scale) | [Editor](../../user/de/reference-tables.md) | Chromatisch, Dur, Moll, Durpentatonik, Mollpentatonik | — | Chromatisch | — | — | Immer |
| Rate (Rate) | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md) | 0–20 000 Hz | Positive Hz | 440 Hz | Hz, kHz | [Auto](../../user/de/rates.md) | Immer |
| Festes Ziel (Fixed target) | [Wert](../../user/de/data-types.md) | 1–20 000 Hz | Positive Hz | 440 Hz | Hz, kHz | [Auto](../../user/de/rates.md) | Immer |
| A4-Stimmung (A4 tuning) | [Wert](../../user/de/data-types.md) | 400–480 Hz | — | 440 Hz | Hz, kHz | — | Inspektor |
| Hysterese (Hysteresis) | [Wert](../../user/de/data-types.md) | 0–12 st | 0–12 st | 0.1 st | st, cent | [Auto](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ziel (Target) | [Wert](../../user/de/data-types.md) | 0–∞ Hz | [Auto](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Scale bildet `69 + 12 log2(f / A4)` auf die nächste erlaubte Note ab und rechnet zurück in Hz. Bei gleichem Abstand gewinnt die tiefere Note. Hysterese ergänzt einen Umschaltabstand hinter der Mitte; 0 schaltet sofort. Nichtpositive/ungültige Eingänge liefern 0. Chromatisch ignoriert Root. Fixed verwendet das feste Ziel statt der Skala. Ein einschließlich beider Grenzen durchlaufener Oktavbereich kann 13 Noten ergeben: zwölf Halbtonschritte besitzen dreizehn Endpunkte.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Frequenz (Frequency) | Ziel (Target) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Frequenz (Frequency) | Ziel (Target) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/scale-quantizer.md)
