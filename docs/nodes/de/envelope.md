# Hüllkurve (Envelope)

## Übersicht

Erzeugt eine kurvenförmige Hüllkurve aus Gate und Trigger.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Hüllkurve (Envelope) | [Editor](../../user/de/reference-tables.md) | Kurveneditor | — | ADSR | — | — | Immer |
| Gate | [Wert](../../user/de/data-types.md) | 0–1 | ±∞ | 0 | — | [Audio](../../user/de/rates.md) | Immer |
| Trigger | [Trigger](../../user/de/data-types.md) | Button | — | — | — | [Trigger](../../user/de/rates.md) | Immer |
| Attack + Decay | [Wert](../../user/de/data-types.md) | 0–60 000 ms | 0–60 000 ms | 300 ms | ms, s, smp | [Auto](../../user/de/rates.md) | Immer |
| Ausklingen (Release) | [Wert](../../user/de/data-types.md) | 0–60 000 ms | 0–60 000 ms | 300 ms | ms, s, smp | [Auto](../../user/de/rates.md) | Immer |
| Halteposition (Sustain position) | [Wert](../../user/de/data-types.md) | 0.01–0.99 | 0.01–0.99 | 0.5 | — | [Auto](../../user/de/rates.md) | Immer |
| Halten (Hold) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Hüllkurve (Envelope) | [Wert](../../user/de/data-types.md) | 0–1 | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Ein Gate-Anstieg über 0 oder ein Trigger startet Attack/Decay. Die Halteposition teilt die Kurve in Attack/Decay und Release. Bei Hold hält ein positives Gate den Sustainwert; das Loslassen startet Release vom aktuellen Pegel. Ohne Hold folgt Release automatisch auf Attack/Decay. Retrigger beginnen beim aktuellen Hüllkurvenpegel. Das Ergebnis ist auf 0–1 begrenzt.

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

[Node-Verzeichnis](index.md) · [English](../en/envelope.md)
