# Audio-Ausgang (Audio Output)

## Übersicht

Sendet Audio an ein Ausgabegerät.

Linux verwendet PulseAudio oder PipeWire-Pulse für Gerätewahl und Lautstärke.
Ohne diesen Dienst unterstützt der Rückfallpfad nur die Standard-Audiogeräte.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Gerät (Device) | [Editor](../../user/de/reference-tables.md) | Verfügbare Ausgabegeräte | — | Standardgerät | — | — | Immer |
| Gerätelautstärke (Device volume) | [Editor](../../user/de/reference-tables.md) | 0–100 % | — | Aktuelle Gerätelautstärke | [%](../../user/de/units.md) | — | Windows- oder Linux-Ausgang |
| Lautstärketasten koppeln (Link volume keys) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Windows- oder Linux-Ausgang |
| Signal | [Editor](../../user/de/reference-tables.md) | Pegelanzeige | — | — | — | — | Windows- oder Linux-Ausgang |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

MIDI-Stimmen werden automatisch summiert. Gerätelautstärke steuert den Windows-Endpunkt und übernimmt externe Änderungen; sie ist kein zusätzlicher Signal-Gain. Gekoppelte Lautstärketasten verändern die gewählten Endpunkte proportional. Mute unterdrückt das EasyKnack-Signal, ohne das Windows-Gerät stummzuschalten.

Unter Linux wird die Lautstärke des Pulse-/PipeWire-Ausgangs gesteuert. Gekoppelte
Tasten folgen Änderungen der Desktop-Standardlautstärke, auch Änderungen im
Lautstärkemenü, und erhalten die relativen Pegel der gewählten Ausgänge. Dafür
werden Desktop-Lautstärkeänderungen statt eines globalen Windows-Tastaturhooks
verwendet. ASIO-Endpunkte bleiben Windows-spezifisch.

Gekoppelte Lautstärketasten zeigen kurz eine Einblendung auf dem Desktop, auch über anderen Programmen. Bei mehreren gekoppelten Ausgängen entspricht die Prozentanzeige dem höchsten Ausgangspegel; die relativen Lautstärkeverhältnisse bleiben erhalten.

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
