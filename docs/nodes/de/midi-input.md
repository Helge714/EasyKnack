# MIDI-Eingang (MIDI Input)

## Übersicht

Empfängt MIDI von einem Gerät oder der Computertastatur.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Gerät (Device) | [Editor](../../user/de/reference-tables.md) | Verfügbare MIDI-Geräte | — | Tastatur | — | — | Immer |
| Transposition (Transpose) | [Editor](../../user/de/reference-tables.md) | −48–48 st | — | 0 st | [cent](../../user/de/units.md), [st](../../user/de/units.md), [oct](../../user/de/units.md), [Faktor](../../user/de/units.md) | — | Tastatur gewählt |
| Tastatur (Keyboard) | [Editor](../../user/de/reference-tables.md) | Tastaturfokus | — | Inaktiv | — | — | Tastatur gewählt |
| Alle Noten aus (All notes off) | [Editor](../../user/de/reference-tables.md) | Schaltfläche | — | — | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | MIDI-Ereignisse | [MIDI](../../user/de/rates.md) | — |

## Verhalten

Wähle einen Hardware-Eingang oder Keyboard. Ein Klick auf die Tastatursteuerung aktiviert die musikalischen Tasten; ein Klick außerhalb gibt den Fokus frei. Ohne Transposition spielt A den Ton C4. Alle Noten aus beendet aktive Noten und leert wartende MIDI-Verzögerungen.

Hardware- und virtuelle Linux-Eingänge verwenden den ALSA-Sequencer. Dafür werden
`libasound.so.2` und ein zugängliches Sequencer-Gerät benötigt. Fehlende oder
getrennte Eingänge werden gemeldet; die Computertastatur bleibt verfügbar.

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

[Node-Verzeichnis](index.md) · [English](../en/midi-input.md)
