# Triggerzähler (Trigger Counter)

## Übersicht

Zählt Eingangstrigger und gibt deren nullbasierten Index aus.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | Button | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Wrap | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | 1–16 777 216, ganzzahlig | 1–16 777 216, gerundet | 16 | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Index | [Wert](../../user/de/data-types.md#value) | 0–16 777 215 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Trigger | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Der erste Trigger liefert Index 0; folgende erhöhen ihn. Zwischen Ereignissen bleibt der Index erhalten. Wrap durchläuft 0…Count−1; ohne Wrap sättigt der Zähler bei 16 777 215. Reset bereitet Index 0 vor; ein gleichzeitiger Trigger zählt weiterhin als erstes Ereignis. Der Trigger-Ausgang reicht die gezählten Ereignisse weiter.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Trigger | Trigger |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Trigger | Trigger |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/trigger-counter.md)
