# Instanzgruppe (Instance Group)

## Übersicht

Führt einen internen Graphen für mehrere unabhängige Instanzen aus.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | 0–256, ganzzahlig | 0–Maximum, abgerundet | 8 | — | [Control](../../user/de/rates.md#control) | Immer |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 220 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Maximum (Maximum) | [Wert](../../user/de/data-types.md#value) | 1–256 | — | 16 | — | — | Inspektor |
| Allgemein / IO / Editoren (General / IO / Editors) | [Editor](../../user/de/reference-tables.md) | Gruppendefiniert | — | — | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Ausgang (Out) | [Audio](../../user/de/data-types.md#audio) | ±∞ | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Zustandslose Formula-Groups zur Berechnung von Instanzgewichten erhalten die optimierte Oszillatorsummierung. Beim Bake werden unveränderte Berechnungen zwischengespeichert; zeit-, trigger- oder bufferabhängige Formeln verwenden weiterhin die allgemeine Ausführung.

Count kann über einen kopierten Slider-Editor an einer umschließenden normalen Gruppe bereitgestellt werden. Der äußere Port bleibt ein gewöhnlicher, bearbeitbarer Gruppeneingang; die feste Count-Sonderrolle gehört zur Instance Group selbst.

Der Anfangsgraph erzeugt harmonische Oszillatoren und einen [Instance Merge](instance-merge.md); Frequency ist sein anfänglicher öffentlicher Parameter, kein dauerhaft vorgeschriebener Port. Jede Instanz besitzt eigenen DSP-Zustand. [Instance Context](instance-context.md) liefert Index, Count, Position und Seed. Maximum reserviert Zustand/Speicher; Count wählt die aktive Anzahl und ist kein gewöhnlicher Group-IO-Port. Mehrere Merge-Zweige und gemeinsame Verarbeitung danach sind möglich. Verschachtelte Instanz-/Spektralgruppen werden unterstützt. Öffentliche Eingänge erlauben Zahlen, Buffer, Vector, Wavetable und Spectrum; Ausgänge derzeit Zahlen oder Spectrum. MIDI und andere Ressourcengrenzen werden nicht unterstützt. Beide Buffer Reader werden unterstützt. Externe Buffer werden gemeinsam gelesen; jede Instanz behält ihren eigenen Wiedergabezustand. Interne Live-Buffer besitzen pro Instanz eigenen Speicher. Gleiche Generatoren können Arbeit teilen, solange Kontext und Verlauf gleich bleiben.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Gewählter Gruppeneingang (Configured group input) | Gewählter Gruppenausgang (Configured group output) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Gewählter Gruppeneingang (Configured group input) | Gewählter Gruppenausgang (Configured group output) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/instance-group.md)
