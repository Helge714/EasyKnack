# Binkontext (Bin Context)

## Übersicht

Stellt den aktuellen Bin einer Spectral Group bereit.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Realteil (Real) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Imaginärteil (Imaginary) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Betrag (Magnitude) | [Wert](../../user/de/data-types.md#value) | 0–∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Phase | [Wert](../../user/de/data-types.md#value) | ±π rad | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 0–fs/2 Hz | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Binindex (Bin index) | [Wert](../../user/de/data-types.md#value) | 0–N/2 | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Position | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Framezeit (Frame time) | [Wert](../../user/de/data-types.md#value) | ±∞ s | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Nur innerhalb einer [Spectral Group](spectral-group.md) gültig. Bei FFT-Größe N hat Bin k die Frequenz `k × fs/N` und Position `k/(N/2)`. Real/Imaginary und Magnitude sind rohe komplexe FFT-Koeffizienten, keine normierten Amplituden. Framezeit bezeichnet den Fensterbeginn und kann anfangs durch Nullauffüllung negativ sein. Die angezeigte Audiorate bezeichnet hier einen je Frame ausgewerteten Binvektor, kein zeitliches Audio.

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

[Node-Verzeichnis](index.md) · [English](../en/bin-context.md)
