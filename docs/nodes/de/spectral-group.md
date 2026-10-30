# Spektralgruppe (Spectral Group)

## Übersicht

Verarbeitet jeden Bin eines eingehenden Spektrums.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md) | — | — | — | — | [Control](../../user/de/rates.md) | Immer |
| Frame-Eingänge (Frame inputs) | [Wert](../../user/de/data-types.md) | Schnittstellenabhängig | Audio / Control / Trigger | Schnittstellenabhängig | — | [Auto](../../user/de/rates.md) | Immer |
| Leere Bins überspringen (Skip empty bins) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Inspektor |
| Parallele Bins (Parallel bins) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Inspektor |
| Allgemein / IO / Editoren (General / IO / Editors) | [Editor](../../user/de/reference-tables.md) | Gruppendefiniert | — | — | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Spectrum-Ressource](../../user/de/data-types.md) | — | [Control](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Ein Spectrum bestimmt Binanzahl und Framezeitpunkte. [Bin Context](bin-context.md) stellt komplexe Komponenten, Frequenz und Index bereit; [Bin Reader](bin-reader.md) liest andere Bins. [Bin Output](bin-output.md) schreibt unabhängige komplexe Ausgänge; die Anfangsverdrahtung reicht die komplexen Werte unverändert durch. Externe Frame-Werte werden am FFT-Fensterende abgetastet; asynchrone Pfade erhalten ihre zeitliche Zuordnung. Frame-Trigger wirken auf den zugehörigen Frame. Reine Frame-Verarbeitung läuft einmal je Bin/Frame, nicht je Audiosample. Oszillatoren/Rauschen erzeugen dauerndes Audio je Bin, reduziert über Instance Merge; Bin Output nimmt dieses Audio nicht an. Bin Discard läuft vor der Binberechnung und betrifft alle Spectrum-Ausgänge. Parallele Worker erhalten unabhängige Binzustände. [IFFT](ifft.md) rekonstruiert komplexe Spektren; eine Oszillatorbank ist keine phasengleiche IFFT.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Spektrum (Spectrum) | Ausgang (Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Spektrum (Spectrum) | Ausgang (Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/spectral-group.md)
