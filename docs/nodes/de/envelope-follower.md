# Hüllkurvenfolger (Envelope Follower)

## Übersicht

Ermittelt den sampleweisen Amplitudenverlauf eines Audiosignals.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audioeingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Detektor (Detector) | [Editor](../../user/de/reference-tables.md) | Spitzenwert / RMS | — | Spitzenwert | — | — | Immer |
| Anstiegszeit (Attack) | [Value](../../user/de/data-types.md#value) | 0–2000 ms | 0–2000 ms | 5 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Abklingzeit (Release) | [Value](../../user/de/data-types.md#value) | 0–10000 ms | 0–10000 ms | 100 ms | [ms](../../user/de/units.md) / [s](../../user/de/units.md) / [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Hüllkurve (Envelope) | [Value](../../user/de/data-types.md#value) | 0–∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Peak folgt dem Betrag des Eingangs. RMS glättet das quadrierte Signal und zieht anschließend die Quadratwurzel. Anstiegszeit gilt bei steigenden, Abklingzeit bei fallenden Pegeln. Der interne Detektor folgt `e = a × e + (1 − a) × Zielwert` mit `a = exp(−1 / (Zeit × fs))`. Zeit steht hier in Sekunden; null bedeutet sofortige Reaktion. Eine Zeitkonstante erreicht etwa 63% eines Peak-Pegelsprungs beziehungsweise 63% des quadrierten RMS-Zielwerts. Dies ist eine exponentielle Nachführung, kein endliches RMS-Messfenster. Die Kanäle werden getrennt ausgewertet.

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

[Node index](index.md) · [English](../en/envelope-follower.md)
