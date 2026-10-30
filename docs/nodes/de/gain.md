# Verstärkung (Gain)

## Übersicht

Skaliert ein Audiosignal mit einem Amplitudenfaktor.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Verstärkung (Gain) | [Wert](../../user/de/data-types.md) | 0–1.995262 × | 0–1e6 × | 0.251189 × | ×, %, dB | [Auto](../../user/de/rates.md) | Immer |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

`y = x × gain`, mit `gain = 10^(dB/20)`. Manuelle −12 dB entsprechen etwa 0,251189×. Werte bis −160 dB schalten das Signal stumm. Verbundene Verstärkung wird auf 120 dB begrenzt; manuelle Änderungen werden geglättet. Gain begrenzt das Audiosignal nicht.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/gain.md)
