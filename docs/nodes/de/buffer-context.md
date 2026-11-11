# Buffer-Kontext (Buffer Context)

## Übersicht

Liest Größe und Schreibposition einer Buffer-Ressource aus.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Buffer | [Buffer-Ressource](../../user/de/data-types.md#buffer) | — | Buffer-Ressource | — | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Größe (Size) | [Wert](../../user/de/data-types.md#value) | 0–+∞ smp | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Schreibposition (Write position) | [Wert](../../user/de/data-types.md#value) | 0–1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Größe ist die allokierte Sampleanzahl pro Kanal, einschließlich noch unbeschriebener Aufnahmekapazität. Bei Dateibuffern entspricht sie dem geladenen Start-/Endausschnitt. Standardeinheit ist smp; Sekunden und Millisekunden stehen im Einheitenmenü des Ausgangs bereit.

Schreibposition ist die normierte nächste Schreibstelle. Aufnahmebuffer laufen sampleweise vorwärts und springen am Ende auf 0; die Referenz entspricht den Buffer-Readern. Dateibuffer liefern 1 (Dateiende). Leere oder unverbundene Ressourcen liefern an beiden Ausgängen 0.

Beim Laden werden bisherige Schreibpositionsverbindungen von Aufnahmebuffern automatisch auf Buffer-Kontext umgestellt, auch bei veröffentlichten Gruppenausgängen. Ein Hinweis informiert darüber; Speichern erhält die neue Verdrahtung.

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

[Node-Verzeichnis](index.md) · [English](../en/buffer-context.md)
