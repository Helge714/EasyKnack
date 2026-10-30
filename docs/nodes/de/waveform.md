# Wellenform (Waveform)

## Übersicht

Erstellt eine editierbare periodische Wavetable-Ressource.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Editor](../../user/de/reference-tables.md) | Kurven- / Ressourceneditor | — | Sinus | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Wavetable-Ressource](../../user/de/data-types.md) | — | [Control](../../user/de/rates.md) | — |

## Verhalten

Im Kurvenmodus definieren Kontrollpunkte eine periodische Wellenform. Der Ressourcenmodus bearbeitet Abtastwerte direkt und ohne Béziersegmente. Ein Moduswechsel kann die bisherige Darstellung verwerfen. Aufgenommene Wavetables lassen sich als Sample-Ressourcen einfügen. Verbinde den Ausgang mit dem Wellenformeingang eines Oszillators; die Ressource spielt sich nicht selbst ab.

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

[Node-Verzeichnis](index.md) · [English](../en/waveform.md)
