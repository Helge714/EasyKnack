# Digital Noise (Digital Noise)

## Übersicht

Erzeugt zyklusbasierte zufällige Frequenzmodulation.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 1e-5–20 000 Hz | 0–20 000 Hz | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Stärke (Strength) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Verteilung (Distribution) | [Editor](../../user/de/reference-tables.md) | Gleichverteilt, Gauß | — | Gleichverteilt | — | — | Immer |
| Startwert (Seed) | [Wert](../../user/de/data-types.md#value) | −1–16 777 215 | −1–16 777 215, gerundet | −1 | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Faktor (Factor) | [Wert](../../user/de/data-types.md#value) | 2^(−s)–2^s | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | f × Factor | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Factor wird über einen selbstmodulierten Zyklus gehalten; Frequency liefert `f × Factor`. Stärke `s` begrenzt die Oktavabweichung auf `±s`. Gauß verwendet die Standardabweichung `s/3`, begrenzt auf `±s`. Die Ziehung berücksichtigt die Zyklusdauer, damit zeitgewichtete Oktavabweichungen symmetrisch sind. Der arithmetische Hz-Mittelwert bleibt dadurch nicht konstant. Stärke 0 ergibt Faktor 1 und reicht Frequency unverändert durch. Trigger markiert eine neue Ziehung. Seed entspricht [Rauschen](noise.md).

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/digital-noise.md)
