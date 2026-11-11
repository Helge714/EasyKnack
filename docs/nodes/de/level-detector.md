# Pegeldetektor (Level Detector)

## Übersicht

Misst Audio-Spitzenwert und gleitenden RMS-Pegel.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Kanal (Channel) | [Editor](../../user/de/reference-tables.md) | Links / Rechts / Mix | — | Links | — | — | Immer |
| RMS-Fenster (RMS window) | [Editor](../../user/de/reference-tables.md) | 0.1–2000 ms | — | 50 ms | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |
| Glättung (Smoothing) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Anstieg (Attack) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 10 ms | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer; aktiv bei Glättung |
| Abfall (Release) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 100 ms | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer; aktiv bei Glättung |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spitzenwert (Peak) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| RMS | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Spitzenwert = max(abs(x)) des aktuellen Blocks; RMS = sqrt(mean(x²)) über das zurückliegende Fenster. Zu Beginn verwendet RMS nur eingetroffene Samples. Polyphone Stimmen werden zuerst summiert; Mix = (L+R)/2. Optionale getrennte Anstiegs-/Abfallglättung folgt beiden Messungen; eine Zeitkonstante erreicht etwa 63% eines Sprungs. Zeit null reagiert sofort. Ausgangseinheiten sind Faktor, % oder dB; das RMS eines voll ausgesteuerten Sinus ist ungefähr 0.707. Es gibt keine Frequenzgewichtung.

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

[Node-Verzeichnis](index.md) · [English](../en/level-detector.md)
