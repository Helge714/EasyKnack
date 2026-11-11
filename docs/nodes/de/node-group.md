# Nodegruppe (Node Group)

## Übersicht

Stellt eine wiederverwendbare Schnittstelle zu einem internen Graphen bereit.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Öffentliche Eingänge (Public inputs) | [Schnittstellenabhängig](../../user/de/data-types.md) | Schnittstellenabhängig | Schnittstellenabhängig | Schnittstellenabhängig | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Allgemein (General) | [Editor](../../user/de/reference-tables.md) | Name, Kategorie, Typ, Routing, Bake | — | Gruppendefiniert | — | — | Inspektor |
| IO | [Editor](../../user/de/reference-tables.md) | Ports, Editoren, Einheiten, Standardwerte, Raten | — | Gruppendefiniert | — | — | Inspektor |
| Editoren (Editors) | [Editor](../../user/de/reference-tables.md) | Editorvorlagen des Dokuments | — | — | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Öffentliche Ausgänge (Public outputs) | [Schnittstellenabhängig](../../user/de/data-types.md) | Schnittstellenabhängig | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Bei einem unverbundenen Gruppeneingang zuerst Editor übernehmen und anschließend die Quelle ausdrücklich auswählen. Bis zur Auswahl wird keine interne Verbindung oder Editorkopplung angelegt.

Öffne die Gruppe zur Bearbeitung von Graph und Schnittstelle. Jeder öffentliche Port behält seinen gewählten Datentyp und seine Rate; die Tabelle bezeichnet konfigurierbare Ports und keine fest numerische Schnittstelle. Group-General legt Titelgestaltung sowie Mute- und Einfügepaare fest; Group-IO Portreihenfolge, Editoren und Bereiche. Übernommene Editoren erben ihre Darstellung und können kompatible interne Parameter steuern. Einheitenadapter erhalten die internen Einheiten an der Grenze. Bake optimiert die übernommene innere Definition; öffentliche Parameteränderungen bleiben Parameter. [Instance Groups](instance-group.md), [Spectral Groups](spectral-group.md) und [Formula Groups](formula-group.md) besitzen eigene Ausführungsregeln.

Nach Änderungen werden ungültige Verbindungen zu gelöschten Nodes sowie entfernten oder inkompatiblen Ports und Editor-Eigenschaften automatisch entfernt. Gültige übrige Ziele und öffentliche Bedienelemente bleiben erhalten. Beim Löschen oder Gruppieren stellt Undo die ursprünglichen Nodes und Verknüpfungen wieder her.

Eigenständige Editoren verschachtelter Gruppen stehen als Quellen für Copy Editor zur Verfügung. Eingänge mit sichtbarem Anschluss werden durch Kabel verbunden und fehlen in dieser Liste. Copy Editor erscheint nur bei einem kompatiblen verbundenen Editor oder einer eigenständigen Quelle; die Auswahl legt keine automatische Kabelverbindung an.

Die gesteuerten Gruppeneditoren sind innerhalb der übergeordneten Gruppe ausgegraut. Nach dem Entfernen der Verknüpfung sind sie wieder bedienbar.

Änderungen an wiederverwendbaren Combobox-Einträgen aktualisieren vorhandene Gruppenbedienelemente unmittelbar. Undo und Redo stellen Vorlage und verwendete Editoren gemeinsam wieder her. Gruppen tragen im Add-Menü das Gruppensymbol.

Undo und Redo gelten für den gerade geöffneten Graphen. Jede Gruppendefinition behält beim erneuten Öffnen ihren eigenen Verlauf; Speichern setzt den Speicherstand für das ganze Dokument.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Gewählter Durchreicheingang (Configured bypass input) | Gewählter Durchreicheausgang (Configured bypass output) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Gewählter Einfügeeingang (Configured insertion input) | Gewählter Einfügeausgang (Configured insertion output) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Vom inneren Graphen abhängig |

[Node-Verzeichnis](index.md) · [English](../en/node-group.md)
