# Binausgang (Bin Output)

## Übersicht

Schreibt einen komplexen Spectrum-Ausgang aus Binwerten.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Komponenten (Components) | [Editor](../../user/de/reference-tables.md) | Real / Imaginär, Betrag / Phase | — | Real / Imaginär | — | — | Immer |
| Realteil / Betrag (Real / Magnitude) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞; polarer Betrag ≥ 0 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Imaginärteil / Phase (Imaginary / Phase) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Kartesisch wird `Real + i × Imaginary` geschrieben; polar `max(0,Magnitude) × exp(i×Phase)` mit Phase in Radiant. Jeder Bin Output erzeugt ein eigenes Spectrum bei gemeinsamem Bin-Ausführungsplan. Die Gruppe stellt die konjugierte Symmetrie für reelle Signale her. Es dürfen nur Frame-/Binberechnungen hineinführen, kein Oszillatoraudio. Siehe [Spectral Group](spectral-group.md).

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Nein | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/bin-output.md)
