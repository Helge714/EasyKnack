# Freeverb

## Übersicht

Erzeugt einen Stereo-Nachhall mit einstellbarer Raumgröße, Dämpfung und Breite.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Raumgröße (Room Size) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Dämpfung (Damping) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Breite (Width) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 1 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Hallanteil (Wet) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 1/3 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Direktanteil (Dry) | [Value](../../user/de/data-types.md#value) | 0–1 | 0–1 | 0.5 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Stereo](../../user/de/data-types.md#channels) |

## Verhalten

Acht parallele, gedämpfte Kammfilter und vier serielle Allpässe pro Kanal erzeugen den Nachhall. Die Verzögerungszeiten skalieren mit der Samplerate. Beide Kanäle erhalten als Anregung `0.015 × (L + R)`.

Raumgröße bestimmt die Rückkopplung `0.7 + 0.28 × Room Size`, Dämpfung den Tiefpassfaktor `0.4 × Damping`. Breite 0 ergibt einen monophonen Hallanteil, Breite 1 getrennte Stereokanäle. Das Direktsignal bleibt kanalgetrennt.

Der Hallpegel beträgt `3 × Wet`, der Direktpegel `2 × Dry`; Dry = 0.5 lässt das Direktsignal unverstärkt passieren. Verbundene Parameter werden auf 0–1 begrenzt und entsprechend ihrer Rate ausgewertet.

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

[Node index](index.md) · [English](../en/freeverb.md)
