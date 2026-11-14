# MIDI-Note (MIDI Note)

## Übersicht

Wandelt MIDI-Noten in Frequenz, Gate, Anschlagstärke und Trigger je Stimme um.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MIDI | [MIDI](../../user/de/data-types.md#midi) | — | MIDI-Ereignisse | — | — | [MIDI](../../user/de/rates.md) | Immer |
| Kanal (0 = alle) (Channel (0 = all)) | [Editor](../../user/de/reference-tables.md) | 0–16 | — | 0 | — | — | Immer |
| Stimmen (Voices) | [Editor](../../user/de/reference-tables.md) | 1–32 | — | 1 | — | — | Immer |
| Pitch-Bend (Pitch bend) | [Editor](../../user/de/reference-tables.md) | 0–48 st | — | 2 st | [cent](../../user/de/units.md), [st](../../user/de/units.md), [oct](../../user/de/units.md), [Faktor](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | > 0 Hz | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Gate | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |
| Anschlagstärke (Velocity) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Stimme (Voice) | [Wert](../../user/de/data-types.md#value) | 0–Voices−1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Frequenz = `440 · 2^((note − 69 + transpose + bend)/12)`. Gate ist 1, solange eine Note gehalten wird, einschließlich Haltepedal. Anschlagstärke ist Note-on-Velocity/127. Anschlagstärke und Frequenz halten nach Note-off ihre letzten Werte je Stimme, damit eine Hüllkurve ausklingen kann. Nur Gate fällt auf 0. Losgelassene Stimmen halten ihre Frequenz auch bei Pitchwheel-Bewegungen; aktive Stimmen folgen weiterhin Pitchbend. Eine neue Note ersetzt die gehaltenen Werte; Panic oder ein Quellen-Reset setzt Velocity auf 0. Jedes Note-on erzeugt einen Trigger. Eine Stimme bevorzugt die zuletzt gespielte Note und fällt auf gehaltene Noten zurück; bei Polyphonie entstehen getrennte Stimmen, bei Überbelegung wird die älteste gehaltene Stimme ersetzt. [Voice Merge](voice-merge.md) fasst sie vor gemeinsamen Effekten zusammen; ein Audio-Ausgang mischt Stimmen ebenfalls.

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

[Node-Verzeichnis](index.md) · [English](../en/midi-note.md)

Voice ist ein stabiler, nullbasierter Index: 0 bei Mono, 0 bis Voices−1 bei Polyphonie, auch nach Note-off. Der Pitchbend-Bereich bleibt standardmäßig bei 2 Halbtönen: Eine MIDI-Pitchwheel-Nachricht bewirkt bis zu ±2 Halbtöne. Der Bereichsregler allein verstimmt keine Note; 0 deaktiviert Pitchbend.
