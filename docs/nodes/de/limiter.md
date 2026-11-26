# Limiter

## Übersicht

Begrenzt Stereo-Sample-Pegel auf eine einstellbare Obergrenze.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Pegelgrenze (Ceiling) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0.98 | [Factor](../../user/de/units.md) / [%](../../user/de/units.md) / [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Abklingzeit (Release) | [Value](../../user/de/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Vorausschau (Lookahead) | [Editor](../../user/de/reference-tables.md) | 0–20 ms | — | 5 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±Pegelgrenze | [Audio](../../user/de/rates.md#audio) | [Stereo](../../user/de/data-types.md#channels) |

## Verhalten

Beide Kanäle verwenden eine gemeinsame Verstärkung aus dem größeren Sample-Betrag. Pegelgrenze und Abklingzeit verwenden die Parameterwerte des linken Kanals. Ein gleitendes Maximum erfasst das Vorausschau-Fenster; nötige Absenkung erfolgt sofort, die Erholung exponentiell mit der Abklingzeit. Vorausschau verzögert beide Kanäle um `round(ms × fs / 1000)` Samples; null fügt keine Verzögerung hinzu. Eine Änderung leert den Verzögerungsspeicher. Mute überbrückt den Node und entfernt damit seine Latenz und Begrenzung. Parallele Direktpfade müssen bei Bedarf manuell zeitlich angeglichen werden. Die Grenze gilt für digitale Samples, auch während Parameteränderungen. Dies ist kein überabgetasteter True-Peak-Limiter und garantiert keine Begrenzung rekonstruierter Zwischen-Sample-Spitzen. Pegelgrenze 0 ergibt Stille.

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

[Node index](index.md) · [English](../en/limiter.md)
