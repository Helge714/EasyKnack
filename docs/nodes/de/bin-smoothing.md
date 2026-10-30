# Binglättung (Bin Smoothing)

## Übersicht

Mischt neue Binwerte in einen Speicher je Bin.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Aktualisierung (Update) | [Wert](../../user/de/data-types.md#value) | 0–1 | 0–1 | 1 | — | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wert (Value) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Jeder FFT-Frame berechnet `held = (1−u) × held + u × value`. Der erste Frame initialisiert direkt. Update 0 hält und 1 ersetzt. Kleinere Hops aktualisieren häufiger; derselbe Faktor besitzt daher bei anderer Hopgröße ein anderes Zeitverhalten. Audioeingänge werden am Fensterende abgetastet. Nur in [Spectral Groups](spectral-group.md) gültig.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Wert (Value) | Wert (Value) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Wert (Value) | Wert (Value) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/bin-smoothing.md)
