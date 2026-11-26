# Kompressor (Compressor)

## Übersicht

Verringert den Dynamikumfang mit einem Feed-forward-Peak-Kompressor.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Sidechain | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Schwelle (Threshold) | [Value](../../user/de/data-types.md#value) | −120–24 dB | −120–24 dB | −12 dB | [Factor](../../user/de/units.md) / [%](../../user/de/units.md) / [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Verhältnis (Ratio) | [Value](../../user/de/data-types.md#value) | 1–100 | 1–100 | 4 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Übergangsbreite (Knee) | [Value](../../user/de/data-types.md#value) | 0–48 dB | 0–48 dB | 6 dB | [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Anstiegszeit (Attack) | [Value](../../user/de/data-types.md#value) | 0–2000 ms | 0–2000 ms | 10 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Abklingzeit (Release) | [Value](../../user/de/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Ausgangsverstärkung (Make-up Gain) | [Value](../../user/de/data-types.md#value) | −60–24 dB | −60–24 dB | 0 dB | [Factor](../../user/de/units.md) / [%](../../user/de/units.md) / [dB](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Stereokopplung (Stereo Link) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Stereo](../../user/de/data-types.md#channels) |

## Verhalten

Verwendet die weiche Dynamikkennlinie und glättet die Verstärkungsabsenkung exponentiell in dB. Anstiegszeit gilt bei stärkerer Absenkung, Abklingzeit beim Erholen der Verstärkung. Null bedeutet sofortige Reaktion; die Zeiten sind Einpol-Zeitkonstanten. Ohne Sidechain-Verbindung wird das Hauptsignal gemessen; ein verbundenes Sidechain-Signal steuert nur die Verstärkung. Stereokopplung verwendet den größeren Detektor-Betrag und die Parameterwerte des linken Kanals für eine gemeinsame Verstärkung beider Kanäle. Ohne Kopplung werden Detektor und Parameter kanalgetrennt ausgewertet. Ausgangsverstärkung ist eine manuelle Anhebung, kein automatischer Lautheitsausgleich. Der Kompressor fügt keine Vorausschau-Latenz hinzu und garantiert keine Pegelobergrenze.

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

[Node index](index.md) · [English](../en/compressor.md)
