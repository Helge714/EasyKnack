# Hilbert

## Übersicht

Erzeugt ein verzögertes Direktsignal und dessen Hilbert-Quadraturkomponente.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Direkt (Direct) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Quadratur (Quadrature) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Ein Hamming-gefensterter FIR-Filter mit 257 Koeffizienten nähert für positive Frequenzen eine Phasenverschiebung um −90° an. Beide Ausgänge haben eine Gruppenlaufzeit von 128 Samples (`128 / sampleRate` Sekunden; 2,67 ms bei 48 kHz). Der Verlaufsspeicher ist beim Start mit Nullen gefüllt; Kanäle und Stimmen besitzen unabhängige Zustände.

Nahe Gleichanteil und Nyquist-Frequenz nimmt die Genauigkeit ab; Quadratur unterdrückt diese beiden Komponenten. Bei 48 kHz sind für genaue Quadratur etwa 400 Hz Übergangsbereich an beiden Enden freizuhalten; diese Grenzen skalieren mit der Samplerate. Ausgänge werden nicht in der Amplitude begrenzt.

Eine Frequenzverschiebung aufwärts ergibt sich durch `Direct * cos(phase) - Quadrature * sin(phase)` mit gemeinsamer Trägerphase. Addition wählt die Verschiebung abwärts. Mute leitet den Eingang unverzögert an Direkt weiter und setzt Quadratur auf 0.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Audioeingang (Audio In) | Direkt (Direct) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Audioeingang (Audio In) | Direkt (Direct) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Nodeverzeichnis](index.md) · [English](../en/hilbert.md)
