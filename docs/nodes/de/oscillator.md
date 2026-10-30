# Oszillator (Oscillator)

## Übersicht

Erzeugt eine periodische Wellenform.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Wavetable-Ressource](../../user/de/data-types.md#wavetable) | Sinus, Dreieck, Sägezahn, Rechteck | — | Sinus | — | [Control](../../user/de/rates.md#control) | Immer |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 1e-5–20 000 Hz | 0–20 000 Hz | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Phase | [Wert](../../user/de/data-types.md#value) | ±∞ turn | ±∞ turn | 0 turn | [turn](../../user/de/units.md#angle), [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle) | [Auto](../../user/de/rates.md#auto) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die laufende Phase steigt pro Sample um `f / fs` Umdrehungen. Phase addiert einen Versatz ohne Neustart; Reset startet vor dem betreffenden Sample neu. Bei Frequenz 0 bleibt die Phase stehen und kann einen konstanten Ausgang erzeugen. Die gewählte oder verbundene Wavetable wird frequenzabhängig bandbegrenzt gelesen. Die Phase wrappt nach einer Umdrehung. Ein manueller Reset erfolgt am nächsten Blockanfang.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/oscillator.md)
