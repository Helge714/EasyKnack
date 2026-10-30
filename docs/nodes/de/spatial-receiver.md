# Räumlicher Empfänger (Spatial Receiver)

## Übersicht

Gibt eine positionierte Quelle an einem Mono- oder Stereoempfänger aus.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Kanäle (Channels) | [Editor](../../user/de/reference-tables.md) | Mono, Stereo | — | Stereo | — | — | Immer |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Quellposition (Source Position) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 1) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Empfängerposition (Receiver Position) | [Vektor](../../user/de/data-types.md#vector) | ±∞ je Komponente | ±∞ je Komponente | (0, 0, 0) m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Drehwinkel (Yaw) | [Wert](../../user/de/data-types.md#value) | ±∞ ° | ±∞ ° | 0° | [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle), [turn](../../user/de/units.md#angle) | [Auto](../../user/de/rates.md#auto) | Stereo |
| Empfängerabstand (Receiver Spacing) | [Wert](../../user/de/data-types.md#value) | 0–∞ m | 0–∞ m | 0.18 m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Stereo |
| Entfernungsdämpfung (Distance Attenuation) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |
| Referenzabstand (Reference Distance) | [Wert](../../user/de/data-types.md#value) | 0.001–∞ m | — | 1 m | [m](../../user/de/units.md), [km](../../user/de/units.md), [cm](../../user/de/units.md), [mm](../../user/de/units.md) | — | Inspektor |
| Schalllaufzeit (Sound Travel Time) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Abstand (Distance) | [Wert](../../user/de/data-types.md#value) | 0–∞ m | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Stereoeingänge werden zu einer Mono-Punktquelle gemittelt. Stereoempfänger liegen je einen halben Spacing entlang der gedrehten Rechtsachse versetzt; Distance wird pro Seite gemessen. Mono verwendet die Mitte und ignoriert Yaw/Spacing. Die Dämpfung beträgt `reference/max(reference,distance)`; Stereo verwendet leistungsbezogene Richtungsverstärkung. Schalllaufzeit verzögert um `distance/343` Sekunden, bis höchstens 2 Sekunden (686 m); weiter entfernte Quellen sind bei Aktivierung stumm. Geänderter Abstand verändert dann die Tonhöhe. Verwende gleichmäßige Bewegung mit Audiorate für kontinuierliche Dopplerverläufe. Dies rendert Punktquellen im Freifeld, keine Raumakustik, Mikrofoncharakteristik oder HRTF-Simulation.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Audioeingang (Audio In) | Audioausgang (Audio Out) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Audioeingang (Audio In) | Audioausgang (Audio Out) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/spatial-receiver.md)
