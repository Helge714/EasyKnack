> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Dokumente, Beispiele und Darstellung

- **Datei → Neu** (`Strg+N`) erzeugt einen Oszillator, der mit einem Audio-Ausgang verbunden ist. Ungespeicherte Änderungen werden zuvor abgefragt.
- **Datei → Beispiele** liest `.knack`-Dateien aus `%USERPROFILE%/Documents/EasyKnack/Beispiele`. Unterordner werden Untermenüs. Beim ersten Start werden die mitgelieferten Beispiele dort angelegt; vorhandene Dateien werden nicht ersetzt. Eigene Beispiele können über „Aktualisieren“ eingelesen werden. Ein Beispiel wird immer als neues Dokument ohne Speicherpfad geöffnet.
- **Einstellungen** bietet Deutsch/Englisch und eine Combobox mit allen `.gltheme`-Dateien aus den Anwendungsressourcen (auch in Unterordnern und im gepackten JAR). Änderungen werden beim Speichern sofort übernommen. Die erste Sprachwahl folgt der Systemsprache (Deutsch, ansonsten Englisch). Eigene Beschriftungen, Dateinamen und Plugin-Namen werden nicht übersetzt. Externe Compiler- und Betriebssystemdiagnosen behalten ihre ursprüngliche Sprache.
- **Datei → Graph als Bild exportieren** exportiert PNG oder JPG: den gesamten aktuell geöffneten Graphen, den sichtbaren Ausschnitt oder die ausgewählten Nodes. Links zeigt eine verkleinerte Vorschau den tatsächlichen Ausschnitt samt Rand; rechts stehen die Einstellungen. Breite und Höhe lassen sich unabhängig ändern oder über „Proportional“ verknüpfen. Zum gemeinsamen Eingeben im Zahlenfeld senkrecht über beide Zeilen ziehen, loslassen und tippen. Enter übernimmt beide Werte, Escape verwirft sie. Die gemeinsame Eingabe legt ein neues Seitenverhältnis fest; die Proportional-Verknüpfung bleibt für spätere einzelne Änderungen aktiv. Bei abweichendem Seitenverhältnis wird der Graph unverzerrt eingepasst und zentriert. Padding kann innerhalb der Maße liegen oder zu ihnen addiert werden. Neben der geschätzten Dateigröße stehen die endgültigen Pixelmaße. Die feste 32-Megapixel-Grenze entfällt; tatsächliche GPU-Grenzen und die technischen Grenzen der Java-Bildpuffer werden geprüft. Rechtsklick → Zurücksetzen setzt beide Bildmaße gemeinsam zurück, beim Padding nur den Rand. „Ausgewählte Nodes“ erscheint nur bei vorhandener Auswahl. JPG bietet einen Qualitätswert von 1–100 %; nur PNG zeigt die Transparenzoption. Die Dateigrößenschätzung komprimiert eine Vorschau und rechnet bei großen Bildern hoch; deshalb kann die tatsächliche Größe abweichen. Der Live-Graph wird dabei nicht verändert.
- **Datei → Gruppen importieren** öffnet eine andere `.knack`-Datei. Eine enthaltene Gruppe oder deren gesamter Hauptgraph kann als neue Gruppe übernommen werden. Benötigte Untergruppen und Editorvorlagen werden eingebettet; neue IDs verhindern Konflikte mit bestehenden Gruppen. Eigene Beschriftungen und das interne Layout bleiben erhalten. Importierte Gruppen können unabhängig weiterbearbeitet werden. Vorhandene Bakes werden nicht übernommen und können neu erstellt werden. In Spectral-Gruppen ist dieser allgemeine Import nicht verfügbar.

## Tastenkürzel

Vorhandene Editorbefehle zeigen ihre tatsächlichen Core-Tastenkürzel in den Menüs. Dazu gehören Kopieren, Ausschneiden, Einfügen, Duplizieren, Löschen, Herauslösen, Ausblenden, Mute, Gruppieren, Umbenennen und Anordnen.

| Aktion | Tastenkürzel |
|---|---|
| Neu / Öffnen | Strg+N / Strg+O |
| Speichern / Speichern unter | Strg+S / Strg+Umschalt+S |
| Vergrößern / Verkleinern / Ansicht zurücksetzen | Strg++ / Strg+- / Strg+0 |
| Navigation zurück / vorwärts | Alt+Links / Alt+Rechts |
| Inspector umschalten | N |
| Nodes anordnen | Strg+Umschalt+L |

Eingaben, die ein Texteditor verarbeitet, lösen keine zusätzlichen Workspace-Aktionen aus. „Spectral-Gruppe hinzufügen“ befindet sich weiterhin im Hinzufügen-Popup, nicht mehr zusätzlich im oberen Menü.

## Sprachdateien

`src/main/resources/application.properties` und `application_de.properties` enthalten gemeinsame Oberflächentexte; `nodes.properties` und `nodes_de.properties` enthalten benannte Node-Texte. `AppText.text` verwendet benannte Schlüssel. `AppText.literal` verwendet einen stabilen SHA-256-Schlüssel für gemeinsame bestehende Beschriftungen; interne Port-IDs und Protokollwerte bleiben sprachunabhängig.

Für Entwicklung und Tests können `easyknack.language`, `easyknack.theme` und `easyknack.examples` als Java-Systemproperties die gespeicherten Einstellungen übersteuern.
