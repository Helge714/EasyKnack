# Filter (Filter)

## Übersicht

Filtert Audio mit resonantem Tiefpass, Hochpass oder Bandpass.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Modus (Mode) | [Editor](../../user/de/reference-tables.md) | Tiefpass, Hochpass, Bandpass | — | Tiefpass | — | — | Immer |
| Ordnung (Order) | [Editor](../../user/de/reference-tables.md) | 2, 4, 6, 8 | — | 2 | — | — | Immer |
| Pegelausgleich (Level compensation) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md) | — | ±∞ | — | — | [Audio](../../user/de/rates.md) | Immer |
| Grenzfrequenz (Cutoff) | [Wert](../../user/de/data-types.md) | 5–20 000 Hz | 5 Hz–0.45 × fs | 1000 Hz | Hz, kHz | [Audio](../../user/de/rates.md) | Immer |
| Resonanz (Resonance) | [Wert](../../user/de/data-types.md) | 0.1–30 Q | 0.1–30 Q | 0.707 Q | Q | [Audio](../../user/de/rates.md) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md) | ±∞ | [Audio](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Kaskadiert Filter zweiter Ordnung zur gewählten Gesamtordnung. Grenzfrequenz und Resonanz werden geglättet; höhere Ordnungen verteilen die Resonanz auf die Stufen. Pegelausgleich folgt dem Verhältnis von Eingangs- und Ausgangs-RMS mit 0,25–4× Verstärkung. Er erzeugt keine Lookahead-Latenz, ist kein Limiter und stellt entfernte Spektralanteile nicht wieder her.

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

[Node-Verzeichnis](index.md) · [English](../en/filter.md)
