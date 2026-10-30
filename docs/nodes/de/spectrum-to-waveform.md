# Spektrum zu Wellenform (Spectrum to Waveform)

## Übersicht

Erfasst eine periodische Wavetable aus einem Spektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Grundfrequenz (Fundamental) | [Wert](../../user/de/data-types.md#value) | 1–20000 Hz | 1–20000 Hz | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Erfassen (Capture) | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Normieren (Normalize) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |
| Erfasste Ressource kopieren (Copy captured resource) | [Editor](../../user/de/reference-tables.md) | Schaltfläche | — | — | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Wavetable-Ressource](../../user/de/data-types.md#wavetable) | 2048 Samples | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Erfassen fordert eine Hintergrundrekonstruktion aus einem gültigen FFT-Frame an; Grundfrequenz dient als harmonische Referenz. Der Ausgang hält die Ressource bis zur nächsten Erfassung, ohne sie je Block neu zu berechnen. Normieren skaliert die erfasste Wellenform. Erfasste Ressource kopieren legt Sampledaten in die Anwendungszwischenablage; Einfügen erzeugt eine [Waveform](waveform.md) im Ressourcenmodus. Erfasste Samples werden im Dokument gespeichert. [Wavetable Viewer](wavetable-viewer.md) zeigt sie an.

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

[Node-Verzeichnis](index.md) · [English](../en/spectrum-to-waveform.md)
