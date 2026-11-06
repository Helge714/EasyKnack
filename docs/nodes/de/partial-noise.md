# Teiltonrauschen

## Übersicht

Liefert reproduzierbare Zufallswerte pro erkanntem Teilton und Analyseframe.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Seed | [Editor](../../user/de/reference-tables.md) | 0–2 147 483 647, ganzzahlig | — | 0 | — | — | Immer |
| Pro Teilton variieren (Vary by partial) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |
| Pro Frame variieren (Vary by frame) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Zufallswert (Random) | [Wert](../../user/de/data-types.md#value) | 0–1, ohne 1 | [Audio (Teilton/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Nur innerhalb einer Partial Group. Der Zufallswert ist gleichverteilt in [0, 1) und wird aus Seed, stabiler Teilton-ID und FFT-Frame-Position gebildet. Pro Teilton variieren trennt die Teiltöne; deaktiviert erhalten alle Teiltöne desselben Frames denselben Wert. Pro Frame variieren erzeugt neue Werte für jedes FFT-Fenster; deaktiviert bleibt der Wert über die Lebensdauer eines Teiltons konstant. Sind beide Optionen deaktiviert, hängt der Wert nur vom Seed ab. Lautstärkesortierung, Verarbeitungsreihenfolge und Kanal verändern die Zuordnung nicht. Für symmetrische Oktavmodulation `f_neu = f * pow(2, strength * (2 * random - 1))` verwenden. Der Seed-Ausgang von Partial Context wird hierfür nicht benötigt.

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

[Node-Verzeichnis](index.md) · [English](../en/partial-noise.md)

[Partial Group](partial-group.md) · [Partial Context](partial-context.md)
