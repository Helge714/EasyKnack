# Audio-Ausgang (Audio Output)

## Übersicht

Sendet Audio an ein Ausgabegerät.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Gerät (Device) | [Editor](../../user/de/reference-tables.md) | Verfügbare Ausgabegeräte | — | Standardgerät | — | — | Immer |
| Gerätelautstärke (Device volume) | [Editor](../../user/de/reference-tables.md) | 0–100 % | — | Aktuelle Windows-Lautstärke | [%](../../user/de/units.md) | — | Windows-Ausgang |
| Lautstärketasten koppeln (Link volume keys) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Windows-Ausgang |
| Signal | [Editor](../../user/de/reference-tables.md) | Pegelanzeige | — | — | — | — | Windows-Ausgang |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

MIDI-Stimmen werden automatisch summiert. Gerätelautstärke steuert den Windows-Endpunkt und übernimmt externe Änderungen; sie ist kein zusätzlicher Signal-Gain. Gekoppelte Lautstärketasten verändern die gewählten Endpunkte proportional. Mute unterdrückt das EasyKnack-Signal, ohne das Windows-Gerät stummzuschalten.

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

[Node-Verzeichnis](index.md) · [English](../en/audio-output.md)
