# Spektrum ausdünnen (Spectrum Prune)

## Übersicht

Behält den lautesten Anteil der Spektralbins.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Behalten (Keep) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 1 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | Komplexe FFT-Bins | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die Anzahl wird aus Behalten mal Anzahl nichtnegativer Bins gerundet, einschließlich DC und Nyquist. Die Rangfolge verwendet einseitige Amplituden; bei Gleichstand werden kleinere Indizes bevorzugt. Erhaltene Koeffizienten und Phasen bleiben unverändert, ohne Pegelnormierung. Behalten 0 löscht alle Bins, 1 umgeht die Verarbeitung. Eine [Spectral Group](spectral-group.md) kann Nullbins bei aktivem Skip empty bins überspringen.

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

[Node-Verzeichnis](index.md) · [English](../en/spectrum-prune.md)
