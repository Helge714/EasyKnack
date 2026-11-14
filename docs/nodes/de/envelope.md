# Hüllkurve (Envelope)

## Übersicht

Erzeugt eine kurvenförmige Hüllkurve aus Gate und Trigger.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Hüllkurve (Envelope) | [Editor](../../user/de/reference-tables.md) | Kurveneditor | — | ADSR | — | — | Immer |
| Gate | [Wert](../../user/de/data-types.md#value) | 0–1 | ±∞ | 0 | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Anstieg (Attack) | [Wert](../../user/de/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 120 ms | [ms](../../user/de/units.md), [s](../../user/de/units.md), [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Abfall (Decay) | [Wert](../../user/de/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 180 ms | [ms](../../user/de/units.md), [s](../../user/de/units.md), [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Ausklingen (Release) | [Wert](../../user/de/data-types.md#value) | 0–60 000 ms | 0–60 000 ms | 300 ms | [ms](../../user/de/units.md), [s](../../user/de/units.md), [smp](../../user/de/units.md) | [Auto](../../user/de/rates.md#auto) | Immer |
| Halten (Hold) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Hüllkurve (Envelope) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Eine steigende Gate-Flanke über 0 oder ein Trigger startet Attack und danach Decay. Jede Phase hat einen eigenen Dauereingang (0–60.000 ms). Bei eingeschaltetem Hold und positivem Gate wird der Pegel am Ende von Decay gehalten. Gate 0 startet Release vom aktuellen Pegel, auch während Attack oder Decay. Bei ausgeschaltetem Hold oder einem Trigger ohne Gate folgt Release direkt auf Decay. Phasen mit Dauer 0 enden sofort. Retrigger starten beim aktuellen Pegel; der Ausgang bleibt auf 0–1 begrenzt.

Kurvenpunkte lassen sich vertikal bewegen; ihre horizontalen Positionen und ihre Anzahl bleiben fest. Der erste innere Punkt beendet Attack, der letzte innere Punkt beendet Decay. Bézier-Griffe formen weiterhin die Übergänge. Punkte und Griffe erscheinen bei Hover oder aktivem Editor und verschwinden nach einem Fokuswechsel nach außen. Vorhandene Kurven werden phasenweise an die Darstellung angepasst.

Die bisherige kombinierte Zeit wird am ersten inneren Kurvenpunkt aufgeteilt. Bestehende Verbindungen des Dauereingangs steuern jetzt Attack. Die Halteposition entfällt samt ihren Verbindungen und kopierten Bindungen; Hold verwendet das Ende von Decay. Alte Patches mit modulierter Zeit oder verschobener Halteposition können dadurch anders klingen.

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

[Node-Verzeichnis](index.md) · [English](../en/envelope.md)

Die Punktform zeigt den folgenden Abschnitt: Kreis = Bézier, Quadrat = Linear, Raute = Step. Der letzte Punkt verwendet den vorherigen Abschnitt.

Attack, Decay und Release belegen je ein Drittel des randbündigen Graphen. Die tatsächlichen Dauern bestimmen die drei Zeiteingänge. Eine zusätzliche Hold-Markierung gibt es nicht.
