# Spektralstatistik (Spectrum Statistics)

## Übersicht

Reduziert angezeigte Spektralwerte auf eine Zahl.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Daten (Data) | [Editor](../../user/de/reference-tables.md) | Magnitude / Phase / Power | — | Magnitude | — | — | Immer |
| Statistik (Statistic) | [Editor](../../user/de/reference-tables.md) | Peak / Mean / RMS / Integral | — | Peak | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | Statistikabhängig | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Magnitude ist die einseitige Amplitude, Power deren Quadrat und Phase der Winkel in Radiant. Für die gewählten Binwerte v[k]: Peak = max(abs(v)), Mean = Σv/K, RMS = sqrt(Σv²/K), Integral = Σv·Δf. Bin-RMS ist nicht das Audio-RMS im Zeitbereich. Das Ergebnis bleibt bis zum nächsten Frame stehen.

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

[Node-Verzeichnis](index.md) · [English](../en/spectrum-statistics.md)
