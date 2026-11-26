# Allpass

## Übersicht

Verändert die frequenzabhängige Phase bei unverändertem stationärem Amplitudengang.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Ordnung (Order) | [Editor](../../user/de/reference-tables.md) | 1 / 2 | — | 1 | — | — | Immer |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Frequenz (Frequency) | [Value](../../user/de/data-types.md#value) | 1–20000 Hz | 1 Hz–0.49 × fs | 1000 Hz | [Hz](../../user/de/units.md) / [kHz](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Güte (Resonance) | [Value](../../user/de/data-types.md#value) | 0.1–30 Q | 0.1–30 Q | 0.70710678 Q | [Q](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Ordnung = 2 |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Ordnung 1 verwendet einen bilinear transformierten Allpass mit `a = (tan(πf / fs) − 1) / (tan(πf / fs) + 1)`. Ordnung 2 verwendet den RBJ-Biquad-Allpass mit einstellbarer Güte. Bei konstanten Parametern beträgt der Amplitudengang bei jeder Frequenz eins. Das Mischen mit dem Direktsignal erzeugt Phasenauslöschungen; mehrere Stufen ermöglichen Phaser-Netzwerke. Ein Ordnungswechsel setzt den Filterzustand zurück. Audioratenmodulation wird unterstützt; zeitlich veränderliche Parameter müssen die momentane Amplitude nicht erhalten.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Audioeingang (Audio In) | Audioausgang (Audio Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Audioeingang (Audio In) | Audioausgang (Audio Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node index](index.md) · [English](../en/allpass.md)
