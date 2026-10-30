# Wertemittelung (Value Average)

## Übersicht

Führt eine getriggerte arithmetische Messung aus und hält ihr Endergebnis.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Start | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Messen nach (Measure by) | [Editor](../../user/de/reference-tables.md) | Zeit / Messungen | — | Zeit | — | — | Immer |
| Dauer (Duration) | [Editor](../../user/de/reference-tables.md) | 1 smp–86400 s | — | 1 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Zeit |
| Messungen (Measurements) | [Editor](../../user/de/reference-tables.md) | 1–16777216 | — | 16 | — | — | Messungen |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Mittelwert (Mean) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Abgeschlossen (Finished) | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |
| Standardabweichung (Standard Deviation) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Minimum | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Maximum | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Start löscht und beginnt die Aufnahme neu und übernimmt Dauer/Messungen. Zeit misst die Audiouhrdauer; Messungen stoppt nach der gewählten Anzahl akzeptierter Werte. Abgeschlossen sendet einmal beim Ende. Das laufende Ergebnis ist während der Aufnahme verfügbar und bleibt danach bis Start stehen.

Audio contributes every finite sample; Control contributes once per block, plus a value at Start. Mean = Σx/N, population standard deviation = sqrt(Σ(x−mean)²/N). Minimum/Maximum describe the same accepted values; deviation is zero for fewer than two. All retain the input’s numerical unit.~Audio liefert jedes endliche Sample; Control einmal je Block sowie einen Wert bei Start. Mittelwert = Σx/N, Populationsstandardabweichung = sqrt(Σ(x−mean)²/N). Minimum/Maximum beschreiben dieselben akzeptierten Werte; bei weniger als zwei ist die Abweichung null. Alle behalten die numerische Eingangseinheit.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Wert (Value) | Mittelwert (Mean) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Wert (Value) | Mittelwert (Mean) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/value-average.md)
