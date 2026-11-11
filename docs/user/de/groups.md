# Gruppen und Gruppeneinstellungen

Markiere Nodes und erstelle eine Gruppe. Öffne sie zum Bearbeiten; Input und Output verbinden ihren inneren Graphen mit der öffentlichen Schnittstelle. Eine Definition kann mehrfach verwendet werden: Änderungen am Aufbau gelten für ihre Instanzen, deren Parameterwerte bleiben individuell.

## Einstellungen

| Bereich | Bedeutung |
| --- | --- |
| Allgemein | Name, Node-Typ/Titelfarbe und Add-Menükategorie bestimmen Darstellung und Auffindbarkeit. |
| Einfügen | Wähle Eingang und Ausgang für das Einsetzen in eine vorhandene Verbindung. |
| Mute-Bypass | Wähle den Eingang und Ausgang, die bei Mute verbunden werden sollen. |
| IO | Eingänge/Ausgänge anlegen, benennen, sortieren, verbinden und entfernen; Datentyp, Rate und Einheiten passend zum inneren Ziel wählen. |
| Sichtbarkeit | Ein öffentlicher Eingang kann nur einen Editor zeigen; sein innerer Port und seine Verdrahtung bleiben bestehen. |
| Editoren | Kein Editor, Slider, Checkbox, Button, Combobox oder Copy Editor. Checkbox liefert 0/1; Button löst Trigger aus. |
| Slider | Minimum/Maximum, Standardwert, Ganzzahligkeit, Balken-/Buttondarstellung, erlaubte Einheiten und Standardeinheit. Leere Grenzen erlauben unbeschränkte Werte. |
| Combobox | Dokumentvorlage mit Bezeichnungen und Zahlenwerten sowie Standardauswahl. Vorlagen werden im Editoren-Bereich verwaltet. |
| Spezialgruppen | Instance: Maximum und Mix; Spectral: leere Bins überspringen und Parallelverarbeitung; Formula: Verarbeitungsrate und Code-Bezeichner. Details stehen im jeweiligen Kapitel. |

Ziehen einer Verbindung auf einen Grenz-Node legt schnell einen Port an. Prüfe danach Typ, Rate, Editor und Routing. Die erlaubten Einheiten müssen die Standardeinheit enthalten. Ein verbundener Eingang hat Vorrang vor seinem manuellen Wert.

## Copy Editor

Copy Editor übernimmt einen passenden inneren Editor mit Typ, Grenzen und Einheiten. Auch öffentliche Editoren verschachtelter Gruppen können Quellen sein. Bei unterstützten Editoren ohne eigenen Port wählst du das zu steuernde Ziel ausdrücklich aus. Gekoppelte Originale erscheinen deaktiviert; ein Ziel kann nicht gleichzeitig von mehreren öffentlichen Editoren gesteuert werden. Nicht jeder Spezialeditor ist kopierbar. Änderungen an der Schnittstelle können unbrauchbare Verknüpfungen entfernen; prüfe danach deine Verdrahtung.

## Verwalten und Weitergeben

Unter **Dokumenteneinstellungen → Gruppen verwalten** siehst du Definitionen, direkte Verwendungen und ihre Orte. Nur unbenutzte, nicht gerade geöffnete Definitionen lassen sich löschen; Undo stellt sie wieder her. Count einer Instance Group vervielfacht diese Verwendungszahl nicht. Die Option zum Erhalten unbenutzter Gruppen betrifft das Speichern.

Import kopiert ausgewählte Definitionen samt Abhängigkeiten und Editorvorlagen, ohne Live-Verknüpfung zur Quelldatei. **Ungroup** entpackt eine gewöhnliche Instanz; Spezialgruppen behalten ihren Ausführungskontext und lassen sich nicht entsprechend entpacken.

**Bake** kompiliert lokal und lässt den Graphen bearbeitbar. Interne Rechenänderungen können Neukompilation erfordern; Bake garantiert keinen Geschwindigkeitsgewinn. Der lokale Binärcache ist nicht im Dokument. Gruppenexport erzeugt ein Source-Plugin; Installation kompiliert es. Export gekoppelter standalone Slider, Checkboxen und Trigger ist eingeschränkt. Prüfe die Oberfläche vor Weitergabe; externe Medien bleiben externe Dateien.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/groups.md)

<a id="aktuellen-graphen-anordnen"></a>
<a id="gruppen-im-dokument-verwalten"></a>
