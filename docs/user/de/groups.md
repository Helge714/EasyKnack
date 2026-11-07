# Gruppen im Dokument verwalten

Öffne **Einstellungen → Dokumenteneinstellungen → Gruppen verwalten**.

Links stehen alle eingebetteten Gruppendefinitionen mit der Anzahl ihrer direkten
Instanzen. Rechts erscheinen die enthaltenden Graphen und die einzelnen Instanzen
der ausgewählten Gruppe. Gezählt werden auch ausgeblendete Nodes und Instanzen
in unbenutzten Gruppendefinitionen. Ein Instance-Group-Node zählt als eine gesetzte
Instanz; seine aktive Stimmenanzahl vervielfacht die angezeigte Anzahl nicht.

Wähle eine unbenutzte Gruppe und **Unbenutzte Gruppe löschen**. Das entfernt ihre
Definition aus diesem Dokument und dem Add-Menü. Es wird keine Datei auf dem
Datenträger gelöscht. Undo stellt Definition und Menüeintrag wieder her; Redo
entfernt sie erneut.

Eine Gruppe lässt sich nicht löschen, solange sie irgendeine Instanz besitzt,
auch innerhalb einer unbenutzten übergeordneten Gruppe, oder zum aktuell geöffneten
Gruppen-Navigationspfad gehört. Entferne zuerst ihre Instanzen oder unbenutzte
übergeordnete Definitionen und verlasse die Gruppe vor dem Löschen. Beim Löschen
einer zuvor besuchten Gruppe wird die alte Vorwärts-Navigation entfernt, damit
sie darüber nicht wieder auftaucht.

Diese ausdrücklichen Löschungen wirken sofort und lassen sich nach dem Schließen
des Dialogs rückgängig machen. Die separate Dokumentenoption zum Erhalten
unbenutzter Gruppen steuert das Speichern; sie umgeht die Löschprüfung nicht.

Mit **Gruppen importieren** lassen sich mehrere Definitionen aus einem anderen Dokument auswählen. Gemeinsame Abhängigkeiten werden einmal kopiert; die Instanzen werden gemeinsam eingefügt und lassen sich gemeinsam rückgängig machen.

Undo/Redo wirkt auf den gerade geöffneten Graphen. Gruppenverläufe bleiben beim Verlassen und erneuten Öffnen einer Definition erhalten. Speichern gilt für alle Graphen. **N** schaltet den Inspektor außerhalb von Texteingaben um. **Esc** schließt den fokussierten Inspektor; eine aktive Textbearbeitung oder ein Popup behandelt zunächst das erste Esc.

[English](../en/groups.md) · [Gruppenreferenz](../../nodes/de/node-group.md)
