# Inverse FFT (IFFT)

## Übersicht

Rekonstruiert Audio aus komplexen Spectrum-Frames.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Stabilität (Stability) | [Wert](../../user/de/data-types.md#value) | 0–1 | — | 0 | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Verwendet inverse FFT und normiertes Overlap-Add mit Zeit- und Fenstermetadaten der Quelle. Ein synchroner Pfad besitzt eine Fensterlänge Rekonstruktionslatenz. Asynchrone FFT wird angenommen, verspätete/fehlende Frames können aber Lücken hinterlassen. Ein Wechsel der Quellidentität setzt den Überlappungszustand zurück. Stability verwendet begrenzte RMS-Korrektur mit kurzer/langer Nachführung; 0 bleibt unverändert, 1 korrigiert vollständig. Sie erzeugt keine Lookahead-Latenz, kann aber gewollte Dynamik abschwächen und Spektren verbreitern. Siehe [Spectral Group](spectral-group.md).

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

[Node-Verzeichnis](index.md) · [English](../en/ifft.md)
