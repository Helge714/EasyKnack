# Formelgruppe (Formula Group)

## Übersicht

Berechnet eigene Formeln mit konfigurierbarer Gruppenschnittstelle.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| signal | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Quelltext (Source) | [Editor](../../user/de/reference-tables.md) | Formeleditor | — | result = signal; | — | — | Inspektor |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Live-Vorschau (Live preview) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Inspektor |
| Gruppenschnittstelle (Group IO) | [Editor](../../user/de/reference-tables.md) | Zahlen-, Trigger- und Bufferports | — | signal → result | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| result | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Group-IO legt Portbeschriftungen, Einheiten und stabile Codebezeichner fest. Die Anfangsschnittstelle ist signal → result. Für einen Buffer-Eingang **Buffer-Ressource** wählen; Ressourcen sind nur als Eingänge unterstützt.

Audio wertet pro Sample aus, Control pro Block; Auto wählt die zulässige Rate anhand der verwendeten Eingänge. Verwendete Audio-/Trigger-Eingänge dürfen durch eine unpassende Control-Einstellung nicht verloren gehen. In Spectral Groups laufen Formeln pro Bin/Frame.

Die [Formula-Sprachreferenz](../../user/de/formula-language.md) erklärt Syntax, state/final/const, Datentypen, Kontext, Buffer-Zugriffe, Funktionen und Schleifen. Der [Arbeitsablauf](../../user/de/formula-group.md) zeigt eine erste Formel.

### Editor

Vervollständigung erscheint beim Tippen von Bezeichnern oder mit Strg+Leertaste. Hover, Signaturhilfe und eingeblendete Argumentnamen erklären Funktionen. Strg+Linksklick springt zur eigenen Deklaration oder öffnet die eingebaute Funktion in der Sprachreferenz, in der unabhängig eingestellten Dokusprache. Enter und Format rücken Code ein; Aufrufe verwenden `name(...)`, Definitionen `name(...) {`.

Der Status unterscheidet Übernommen, Noch nicht übernommen und Ungültiger Entwurf vom Stand der nativen Optimierung. Live-Vorschau übernimmt gültige Änderungen nach einer kurzen Pause; sonst Apply verwenden. Ungültige Entwürfe lassen das letzte übernommene Programm weiterlaufen und zeigen ein kontrastierendes ! im Formula-Nodetitel; Hover zeigt den Fehler. Läuft die Editorvorbereitung noch im Hintergrund, erscheint eine Ladeanzeige.

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
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/formula-group.md)
