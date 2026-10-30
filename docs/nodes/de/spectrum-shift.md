# Spektrumverschiebung (Spectrum Shift)

## Übersicht

Verschiebt ein Spektrum um einen Binversatz.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Verschiebung (Bins) (Shift (bins)) | [Wert](../../user/de/data-types.md#value) | ±8192 | ±∞ | 0 | bins | [Auto](../../user/de/rates.md#auto) | Immer |
| Kontinuierliche Phase (Continuous phase) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | Komplexe FFT-Bins | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Positive Verschiebung verschiebt den Inhalt um `shift·sampleRate/FFTSize` Hz nach oben. Gebrochene Verschiebungen interpolieren komplexe Koeffizienten; Inhalt außerhalb des gültigen Bereichs entfällt. Kontinuierliche Phase kompensiert die Frame-Zeitphase bei konstantem Versatz. Dies ist Frequenztranslation statt proportionaler musikalischer Transposition; schnelle Modulation kann Artefakte erzeugen.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Spektrum (Spectrum) | Spektrum (Spectrum) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Spektrum (Spectrum) | Spektrum (Spectrum) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/spectrum-shift.md)
