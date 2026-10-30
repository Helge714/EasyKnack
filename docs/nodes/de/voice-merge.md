# Stimmenzusammenführung (Voice Merge)

## Übersicht

Führt polyphone Stimmen zu einem Signal pro Kanal zusammen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Addieren, Mitteln | — | Addieren | — | — | Immer |
| Stimmen (Voices) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audio | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Add summiert alle Stimmen; Mean teilt durch die konfigurierte Stimmenzahl, einschließlich stummer oder losgelassener Stimmen. Stereopositionen bleiben erhalten. Ein einzelner Stimmenpfad wird durchgereicht. Effekte nach Merge laufen auf dem gemeinsamen Signal statt je Stimme.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Stimmen (Voices) | Audio |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/voice-merge.md)
