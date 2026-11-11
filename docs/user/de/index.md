# Grundlagen

| Thema | Inhalt |
| --- | --- |
| [Gruppen verwalten](groups.md) | Verwendungsanzahl, Einbindungsorte und rückgängig machbares Löschen unbenutzter Definitionen |
| [Raten](rates.md) | Audio, Control, Auto und Trigger; Abtastung und Ratenwechsel |
| [Datentypen](data-types.md) | Zahlen, Trigger, MIDI, Vektoren und Ressourcen; Kanäle |
| [Einheiten](units.md) | Darstellung, Interpretation verbundener Werte und Umrechnung |
| [Mute](muting.md) | Durchreichen, ignorierte Verbindungen und Nullsignale |
| [Referenztabellen](reference-tables.md) | Reglerbereich, Signalbereich und weitere Tabellenüberschriften |

[English](../en/index.md)

## Experimentelle Funktionen

Über **Dokumenteinstellungen → Experimentelle Funktionen aktivieren** werden als **Experimentell** markierte Nodes und Beispiele angeboten. Die Option gilt für das jeweilige Dokument und ist bei neuen gewöhnlichen Dokumenten aus. Alte Dateien aktivieren sie automatisch, wenn sie experimentelle Nodes enthalten; ein ausdrücklich gespeichertes Aus bleibt erhalten. Beim Ausschalten wird eine Bestätigung verlangt, solange experimentelle Nodes im Dokument vorhanden sind. Gruppen und neu exportierte Plugins übernehmen den Status ihrer Abhängigkeiten. Vorhandene Dokumente bleiben auch bei ausgeschalteter Option ladbar und behalten ihre Nodes. Kamerabewegungen oder Fenstergrößenänderungen allein lösen keine Speicherabfrage aus; beim Speichern wird die aktuelle Kamera weiterhin mitgesichert. Derzeit benötigen Pitch Hold und davon abhängige Beispiele diese Freigabe.

## Dokumentationssprache

In der Dokuansicht lässt sich unter **Dokumentationssprache** zwischen **Wie Anwendung**, **Deutsch** und **English** wählen. Die Auswahl wird unabhängig von der Programmsprache gespeichert und für Node-Hilfe, Verzeichnis und lokale Navigation verwendet. Fehlende Übersetzungen werden auf Englisch angezeigt, ohne die Auswahl zu ändern; die Statuszeile kennzeichnet nicht übersetzte Workflow-Anleitungen.
