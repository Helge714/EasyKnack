# Binzufall (Bin Random)

## Übersicht

Liefert deterministische Zufallszahlen je Bin und Frame.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Startwert (Seed) | [Wert](../../user/de/data-types.md#value) | 0–2 147 483 647, ganzzahlig | 0–2 147 483 647 | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Je Bin variieren (Vary by bin) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |
| Je Frame variieren (Vary by frame) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Zufall (Random) | [Wert](../../user/de/data-types.md#value) | 0–1, ohne 1 | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Gleichverteilte Werte hängen von Seed und den aktivierten Bin-/Frameschlüsseln ab. Nur Bin liefert einen festen Zufallswert je Frequenz. Nur Frame liefert einen gemeinsamen Zufallswert pro Fenster. Beide variieren pro Bin und Fenster; keiner liefert einen konstanten Zufallswert. Gleiche Schlüssel ergeben auch kanalübergreifend gleiche Werte. Frame meint ein FFT-Fenster, keinen Audioblock. Nur in [Spectral Groups](spectral-group.md) gültig.

Ein verbundener Seed wird je ausgewertetem Bin abgetastet, abgerundet und auf 0–2 147 483 647 begrenzt. Nicht endliche Werte verwenden den manuellen Seed. Ohne Verbindung bleibt der ganzzahlige Editorwert exakt erhalten. Ein Kontextindex oder Gruppeninput kann den Seed je Instanz verändern.

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

[Node-Verzeichnis](index.md) · [English](../en/bin-random.md)
