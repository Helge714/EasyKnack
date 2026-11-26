# Delay-Allpass (Delay Allpass)

## Übersicht

Verteilt Audio zeitlich über einen verzögerungsbasierten Allpass.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Verzögerung (Delay) | [Value](../../user/de/data-types.md#value) | 1/48000–1 s | 1/fs–1 s | 10 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Immer |
| Rückkopplung (Feedback) | [Value](../../user/de/data-types.md#value) | ±0.99 | ±0.99 | 0.5 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Für eine ganzzahlige Verzögerung D und Rückkopplung g gilt `y[n] = v[n − D] − g × x[n]` und `v[n] = x[n] + g × y[n]`. Bei konstanten Parametern ergibt sich ein Allpass mit Amplitudengang eins; dies ist nicht die angenäherte Allpass-Stufe des klassischen Freeverb. Die Verzögerung wird auf ganze Samples gerundet, mindestens ein Sample, und einmal pro Block ausgewertet. Verzögerungsänderungen können klicken. Rückkopplung wird auf ±0.99 begrenzt. Durch den unmittelbaren Signalanteil sichert dieser Node keine externe Feedbackschleife ab; dafür wird ein Delay-Node als Schleifengrenze benötigt.

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

[Node index](index.md) · [English](../en/delay-allpass.md)
