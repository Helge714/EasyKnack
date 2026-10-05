# Historical README (archived October 2026)

This is an record of earlier development notes, not current user
instructions. Original prose is retained and relative links are adjusted. Some names, rates,
menus and limitations are obsolete. Use the
[English user guide](../user/README.md) for current workflows.

---

# EasyKnack

Eigenständige nodebasierte Audioanwendung mit einer GLSwing-GLCanvas-Oberfläche.
Der Model-Graph steuert einen separaten C++-Audiohost. Oszillator, Verstärkung und
Ausgang sowie Math-Nodes berechnen Samples direkt in Float-Buffern. Node-Dokumente
lassen sich im versionierten GLSwing-Format `.knack` speichern und öffnen.

## Voraussetzungen und Start

- JDK 17 oder neuer, Maven 3.9 und ein OpenGL-fähiger Desktop.
- `com.vingame:glswing:0.1.0` muss als fertiges Maven-Artefakt einschließlich
  zugehöriger POMs und transitiver Abhängigkeiten im lokalen Repository oder in
  einem über Maven konfigurierten Artefakt-Repository verfügbar sein.
  Eine öffentliche Verfügbarkeit wird nicht vorausgesetzt.
- `JAVA_HOME` und `PATH` auf das gewünschte JDK setzen (Java 8 reicht nicht).

```sh
mvn verify
java -jar target/easy-knack-0.0.1-SNAPSHOT.jar
```

Für Audio unter Windows zusätzlich LLVM-MinGW/MinGW mit C++17 installieren und bauen:

```powershell
mvn -Pnative-audio verify
```

Alternativer Compilerpfad: `-Daudio.cxx=C:/path/to/clang++.exe`.
Der fertige Host liegt unter `target/native/easyknack-audio-host.exe`.
Ohne Host bleibt der Nodeeditor nutzbar; Start meldet den fehlenden Host in der Statuszeile.

Zum Weitergeben die JAR **zusammen mit `target/lib/` und `target/native/`** kopieren. GLSwing bleibt
eine separate Bibliothek. Alternativ: `mvn compile exec:java`.
Die nötigen GLCanvas-Modulfreigaben sind im JAR-Manifest, in `.mvn/jvm.config`
und für Eclipse in `EasyKnack.launch` hinterlegt. In Eclipse diese Startkonfiguration
über **Run As → Java Application** ausführen.

## Bedienung

Neue Gruppen-Editoren, FFT/IFFT und Bin-Verarbeitung: [Anleitung](../GROUPS-SPECTRAL.md).

- **Nodes → Node hinzufügen …** öffnet die durchsuchbare Workspace-Palette mit
  Oszillator, Verstärkung, Math, Audioausgang und erstellten Composites.
- **Math**: Operations-Combobox mit Multiply, Add, Subtract, Divide, Minimum, Maximum,
  Absolute und Negate, Sliderports A/B und eine Checkbox für Clamp auf −1 … +1.
  Absolute und Negate entfernen Eingang B samt Kabeln; Undo stellt beides wieder her.
  Das gilt auch für exportierte Anschlüsse von Composites. Ohne Kabel gilt
  der Sliderwert, mit Kabel der jeweilige Samplewert. Nach dem Trennen bleibt der
  gespeicherte Sliderwert erhalten. Zahlen lassen sich ziehen oder direkt eingeben.
  Division durch nahezu null ergibt 0. A/B erlauben Werte von −1000000 bis +1000000.
- **Oszillator**: Frequenz-Sliderport direkt im Node (20–20000 Hz), ebenfalls durch
  ein Signal ersetzbar. Verbundene Frequenzen werden pro Sample auf 0–20000 Hz begrenzt.
- Node-Titel ziehen, um Nodes zu verschieben; Ausgangsport auf Eingangsport ziehen,
  um eine Verbindung herzustellen. Ein Audioeingang nimmt eine Quelle an;
  erneutes Verbinden ersetzt die bisherige Quelle.
- **Bearbeiten**: Kopieren, Ausschneiden, Einfügen, Duplizieren, Löschen und
  Undo/Redo. Menüs und Core-Tastenkürzel verwenden dieselben Workspace-Aktionen.
- **Nodes**: Hide und Mute umschalten; Auswahl als Composite erstellen oder
  ausgewähltes Composite öffnen. Neue Gruppen erhalten eindeutige IDs und Namen
  wie „Audio-Gruppe 1“ und können im Core-Inspector umbenannt werden.
- **Ansicht**: Zoom ändern, Ansicht zurücksetzen sowie zurück/vorwärts navigieren.
  Der Workspace stellt außerdem Breadcrumbs und Composite-Navigation bereit.
- **Datei → Öffnen / Speichern / Speichern unter**: Dokumentdateien mit den Core-Dateidialogen
  auswählen; Tastenkürzel **Strg+O**, **Strg+S**, **Strg+Umschalt+S**.
- **Datei → Beenden**: Fenster und GL-Ressourcen schließen. Ungespeicherte Änderungen
  werden vor dem Schließen oder dem Öffnen eines anderen Dokuments abgefragt.
- **Audio → Start / Stop**: lokalen Audiohost starten und Wiedergabe schalten.
  Der Beispielgraph erzeugt einen 440-Hz-Sinus mit −12 dB Node-Verstärkung.
  **Ansicht → Inspector ein-/ausblenden**, dann Oszillator oder Verstärkung auswählen:
  Frequenz und Pegel sind live editierbar. Es gibt zusätzlich einen festen Masterpegel von 0,2.

## Struktur

- `com.vin.easyknack.EasyKnack`: Start und GLCanvas-Lebenszyklus.
- `nodes.BasicNode`: Basis für zustandsabhängige Node-Definitionen.
- `nodes.MathNode`, `OscillatorNode`, `GainNode`, `OutputNode`: jeweils Darstellung,
  Ports, Parameterdefaults, Mute-Routing und native Prozessorverknüpfung.
- `nodes.NodeRegistry`: überträgt Definitionen in die Model-Verträge und den Katalog
  von GLSwing. Interne Vertragsvarianten ermöglichen unterschiedliche Ports pro Instanz.
- `src/processor`: Annotation Processor; erzeugt beim Maven-Build `GeneratedNodes`.
  Es gibt keine handgepflegte Liste der Node-Klassen.
- `ui.WorkspaceView`: Menüleiste, Kopfzeile, `GLNodeWorkspace` und Statuszeile.
- `ui.WorkspaceController`: `GLNodeGraph`, `GLNodeTypeRegistry`, `GLNodeCatalog`,
  `GLCompositeGraphRegistry`, gemeinsame History und Workspace-Lebenszyklus.
  Der Beispielgraph hat weiterhin die Positionen (70, 140), (390, 200), (710, 140).
- `ui.AudioNodeFactory`: ausschließlich `GLNodeViewFactory` für die Projektion
  registrierter Audio-Model-Nodes. Graph-Bindung, Clipboard, Hide/Mute und
  Composite-Bearbeitung liegen im Core; EasyKnack erstellt keinen nackten Editor.
- `audio`: Root-Graph-Übertragung, Child-Prozess und lokale Steuerverbindung.
- `native/audio-host`: C++-Audiohost und Node-Rechendateien; siehe dortige README
  für Buffer-Vertrag, Protokoll, Build und aktuelle Grenzen.

Es gibt keinen GLSwing-Parent für EasyKnack, keine Maven-Module, Systempfade,
Quellcodekopien oder Eclipse-Projektreferenzen zum GLSwingNext-Repository.
Die Eclipse-Einstellung `resolveWorkspaceProjects=false` verhindert die
Ersetzung des Maven-Artefakts durch ein offenes Workspace-Projekt.
Nach dem Import **Maven → Update Project** ausführen und ein JDK ab 17 auswählen.
Einmal `mvn compile` ausführen, damit der generierte Node-Katalog vorliegt.
Nach dem Hinzufügen oder Entfernen einer Node-Klasse erneut `mvn compile` ausführen
und das Projekt in Eclipse aktualisieren; der generierte Quellordner ist im Classpath enthalten.

## Neue Nodes

### Direkt in Eclipse bauen

Nach Änderungen speichern und die gewünschte `.launch`-Datei über Rechtsklick →
**Run As → Maven build** starten (auch unter **Run → Run Configurations → Maven Build**):

- `EasyKnack Build Nodes.launch`: Java kompilieren, Node-Katalog generieren,
  Tests ausführen und JAR samt Bibliotheken erstellen (`verify`).
- `EasyKnack Build Nodes and Audio.launch`: zusätzlich den C++-Host und die
  nativen Nodes bauen und die Audio-Tests ausführen (`native-audio verify`).
  Diese Konfiguration verwendet den vorhandenen Compiler unter
  `target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe`.
  Bei einem anderen Installationsort unter **Main → Properties** `audio.cxx` anpassen.

Beide Launches verwenden Eclipses Maven und das JDK der Ausführungsumgebung
**JavaSE-17**; dafür muss unter **Preferences → Java → Installed JREs →
Execution Environments** ein passendes JDK zugeordnet sein. Maven im Windows-PATH
ist nicht erforderlich. Das Projekt wird nach dem Build automatisch aktualisiert.
Falls die Dateien noch fehlen, zuerst das Projekt mit **F5** aktualisieren.
Vor dem nativen Build eine laufende EasyKnack-Anwendung beenden, damit die Host-EXE
ersetzt werden kann. Nach **BUILD SUCCESS** die Anwendung über `EasyKnack.launch`
neu starten; neue Nodes erscheinen dann in der Palette.
Kein `clean` voranstellen: Der lokal abgelegte C++-Compiler liegt ebenfalls in `target/`.

Eine neue öffentliche Klasse unter `src/main/java` erbt von `BasicNode` und erhält
`@RegisterNode(id = "easyknack.audio.meinnode", title = "Mein Node", category = NodeCategory.MATH, kind = NodeKind.VALUE)`.
Maven entdeckt sie über den Annotation Processor. Doppelte IDs, ungültige Klassen
und falsch geschriebene Kategorien führen zu Compilerfehlern. Weder Factory noch
Katalog noch ein Node-Enum müssen ergänzt werden.

`BasicNode.createView(context, model)` erzeugt die Ansicht; für individuelle
Darstellung kann die Node-Klasse hier eine eigene `BasicNodeView` zurückgeben.
Deren `control(...)`, `onStateChanged(...)` und `write(...)` verbinden eigene
Editoren mit typisierten Properties. `AudioNodeFactory` delegiert nur noch.
Die Standardbreite kommt aus `node.minimumWidth` im Theme, auch bei langen
Gerätenamen. Explizit gespeicherte oder manuell gesetzte Breiten bleiben erhalten.

Ein einzelner unverbundener Node lässt sich auf eine bestehende Verbindung ziehen.
Die hervorgehobene Verbindung wird beim Loslassen durch den Node geführt.
`BasicNode.insert(InsertionRouting routing, NodeState state)` legt dafür mit
`routing.connect(INPUT_ID, OUTPUT_ID)` genau ein Portpaar fest, unabhängig vom
Mute-Verhalten. Aktiviert für Verstärkung, Math (Eingang A) und Map Range.
`nodeEditor.horizontalNodeSpacing=60` in `easyknack.gltheme` bestimmt den Abstand.
`Strg+Umschalt+L` oder **Nodes → Alle Nodes anordnen** in der Menüleiste ordnet alle
Nodes des geöffneten Graphen in Spalten an, einschließlich Gruppen-Ein-/Ausgängen.
Der vertikale Abstand kommt aus `nodeEditor.verticalNodeSpacing=40`.
Die Anordnung verändert keine Verbindungen und lässt sich als ein Schritt rückgängig machen.
Bei Platzmangel verschiebt der Core den Zielnode und verbundene Nodes rechts davon.
Undo trennt zuerst die neue Verdrahtung und das automatische Platzschaffen; der
Node bleibt an der abgelegten Position. Ein zweites Undo macht das Platzieren
beziehungsweise Erzeugen rückgängig.

`Strg+Entf` löscht ausgewählte Nodes und verbindet ihre Quelle mit allen
typkompatiblen Zielen sämtlicher Ausgänge. `BasicNode.exclusionInput(NodeState)`
kann dafür eine bevorzugte `PropertyId` zurückgeben; standardmäßig ist es der
Einfügeeingang. Fehlt die Vorgabe oder ist sie unverbunden, wird der erste
verbundene Eingang in Definitionsreihenfolge verwendet. Ohne Quelle oder passende
Zieltypen wird einfach gelöscht. Portlimits und Zyklusregeln gelten weiterhin;
eine dadurch unzulässige Ersatzverbindung wird ausgelassen. Andere belegte
Eingänge verhindern das Löschen nicht. Eindeutige lineare Ketten rücken nach links auf;
bei Verzweigungen, Zusammenführungen oder drohenden Kollisionen bleibt das Layout
unverändert. Normales Entf löscht weiterhin ohne Wiederverbinden.
Kopieren speichert den Mausversatz in Graphkoordinaten; Einfügen und Duplizieren
behalten diesen Versatz sowie die relativen Positionen einer Mehrfachauswahl bei.

Die Reihenfolge von Editoren und Inputs steht direkt in `MathNode.define(...)`:

```java
public static final PropertyId OUT = new PropertyId("audio-out");
public static final PropertyId A = new PropertyId("a");
public static final PropertyId B = new PropertyId("b");
public static final PropertyId CLAMP = new PropertyId("clamp");
public static final PropertyId UNARY = new PropertyId("unary");

@Override public void define(NodeBuilder node, NodeState state) {
    node.addOutput(OUT, "Audio Out").keepVisibleOnHide();
    node.addEditor(CLAMP, "Clamp −1 … +1", Editor.checkBox(false)).keepVisibleOnHide();
    node.addEditor(UNARY, "Nur Eingang A", Editor.checkBox(false));
    node.addInput(A, "A", Editor.Slider.button(0, null, null)).keepVisibleOnHide();
    if (!state.flag(UNARY, false)) {
        node.addInput(B, "B", Editor.Slider.button(1, 0.0, null));
    }
}
```

Inputs, Outputs und eigenständige Editoren verwenden denselben Schlüsseltyp `PropertyId`,
deklariert als `public static final` in der jeweiligen Node-Klasse. Die Rolle bestimmt
`addInput`, `addOutput` oder `addEditor`. Ein Slider-Eingang nutzt seine ID auch für
seinen Parameter (`state.number(A, 0)`); Standalone-Editoren verwenden z. B.
`state.flag(CLAMP, false)`. Ein Output benötigt keinen gespeicherten Property-Wert.
Auch Mute-Routing, native Eingangsreferenzen und Änderungs-Callbacks verwenden `PropertyId`.
Strings werden erst an den GLSwing-/Protokollgrenzen verwendet; die bisherigen gespeicherten
Namen bleiben unverändert. Doppelte Zeilennamen werden vom Builder abgewiesen;
Portreferenzen und ihre Richtung werden gegen die deklarierten Ports geprüft.

`.keepVisibleOnHide()` schützt die komplette Zeile einschließlich Port und Editor vor
dem Einklappen. Das funktioniert für Inputs, Outputs und Standalone-Editoren.
`.keepVisibleOnHide(boolean)` kann vom Zustand abhängen. Ohne diese Angabe entscheidet
der Core: Unverbundene Zeilen verschwinden, verbundene Ports bleiben sichtbar.
Der Austausch eines verbundenen Slider-Editors durch seine Portbeschriftung bleibt
ein separates Verhalten. Beim eingebauten MathNode bleibt die Operationsauswahl sichtbar.

Der Core zeigt zuerst Outputs, dann alleinstehende Editoren, dann Inputs; innerhalb
dieser Gruppen gilt die Deklarationsreihenfolge. Stabile IDs erhalten Parameter und
Verbindungen bei Umbauten. `null` bedeutet bei Slidergrenzen unbegrenzt, auch einseitig.
Die eingebauten Math-Slider behalten ihre bisherigen Grenzen von ±1000000.
Defaults entstehen aus den Editoren. Ein beim Umbau entfernter Eingang behält seinen
gespeicherten Parameter; sein Kabel wird nicht automatisch neu verbunden, wenn er später
wieder erscheint. Undo stellt den vollständigen vorherigen Zustand her.

Definitionen sind zustandslos und werden geteilt. Pro Instanz gibt `NodeState` die
unveränderlichen Modellparameter an. `define(...)` darf davon abhängige Ports und
Editoren deklarieren, ohne selbst den Graph zu verändern. Änderungen aus den Node-
Editoren und dem Core-Inspector werden durch den Controller in der History erfasst.
Eigene Controls können `Editor` implementieren und über `create(...)` einen
`Editor.Control` mit Änderungs-Callback und Modell-Update liefern; `viewKey()` beschreibt
bei Bedarf ihre Struktur. Damit braucht auch ein neuer Control-Typ keinen Factory-Branch.

`mute(MuteRouting routing, NodeState state)` legt Weiterleitungen mit
`routing.connect(A, OUT)` mit `PropertyId`-Konstanten fest. Die Basis schaltet Ausgänge stumm;
Verstärkung und Math leiten ihren ersten Audioeingang weiter. Der Oszillator wird
stumm, ohne versehentlich seinen Frequenzeingang weiterzuleiten. `isMuteable()` kann
`false` liefern; Menü, Kontextmenü und Tastenkürzel beachten das gemeinsam.
Mute-Routing beschreibt Kabel zwischen Ports, keine Java-Sampleberechnung.

`processor()` verknüpft die Klasse mit der nativen ID und der `.hpp`, `encode(...)`
liefert die zugehörigen Parameter im Steuerprotokoll. Ein neuer Java-Node kann einen
vorhandenen C++-Prozessor wiederverwenden. Für neue DSP-Berechnungen kommt eine
eigene `.hpp` unter `native/audio-host/src/nodes` hinzu, optional eine `.cpp` daneben.
`EASYKNACK_NODE(MeinNode, "meinnode")` in der Headerdatei genügt für die native
Registrierung; der Build entdeckt und kompiliert die Dateien automatisch.
Die Tests prüfen Java-ID, Headerverknüpfung und native Registrierung gegeneinander.
Details und `ProcessContext`: siehe `native/audio-host/README.md`.

Dies ist Registrierung beim Build. Dynamisches Laden externer Plugin-JARs/DLLs oder
Live-Kompilierung sind noch nicht enthalten. Kategorien sind aktuell das feste Enum
`NodeCategory`; neue Kategorien benötigen dort einen Eintrag. Der Editor kann mehrere
Outputs beschreiben; das Audioprotokoll unterstützt bis zu acht gleich getaktete
Signal-Ausgänge pro Prozessor. Der native Prozessor stellt diese über `outputCount()`
und `ProcessContext.outputAt(index)` in derselben Deklarationsreihenfolge bereit.

## Theme und Größen

Das Anwendungstheme wird in `EasyKnack.java` durch `.theme(ApplicationTheme.current().theme())` geladen.
`AudioNodeFactory` setzt keine festen Editor- oder Node-Größen mehr; die früheren
190 × 30 für Inhalte und 230 Pixel Node-Breite sind entfernt. Auch Portfarben kommen
aus dem Theme. Initiale Node-Größen entstehen aus den bevorzugten Control-Größen,
Titel, Padding und Zeilenabständen. Theme-Wechsel aktualisieren diese Berechnung,
während bewusst manuell vergrößerte Nodes ihre zusätzlichen Abmessungen behalten.

Die relevanten GLSwing-Theme-Einstellungen sind:

- `theme.sliderStyle()`: `height`, `preferredWidth` (Theme-Datei: `slider.height`, `slider.preferredWidth`).
- `theme.comboBoxStyle()`: `height`, `minimumWidth` (`comboBox.height`, `comboBox.minimumWidth`).
- Label-/Checkbox-Styles: Schrift, Padding und ihre eigenen Control-Metriken.
- `theme.values()`: `node.minimumWidth`, `node.titleHeight`, `node.rowGap`, `node.contentPadding`.

Gemeinsame Control-Höhen können in GLSwing-Themes über `metrics.controlHeight` gesetzt
werden. Größenänderungen gehören in das Theme, nicht in einzelne Node-Klassen.

## Prüfung

`mvn test` prüft Model-/View-Layout, Verbindungsbereinigung, Kopieren/Ausschneiden/
Einfügen mit neuen IDs, Duplizieren inklusive Platzierungsabbruch, Hide/Mute,
Undo/Redo, Composite-Erstellung und Navigation sowie den Kopieren-Tastaturpfad.
Zusätzlich werden dynamische Ports pro Instanz, Composite-Portänderungen, automatische
Java-Registrierung und Compilerfehler geprüft. Unter Windows wird auch die native
Dateierkennung getestet; das Audio-Profil kompiliert einen zusätzlichen Test-Node
ohne Änderung der Host-Registry.
Optional startet der folgende Test ein echtes
GLCanvas-Fenster, prüft die Oberfläche und schließt es wieder:

```sh
mvn -Deasyknack.test.gl=true verify
```

Der Grafiktest ist ohne diese Option deaktiviert, damit normale Builds auch
ohne Desktop funktionieren.

Vollständiger Lauf mit C++-Build, DSP-Tests, stummem Host-Integrationstest und GLCanvas:

```powershell
mvn -Pnative-audio -Deasyknack.test.gl=true verify
```

Optional `-Deasyknack.test.device=true` öffnet zusätzlich das echte Standard-Audiogerät,
ohne einen Ton abzuspielen. `-Deasyknack.test.audio=true` nutzt einen bereits gebauten Host.

## Signalraten und Anwendungsfarben

Der Host verarbeitet bei 48 kHz maximal 256 Samples pro Block (ca. 5,33 ms).
Kürzere Restblöcke eines Geräte-Callbacks werden ebenfalls verarbeitet.
Der MathNode bietet im Seitenbereich (Taste `N`) eine Rate-Auswahl: AudioRate berechnet jedes Sample,
ControlRate einmal pro Block. Control-Ausgänge werden über den Block gehalten;
Audio-Eingänge an Control-Ports verwenden das erste Sample des Blocks.
Das ist Sample-and-Hold, keine Mittelwertbildung oder Filterung.

Node-Definitionen deklarieren Ports mit `.rate(ProcessingRate.CONTROL)` und
unabhängig davon `.signal(SignalCategory.FREQUENCY)`. Standard ist jeweils AUDIO.
Audio-Ports sind rund, Control-Ports rautenförmig. Die Frequenz des Oszillators
bleibt AudioRate, damit Audio-Frequenzmodulation möglich bleibt.
Core-erzeugte Composite-Ports bleiben rund, verwenden aber die Theme-Farbe ihres explizit gesetzten öffentlichen Signaltyps;
die Raten der enthaltenen Prozessoren bleiben beim Zusammenfassen erhalten.

`src/main/resources/easyknack.gltheme` enthält die zwei Titelverlaufsfarben
je NodeKind (`AUDIO`, `VALUE`, `MIDI`, `WAVETABLE`) sowie eine Farbe je SignalCategory. Eine externe Datei kann mit
`-Deasyknack.theme=C:/pfad/mein-theme.gltheme` ein vollständiges GLSwing-Theme laden.
Sie wird beim Start geladen. Größen und Control-Metriken kommen weiter aus GLSwing.

NativeProcessor deklariert zusätzlich die PropertyIds seiner Eingänge in derselben
Reihenfolge wie die nativen `sources`. Protokoll v3 ergänzt jeden Node-Datensatz um
`@ a|c Eingangsmaske`, beispielsweise `@ c cc`. Anwendung und Host gemeinsam bauen.


Editor-Properties nur im Seitenbereich deklarieren:

```java
node.addEditor(RATE, "Rate", Editor.comboBox(
    List.of(ProcessingRate.values()), ProcessingRate.AUDIO, ProcessingRate::id))
    .inspectorOnly();
```

Die Property behält ihre PropertyId, ihren Default, ihre Validierung und Undo/Clipboard-
Unterstützung. Im Node wird dafür keine Zeile erzeugt. Der Core-Inspector verwendet
weiter seine Property-Schemas für Comboboxen, Checkboxen und begrenzte Slider.
`N` schaltet den vorhandenen Core-Inspector über die EasyKnack-Tastaturanbindung um;
Textfelder behalten ihre Texteingabe. GLSwing bleibt eine unveränderte externe Bibliothek.
Gruppenfarben stammen ausschließlich aus dem am öffentlichen Gruppenport eingestellten
Signaltyp, nicht aus inneren Verbindungen. Die Auswahl bleibt der Core-Signaltypvertrag
und unterliegt damit auch dessen Regeln für kompatible Verbindungen.


### Welche Theme-Datei wird geladen?

Standardmäßig lädt EasyKnack `easyknack.gltheme` vom Klassenpfad. Dieses Theme erbt
explizit `/com/vingame/glswing/themes/default.gltheme` aus dem externen GLSwing-JAR.
`GLThemes.DEFAULT` lädt diese Ressource ebenfalls indirekt über `GLThemeDefaults`.
Maßgeblich ist die Kopie im Bibliotheks-JAR, nicht die Quelldatei im GLSwing-Projekt.
Anwendungsfarben sind typisierte `@COLOR easyknack.*`-Werte im GLSwing-Format.

Änderungen unter `src/main/resources` benötigen `mvn package` (oder Eclipse Build),
bevor das gebaute Programm sie sieht. Änderungen im GLSwing-Quellprojekt benötigen
ein neu gebautes/installiertes Bibliotheks-JAR und aktualisierte EasyKnack-Abhängigkeiten.
EasyKnack liest weiterhin keine Dateien aus dem GLSwing-Repository.

Für Änderungen ohne Neubau eine externe Datei, etwa `C:/Themes/custom.gltheme`, verwenden:

```properties
theme.name=Mein EasyKnack
theme.extends=default
slider.height=22
@COLOR easyknack.signal.CONTROL=#F0BE5A
@COLOR easyknack.node.MATH.title.light=#796298
@COLOR easyknack.node.MATH.title.dark=#51416A
```

```powershell
java -Deasyknack.theme=C:/Themes/custom.gltheme -jar target/easy-knack-0.0.1-SNAPSHOT.jar
```

Bei Datei-Themes lädt `theme.extends=default` eine benachbarte `default.gltheme`, falls
vorhanden, sonst das Default-Theme aus der eingebundenen Bibliothek. Andere Basen können als relative Dateipfade
angegeben werden. Der absolute Klassenpfad-Verweis der eingebetteten `easyknack.gltheme`
ist ausschließlich für Ressourcen gedacht und darf nicht als Dateiverweis kopiert werden.
Fehlende EasyKnack-Kategoriefarben übernehmen die gebündelte Palette. Die Konsole zeigt
beim Start die verwendete Theme-Datei; externe Änderungen werden nach Neustart wirksam.


## Node-Dokumente speichern und laden

EasyKnack verwendet `GLNodeGraphDocumentCodec` und `GLDocumentManager` aus der externen
GLSwing-Bibliothek. Die Datei enthält den ursprünglichen Model-Graph (kein DSP-Export),
alle eingebetteten Gruppen, Verbindungen, Parameter einschließlich Inspector-Properties,
Hide/Mute, Gruppenport-Verträge und Node-Positionen/-Größen. Gruppennamen werden ebenfalls
bewahrt. Auch innerhalb einer Gruppe speichert Strg+S das gesamte Dokument, inklusive
des aktuellen inneren Layouts. Zoom, Pan und die aktuelle Auswahl werden nicht gespeichert.

Datei-I/O läuft im Hintergrund. Der Core schreibt zunächst eine temporäre Datei und ersetzt
anschließend die Zieldatei. Ein unveränderlicher Snapshot stellt sicher, dass spätere
Bearbeitungen weiterhin als ungespeichert gelten. Ein Stern im Fenstertitel kennzeichnet
Änderungen. Der erste Speichervorgang öffnet die Dateiauswahl; spätere schreiben dieselbe
Datei. Abbrechen verwirft keine Änderungen.

Laden prüft bekannte Node-Typen, Parameter, dynamische Port-Verträge und Verbindungen vor
dem Dokumentwechsel. Defekte oder inkompatible Dateien lassen den bisherigen Graph erhalten.
Nach erfolgreichem Öffnen wird die Bearbeitungshistorie zurückgesetzt und Audio gestoppt;
die Wiedergabe startet erst wieder über Audio → Start. Separat referenzierte externe
Gruppendateien werden in dieser ersten Version ausdrücklich abgewiesen; eingebettete,
auch verschachtelte Gruppen werden vollständig unterstützt.


## DSP-Auslastung

Oben im Editor zeigt die DSP-Anzeige das Verhältnis aus gemessener DSP-Callback-Zeit
und dessen Zeitbudget (`Frames / 48000 Hz`). Bei 256 Frames sind das ca. 5,33 ms;
1 ms Berechnung entspricht 18,75 %. Die Engine besitzt einen eigenen Takt; unabhängige
Geräte-Callbacks sind über Ringbuffer angebunden. Gemessen werden Graphberechnung,
Graphwechsel, Masterrampe und Bufferübergabe; die Anzeige ist keine Gesamt-CPU-Auslastung
und misst nicht die Arbeit sämtlicher Gerätetreiber-Callbacks.

- DSP: über etwa eine halbe Sekunde geglätteter Wert, zehn Aktualisierungen pro Sekunde.
- Peak: höchster Callback-Wert seit Start des Host-Prozesses.
- Über Budget: Anzahl der Callbacks mit mehr als 100 % seit Host-Start. Das sind gemessene
  Zeitüberschreitungen, kein vom Audiotreiber gemeldeter Hardware-XRun-Zähler.

Die Audioseite verwendet eine monotone Uhr und lockfreie Atomics; Netzwerk-I/O und
Textformatierung finden auf dem Steuerthread statt. Solange der Host noch nicht gestartet
ist oder nicht erreichbar ist, erscheint `DSP —`. Der Host berechnet nur Zweige, die
einen Ausgang versorgen. Unverbundene Nodes und nach der Mute-Durchleitung unbenutzte
Eingangszweige entfallen aus dem Ausführungsplan; unbenutzte Capture-Geräte werden
nicht geöffnet. Gemeinsam genutzte Quellen bleiben aktiv, solange ein Ausgang sie
benötigt. Inaktive Prozessoren behalten ihren Zustand, ihre Phase läuft nicht weiter.
Das globale Stummschalten der Host-Ausgabe lässt die benötigten Zweige weiterlaufen.
Anwendung und Host gemeinsam neu bauen: Protokoll 9 enthält Endpoint-Erkennung,
ASIO-Kanalpaare und IO-Konfiguration; der STATS-Befehl bleibt erhalten.

Neue Dokumente erhalten `.knack`. Der Inhalt bleibt der versionierte GLSwing-Codec;
es gibt kein zweites Serialisierungsformat. Bestehende `.glgraph`-Dateien können über
den zusätzlichen Öffnen-Filter geladen werden. Ihr erster Speichervorgang fragt nach
einer `.knack`-Zieldatei; die alte Datei bleibt erhalten.

## Additiver Oszillator

Der zusätzliche Generator „Additiver Oszillator“ verwendet Sinus, Dreieck, Sägezahn
oder Rechteck **pro Teilton**. „Teiltöne (max.)“ zählt den Grundton mit: 1 bedeutet
einen einzelnen Oszillator, maximal sind 144 möglich. Falloff und Spreading erscheinen
ab zwei Teiltönen oder sobald der Partials-Eingang verbunden ist; ihre Werte bleiben
beim Ausblenden erhalten. `NodeState.isConnected(PropertyId)` steht Node-Definitionen
für solche verbindungsabhängigen Anzeigeentscheidungen zur Verfügung.

- Falloff gewichtet Teilton n mit `1 / n^Falloff` (0: gleiche Gewichte).
- Spreading verstimmt die Obertöne deterministisch um bis zu den eingestellten Cent;
  der Grundton bleibt unverändert. Es ist kein zeitlich wechselnder Zufall.
- Frequenz und Phase (Grad) sind verbindbare Sliderports. Die Phase bezieht sich auf
  den Grundton und wird entsprechend dem Frequenzverhältnis auf die Teiltöne angewandt.
- Noise ist als deaktivierter Regler vorbereitet und hat noch keine Klangwirkung.

Die Summe wird durch die Summe der aktiven Gewichte geteilt. Teiltöne ab der
Nyquist-Frequenz werden ausgelassen; die eingebauten Wellenformen verwenden vorberechnete
bandbegrenzte Tabellen. Tabellenaufbau erfolgt außerhalb des Audio-Callbacks.

Der Wellenform-Port hat den eigenen Datentyp `easyknack.waveform`, damit normale
Audiosignale nicht versehentlich als Wellenform angeschlossen werden. Für künftige
Wellenform-Nodes interpretiert der Host einen verbundenen Buffer als genau eine
vollständige Periode mit linearer Interpolation. Ein entsprechender Quellen-Node
ist noch nicht enthalten; eigene Tabellen werden derzeit nicht bandbegrenzt.

## Audioeingänge, Geräte und COPY-Capture (Windows-MVP)

Unter IO gibt es einen **Audioeingang** mit Quellenwahl, Copy-Modus und Mono/Stereo.
Der vorhandene **Audioausgang** hat eine Gerätewahl und verwendet zunächst **System
Default**. Mehrere Eingänge und Ausgänge sind unabhängig verwendbar. Die bisherigen
DSP-Nodes verarbeiten jetzt beide Kanäle mit getrenntem Zustand; Stereo wird nicht
auf Mono reduziert. Mono im Eingang mittelt links/rechts und legt das Ergebnis auf
beide Kanäle.

Der Host wird für die Geräteerkennung im Hintergrund gestartet. Aufnahme und
Gerätewiedergabe beginnen erst mit **Audio → Start** und enden mit **Stop**.
Geräte und Audiositzungen werden alle zwei Sekunden aktualisiert; unter Audio gibt
es zusätzlich „Geräte und Anwendungen aktualisieren“. Anwendungen erscheinen, wenn
Windows eine Audiositzung für sie kennt. Mehrere unabhängige Prozessbäume derselben
EXE werden unter einer Quelle zusammengefasst; Kindprozesse werden nicht doppelt
erfasst. Persistiert wird der EXE-Pfad, nicht die wechselnde Prozess-ID.

System Audio und Anwendungscapture verwenden Windows-Prozess-Loopback und benötigen
Windows Build **20348 oder neuer**. Bei System Audio wird der eigene Host-Prozess
ausgeschlossen, um eine digitale Rückkopplung der eigenen Ausgabe zu vermeiden.
Auf älteren Systemen werden diese Fähigkeiten nicht angeboten; Hardware-IO bleibt
verfügbar. COPY ändert die Windows-Zuordnung der Quelle nicht: Die originale Ausgabe
läuft weiter. Ein zusätzlicher EasyKnack-Ausgang kann sie daher zusätzlich hörbar machen.

`.knack` speichert Endpoint-Typ, stabile ID, Anzeigename, Capture-Modus und Kanäle.
Fehlende Quellen bleiben als „nicht verfügbar“ ausgewählt; ihre Samples sind Stille.
Es gibt keinen stillen Wechsel auf ein anderes Gerät. Nur **System Default** folgt
absichtlich dem Windows-Standardgerät. Open-Fehler erscheinen zusätzlich im Status.

Die Backend-Schicht konvertiert Hardwareformate nach Stereo Float / 48 kHz. Die
Engine berechnet bis zu 256 Frames pro Block. Unabhängige Geräteuhren sind über
begrenzte Ringbuffer angebunden; zwei Blöcke dienen als anfänglicher Jitterpuffer.
Unterläufe liefern Stille, Überläufe verwerfen ankommende Frames. Ein rudimentärer
adaptiver Drift-Ausgleich hält die Füllstände bei unabhängigen Geräteuhren stabil.
Optimierte Gesamtlatenz und einzelne Surround-Nodeports sind noch nicht enthalten.
Änderungen der ausgewählten Streams können die Audioausgabe kurz unterbrechen.

Automatische Windows-Zuordnung und deren Wiederherstellung werden weiterhin nicht
angeboten oder durch COPY simuliert. Der eigene virtuelle Treiber ist zurückgestellt.

## Externe Routingtools: WASAPI und ASIO

Die Ein-/Ausgangsauswahl zeigt **[WASAPI]**, **[ASIO]** oder **[Mitschnitt]** an.
WASAPI bietet physische und externe virtuelle Windows-Geräte unabhängig vom
Hersteller an. ASIO bietet Kanalpaare installierter 64-Bit-Treiber, einschließlich
der Insert-Treiber externer Routingtools. Ein ASIO-Treiber taktet den gesamten
Graphen direkt aus seinem Duplex-Callback. Mehrere Kanalpaare desselben Treibers
und zusätzliche WASAPI-Geräte sind möglich; mehrere unabhängige ASIO-Treiber
in einem Graphen werden mit einer Fehlermeldung abgewiesen.

Weiterhin 48 kHz, Stereo pro Nodeport; die bevorzugte ASIO-Buffergröße wird in
maximal 256 Frames große DSP-Blöcke unterteilt. Der bestehende Masterpegel von 0,2
bleibt erhalten. Routingtools separat installieren und dort Send/Return bzw.
virtuelle Kabel konfigurieren. EasyKnack installiert oder bündelt keine fremden
Treiber und ändert keine Boot-Einstellungen. VST3/JACK folgen später.

Anleitung, Grenzen und Tests: [Externe Audioverbindungen](../external-audio-routing.md).
Der native ASIO-Build verwendet SDK-Header unter deren GPLv3-Option; Lizenz- und
Quellhinweise: [ASIO-Provenienz](../../native/audio-host/third_party/asio/PROVENANCE.md).
Java und Host verwenden jetzt Protokoll 9; bitte beide zusammen bauen.

## Value and wavetable oscillators

`Value` produces a control-rate scalar (once per DSP block). Its value editor has
no application-defined minimum/maximum; the audio engine uses finite 32-bit floats.
Both oscillators select Sine, Triangle, Sawtooth or Square, with an optional
`easyknack.waveform` input. The built-in waves share precomputed 2048-sample tables
with ten harmonic limits and linear interpolation. Table generation runs during
configuration, outside the audio callback. A connected waveform currently supplies
one periodic table per input block; this is not yet a file-based wavetable bank.

The additive oscillator places outputs first and Waveform first among inputs. Partials (1–144) has a control-rate
input; Falloff and Spread (-2400…2400 cents) have audio-rate inputs. Their bounded
editors use bars. Falloff and Spread are hidden at one partial, retaining their
controls and values; connected ports stay visible. Output device volume also uses
a bar. Noise remains disabled.

Parameter updates are coalesced at 20 ms; device volume polling remains at 100 ms,
and load/status polling at 250 ms. Changes do not restart unchanged audio routes.
Oscillator frequency, additive spectrum/phase and audio-rate Math fallback values
transition over a 5 ms time constant. Connected oscillator frequency/phase signals
remain sample-accurate. This reduces parameter clicks; it is not a guarantee
against driver or operating-system underruns and does not change device latency.

English node titles/property labels are in `src/main/resources/nodes.properties`,
keyed by stable node IDs and PropertyIds. A future `nodes_de.properties` can be
selected with `-Deasyknack.language=de`; document and protocol IDs do not change.
The Wavetable port color is `easyknack.signal.WAVETABLE` in `easyknack.gltheme`.

Waveform inputs on both oscillators are control-rate ports. Their payload is block
data: the oscillator uses the complete table supplied for that block. Unlike scalar
control inputs, tables are not reduced to the first sample (`inputUsesBlockData`).
The engine determines mono/stereo per node and reuses mono buffers in stereo graphs.


### Mono and stereo execution

Channel inference follows dependencies at graph compilation time. Built-in
oscillators, Value and processing nodes stay mono when every connected input is
mono. A stereo input promotes its downstream processing to stereo; it does not
promote independent mono branches feeding the same mixer. Control rate and channel
count are independent. Mono buffers are evaluated once per block and reused for
both output channels, including delivery to both hardware channels.

Native processors opt in through `canShareChannels()`. Return true only when equal
inputs and state produce equal channels and one execution has all required side
effects. The default is false for compatibility with channel-dependent plugins.
InputNode opts in only for mono capture. No allocation or channel analysis occurs
in the audio callback. When a graph changes from mono to stereo, both channels
inherit the previously active mono state; stereo to mono continues the left state.
This is structural inference, not an audio-content detector: a stereo capture
remains stereo even when its current samples happen to be identical.

Spread shifts every overtone by the same cent offset; the fundamental stays fixed.
For overtone number n >= 2, frequency = fundamental * n * 2^(cents/1200).
Thus +100 raises every overtone by one semitone, and -100 lowers it by one semitone.


Editor.Slider.unit(UnitFormat.HZ) (or another predefined unit) puts units after the numeric
value in both nodes and the sidebar. Property labels and stored numeric values
remain unit-free; the output device volume uses the same formatting with "%".

Core sliders now expose `setUnit("Hz")` / `getUnit()` independently of their
numeric `setValueFormatter`. Units are drawn last without their own picking target
and are excluded from the inline editor document. Inline editing suppresses slider
background, progress, arrows and label; right padding reserves space for the unit.
Control-rate ports now use Core `GLNodePort.Shape.DIAMOND`, sharing the normal
hover and connection-candidate painting instead of an application-side overlay.

## Kurveneditor, Mapping und Hüllkurven

`Math / Curve` bildet einen frei wählbaren Eingangsbereich auf einen Ausgangsbereich
ab. Standard ist 0..1 → 0..1; für bipolare Kennlinien können beide Bereiche auf
−1..1 stehen. Umgekehrte Bereiche sind erlaubt. Bei identischen Eingangsgrenzen
liefert der Node den unteren Ausgangswert. `Clamp` begrenzt die Eingangsposition;
`Linear extension` setzt die Randsteigung außerhalb des Bereichs fort.
Die Verarbeitungsrate lässt sich im Inspector einstellen; Mute reicht den Eingang durch.

`Generators / Envelope` startet mit einer ADSR-Kurve: Attack, Decay bis zum
Sustain-Marker und Release. Die Punkte bestimmen Form und zeitliche Aufteilung;
`Attack + Decay` und `Release` bestimmen die jeweilige Dauer in ms. `Sustain position`
verschiebt die Trennung zwischen beiden Kurvenabschnitten. Ein positiver Gate-Wert
startet und hält die Hüllkurve, die fallende Gate-Flanke löst Release vom aktuellen
Pegel aus. Eine positive Trigger-Flanke startet erneut; ohne Gate erfolgt ein
vollständiger einmaliger Durchlauf. Die Inputs werden samplegenau ausgewertet.
Retrigger beginnt beim aktuellen Pegel; Release endet bei null. Ein flacher
Release-Abschnitt wird als lineares Ausblenden interpretiert. Im Standard-ADSR
liegt Sustain auf dem dritten Punkt; zusätzliche Punkte sind frei ergänzbar.

Die Kurve lässt sich direkt im Node bearbeiten. Bei Hide bleibt eine kompakte,
nicht editierbare Vorschau erhalten. Der Inspector zeigt dieselbe Kurve größer.
Doppelklick fügt Punkte ein; Ziehen bewegt Punkte und Bézier-Griffe. Shift rastet
auf ein 0,05-Raster ein. Entf entfernt den gewählten Zwischenpunkt. Rechtsklick
auf Punkt oder Abschnitt bietet Linear, Step und Bezier, gekoppelte Griffe,
Löschen, Spiegeln der linken Hälfte und Rücksetzen auf das Node-Preset.
Spiegeln ist für kontinuierliche Kurven verfügbar. Die Endpunkte bleiben bei X=0/1;
Y und Griffe liegen im normierten Bereich 0..1. Maximal 64 Punkte sind erlaubt.
Griffe werden auf ihre jeweilige Segmenthälfte begrenzt, sodass die Funktion
eindeutig bleibt. Ein Drag erzeugt einen Undo-Schritt. Kurven und Bereiche werden
im .knack-Dokument gespeichert; Hide verändert die Audioberechnung nicht.

Die wiederverwendbaren `GLFunctionCurve`/`GLCurveEditor` und die Erweiterung für
eigene Inspector-Editoren gehören zur externen GLSwing-Bibliothek. EasyKnack
besitzt die versionierten Kurvendaten, Node-Definitionen und C++-Prozessoren.
Der Host berechnet Bézier-Lookuptabellen beim Konfigurieren auf dem Steuerthread;
lineare und gestufte Segmente werden exakt ausgewertet. Der Audio-Callback
benötigt weder Speicherallokation noch Bézier-Inversion. Ein dedizierter
Waveshaper mit Oversampling ist noch nicht enthalten; Audio-Kennlinien im
Curve-Node sind derzeit nicht gegen Aliasing durch Nichtlinearitäten geschützt.

Theme: `curve.height`, `curve.previewHeight`, `curve.inspectorHeight`,
`curve.padding`, `curve.pointRadius`, `curve.hitRadius`; Farben über
`curve.background`, `curve.grid`, `curve.line`, `curve.handle`, `curve.marker`.
Die Nodebreite bleibt durch `node.minimumWidth` bestimmt.

## Node-Größe, Filter und MIDI

Normale Nodes, Gruppen und Gruppenports lassen sich nur horizontal vergrößern.
`Curve` und `Envelope` erlauben zusätzlich vertikales Vergrößern: ausschließlich
der Kurveneditor erhält die zusätzliche Höhe. Hide zeigt wieder eine feste kleine
Vorschau; beim Aufklappen kehrt die vorherige Höhe zurück. Eigene Node-Definitionen
können `resizeMode()` überschreiben und eine Editorzeile mit `growVertically()`
markieren; die Verteilung übernimmt GLSwing über `setContentVerticalGrow`.

`Effects / Filter` ist ein resonanter TPT-State-Variable-Filter mit Lowpass,
Highpass und Bandpass. Cutoff (Hz) und Resonanz (Q, 0,1–30) haben Eingangsports.
Parameter und Moduswechsel werden über ungefähr 5 ms geglättet; Stereo-Kanäle
besitzen getrennte Filterzustände. Die Zustände bleiben bei Parameterupdates erhalten.
Mute und Einfügen/Herauslösen verwenden den Audioeingang als Durchleitung.

Die Kategorie `MIDI` enthält:

- `MIDI Input`: Computertastatur (Standard) oder Windows-MIDI-Gerät wählen. Hardwaregeräte werden über WinMM gefunden und
  nur geöffnet, wenn der MIDI-Zweig einen Audioausgang versorgt. Die gespeicherte
  Auswahl bleibt bei fehlenden Geräten sichtbar; Discovery und erneutes Öffnen
  erfolgen ungefähr alle zwei Sekunden. `All notes off` setzt MIDI-Zustände zurück.
- `MIDI Note`: 1–32 Stimmen, bei einer Stimme mit Last-note-Priorität. Ausgänge für Frequenz in Hz,
  Gate, einen ein Sample langen Trigger und Velocity (0–1). Im monophonen Modus führt Loslassen der zuletzt
  gedrückten Taste zur zuvor gehaltenen zurück. Sustain (CC64), Pitch Bend,
  Note-On mit Velocity 0, All Notes Off und All Sound Off werden berücksichtigt.
  Channel 0 verarbeitet alle Kanäle, 1–16 nur den gewählten.
- `MIDI CC`: Controller 0–127 auswählen, Ausgabe normiert auf 0–1. Über einen
  nachgeschalteten Curve-/MapRange-Node beispielsweise auf Filterfrequenzen abbilden.

MIDI-Ports besitzen einen eigenen Datentyp und eine Themefarbe (`easyknack.signal.MIDI`).
Sie transportieren Ereignisse, keine Audiosamples. Die Converter-Ausgänge verwenden
Event-Verarbeitung und behalten Wertänderungen samt Samplepositionen.
Der native MIDI-Callback schreibt Zeitstempel und Nachrichten in begrenzte Queues;
der Audio-Thread ordnet sie Positionen im aktuellen Block zu (höchstens ein Block
zusätzliche Einordnungslatenz). Die Hardware-/WinMM-Zustellung ist keine Garantie
für physisch samplegenaue MIDI-Zeitstempel. Bei Queue-Überlauf oder Geräteverlust
werden gehaltene Noten zurückgesetzt. MIDI-Ausgabe, SysEx und MIDI 2.0
sind noch nicht enthalten. Geräteidentitäten basieren auf Name, Hersteller-/Produkt-ID
und Vorkommensnummer; mehrere identische Geräte können eine erneute Auswahl benötigen.

Das Beispiel `examples/mono-midi.knack` ist eine fertige monophone Kette:
MIDI Input → MIDI Note; Frequency → Oscillator → Filter → Math (Multiply) → Audio Output.
Gate und Trigger steuern Envelope; Envelope liegt am zweiten Math-Eingang.
Nach dem Laden MIDI-Gerät und Audioausgang auswählen, Audio starten und spielen.
Die Velocity kann über einen weiteren Multiply-Node die Lautstärke skalieren.
Das vorhandene Standard-Beispiellayout bleibt unverändert.

Java und C++ verwenden Protokoll 9. Ein Prozessor kann bis zu acht gleich getaktete
Signal-Ausgänge bereitstellen (`NodeProcessor.outputCount`, `ProcessContext.outputAt`).
Quellreferenzen sind `nodeIndex + 256 * outputIndex`; alte einportige Referenzen
bleiben unverändert. MIDI-Events haben eigene Blockpuffer und werden nicht skalar
auf ControlRate reduziert. Das .knack-Dokumentformat bleibt kompatibel.

Technische Referenzen: [TPT-SVF, Andrew Simper](https://www.cytomic.com/files/dsp/SvfLinearTrapOptimised.pdf),
[Microsoft MidiInProc](https://learn.microsoft.com/en-us/previous-versions/dd798460(v=vs.85)).


## Polyphonie und Computertastatur

`examples/poly-midi.knack` enthält einen spielbaren Synthesizer mit acht Stimmen:
MIDI Input → MIDI Transpose → MIDI Delay → MIDI Note. Frequency steuert Oscillator,
Gate/Trigger steuern Envelope. Math multipliziert Oszillator und Hüllkurve **je Stimme**.
Voice Merge summiert die Stimmen; der anschließende Filter wird nur einmal pro
Audiokanal berechnet. Audio starten, im MIDI Input auf **Click to play** klicken
und mit A W S E D F T G Y/Z H U J K O L P spielen. A entspricht C4; Y und Z
bezeichnen alternativ denselben Halbton für QWERTY/QWERTZ. Die Anschlagsstärke
ist 100/127. `Transpose` im Input verschiebt in Halbtönen, 12 = eine Oktave.
Escape, Klick außerhalb, Deaktivieren/Entfernen des Controls und Fensterfokusverlust
lösen gehaltene Tasten. Während des blauen Spielfokus sind globale Menü-/Undo-/Node-
Shortcuts gesperrt. Mehrere Keyboard-Inputs empfangen dasselbe virtuelle MIDI-Gerät.
Die üblichen Grenzen der Computertastatur (Key Rollover/Ghosting) bleiben bestehen.

`Voices` ist lokal am MIDI Note: Jeder Konverter begründet eine eigene Stimmengruppe.
Nachgeschaltete DSP-Nodes übernehmen sie automatisch; monophone Eingänge werden auf
alle Stimmen verteilt. Gruppen und verschachtelte Gruppen werden vor dieser Analyse
aufgelöst. Unabhängige Stimmengruppen müssen vor gemeinsamen Operationen einzeln durch
Voice Merge laufen, selbst wenn ihre Stimmenzahlen gleich sind. Audio Output summiert polyphone Zweige automatisch. Unzulässige Verbindungen pausieren
die Wiedergabe bis zur Korrektur; der Host bleibt verbunden. Voice Merge bestimmt bei Bedarf
Summierung, Mittelwert oder Stereo-Verteilung vor nachfolgenden gemeinsamen Effekten.

Freie Stimmen werden zuerst verwendet, dann freigegebene, schließlich wird die älteste
gehaltene Stimme ersetzt. Sustain wird pro MIDI-Kanal berücksichtigt. Release-Phasen
laufen in ihren Stimmen weiter, bis eine Stimme wiederverwendet wird. Jede konfigurierte
Stimme eines erreichbaren Zweiges besitzt eigene DSP-Zustände; ein Abschalten einzelner
fertiger Stimmen ist noch nicht implementiert. Unverbundene Zweige bleiben wie bisher
inaktiv. Die Grenze beträgt 32 Stimmen pro Gruppe und 2048 aktive Prozessorinstanzen
vor der zusätzlichen Stereoaufteilung. Speicher wird außerhalb des Audiothreads angelegt.

MIDI Transpose verwendet einen Regler in **st**: 1 = Halbton, 2 = Ganzton, 0,01 = Cent.
Feintuning wird intern neben der unveränderten MIDI-Notenidentität transportiert;
so passt NoteOff auch nach einer Parameteränderung. Änderungen gelten für neue Noten.
MIDI Delay verzögert sämtliche Ereignisse um 0–10000 ms, auch NoteOff, Sustain und Bend.
Bereits eingeplante Ereignisse behalten ihre Zeit; beim Verkürzen wird die Ereignisreihenfolge
gewahrt. Panic/Geräteverlust löscht die Queue sofort. Die Queue hält höchstens 4096
Ereignisse; Überlauf setzt den betroffenen MIDI-Zweig zurück, statt Noten hängen zu lassen.

Tooltips erklären Gate, Trigger, Velocity und CC sowie Wertebereiche. Definitionen setzen
sie über `Row.tooltip(...)`; Übersetzungen können `<node-id>.<property-id>.tooltip`
überschreiben. Slider ergänzen ihren Bereich automatisch. Die Texte sind auch direkt
an den Ports erreichbar, wenn der verbundene Editor verborgen ist. Core-Tooltip-Schatten
verwenden `tooltip.shadow.color`, `.softness`, `.offsetX`, `.offsetY`; standardmäßig
erben sie die entsprechenden `menu.shadow.*`-Werte. Der Spielfokus verwendet
`easyknack.midi.keyboard.focus` aus `easyknack.gltheme`.

ControlRate bei Math und Map Range wertet die Formel nur einmal pro DSP-Block aus,
anschließend wird der Ausgang gehalten. Bei 128 Frames sind das 1 statt 128
Formelauswertungen; Pufferbefüllung und Graph-Verwaltung fallen weiterhin an.
ControlRate übernimmt den ersten Eingangswert des Blocks und eignet sich daher
nicht für samplegenaue Trigger oder schnelle Audiomodulation. MIDI-Ereignisblöcke
behalten trotz Rautenports ihre individuellen Samplepositionen.

## Event-Ports und explizite Verarbeitung

Portformen: Kreis = AudioRate, Raute = ControlRate, Raute mit kleiner gefüllter
Innenraute in Borderfarbe = Event. Die Farbe bleibt unabhängig davon der Datentyp.
MIDI-Eingänge/-Ausgänge sind Event-Ports, auch an Gruppen. MIDI Note und MIDI CC
geben numerische Wert-Events aus; rohe MIDI-Nachrichten bleiben ein eigener Datentyp
und lassen sich nicht direkt mit Math verrechnen. Die Größe der Innenraute steht
im Core-Theme unter `node.portEventInnerScale` (Standard 0,36).

Math, MapRange, Mix, Curve und die Einheiten-Converter bieten im Seitenbereich unter
**Rate** Auto (Standard), AudioRate, ControlRate und Event. Auto nimmt die kleinste
Rate, die alle angeschlossenen Quellen verlustfrei abbildet: Audio vor Event vor
Control. Ohne Anschluss genügt ControlRate. Eine explizit gewählte Rate bleibt erhalten.
Tooltip am Regler und Eingangsport erklärt die Modi. Bei einem direkt verbundenen
AudioRate-Node gibt der Tooltip zusätzlich einen konkreten Abtastungshinweis.

- AudioRate: alle Samples verarbeiten. Event-/Control-Werte werden bis zur nächsten
  Aktualisierung gehalten.
- ControlRate: ausschließlich den ersten Samplewert jedes DSP-Blocks verwenden.
  Kurze Ereignisse innerhalb des Blocks können dadurch vollständig verpasst werden.
- Event: bei numerischen Wertänderungen oder geänderten Control-Blockwerten auswerten.
  Audioeingänge werden nur zu diesen Zeitpunkten abgetastet und lösen allein keine
  Berechnung aus. Neue Nodes und Änderungen ihrer Parameter werden einmal ausgewertet.
  Ein unveränderter Graph erhält den gehaltenen Wert ohne zusätzliche Berechnung.

Alle Änderungen am gleichen Sample werden gemeinsam verarbeitet, es entstehen keine
Zwischenergebnisse durch die Reihenfolge der Eingänge. Bleibt ein berechnetes Ergebnis
gleich, wird kein neues Wert-Event weitergereicht. Gate, Frequenz, Velocity und beide
Flanken eines Triggerpulses bleiben samplegenau, auch an Blockgrenzen. Polyphone Stimmen
und Stereo-Kanäle führen eigene Werte und Ereignispositionen.

Beispiel: MIDI CC → MapRange (Event, 0–1 auf 100–8000) → Filter-Cutoff.
MapRange rechnet nur bei Änderungen, während der Filter weiterhin Audio verarbeitet
und seine Frequenz glättet. Curve unterstützt ebenfalls Event.
Intern stehen gehaltene Samplepuffer für Audioverbraucher bereit; Event
spart Formelauswertungen, aber nicht sämtliche Puffer- und Graph-Verwaltungskosten.


## Mischung, Einheiten und Fehlerpause

**Mix** ist ein linearer Crossfade mit zwei Signalen und einem Faktor-Balken:
`A * (1 - factor) + B * factor`. Faktor wird auf 0–1 begrenzt. Die Rate steht im
Inspektor (Auto, Audio, Control, Event); lokale Regleränderungen werden bei AudioRate geglättet.

**Voice Merge** bietet ausschließlich Add und Mean. Add summiert alle Stimmen,
Mean teilt zusätzlich durch alle konfigurierten Stimmen (auch freigegebene/stille).
Stereo-Kanäle bleiben erhalten. Ausgänge summieren Stimmen automatisch.

**Pan** ist unabhängig davon: Audio In und ein modulierbarer Pan-Balken (−1 links,
0 Mitte, +1 rechts). Vor Merge arbeitet Pan pro Stimme, danach auf dem gesamten Mix.
Mono wird mit konstanter Leistung verteilt; Stereo wird balanciert. In der Mitte ist
die Verstärkung je Kanal 1, am Rand auf dem verbleibenden Kanal √2. Ohne verbundenen
Pan-Eingang werden Regleränderungen über etwa 5 ms geglättet. Für Position nach Tonhöhe
MIDI-Note-Frequenz über Map Range/Math auf −1 bis +1 abbilden und an Pan anschließen.
Alte Stereo-Merge-Instanzen werden als nicht im Katalog angebotene Kompatibilitäts-Nodes
weitergeladen, damit bestehende Dokumente ihren Klang und ihre Verbindungen behalten.

Ein ungültiger Java- oder nativer Graph pausiert einen gestarteten Host. Ein gültiger
Graph setzt die Wiedergabe automatisch fort. Stop während der Pause verhindert das
automatische Fortsetzen. Transport-/Prozessausfälle bleiben davon getrennte Fehler.

Slider mit zugeordneten **UnitFormats** besitzen im Slider-Kontextmenü das Untermenü `Unit` (im Node und im Inspektor).
Neue Gain-Nodes verwenden Faktor (1 = 100 % = 0 dB), neue Phase-Regler Umdrehungen
(1 = 360°). Gain 0 entspricht Stille; die endliche dB-Untergrenze ist −160 dB.
Tonhöhe bietet Cent, Semitones, Octaves und Factor: 0 Cent = Faktor 1, 1200 Cent =
12 Semitones = 1 Octave = Faktor 2. Negative Tonhöhenverschiebung braucht einen Faktor
zwischen 0 und 1. Spread reicht von −2400 bis 2400 Cent, entsprechend ±2 Oktaven.
Frequenz unterstützt Hz/kHz, Zeit ms/s, Phase Umdrehungen/Grad/Radian.

Die Auswahl gilt für den Slider **und seinen Eingangsport**. Beim Wechsel wird der
lokale Sliderwert physikalisch erhalten; angeschlossene Zahlen werden in der neuen
Einheit interpretiert. Bei verbundenem Slider steht die Einheit im Ersatzlabel.
Ein Faktor-2-Signal an Spread bedeutet in Faktorstellung +1200 Cent. Die Umrechnung
zum internen Format erfolgt durch generierte native Converter im Graph; diese erben
Stimmen, Rate und Erreichbarkeit. Sie zählen zum Limit von 256 Prozessoren.
Alte Dokumente ohne Einheitenwahl behalten die ursprünglichen Einheiten. Die Auswahl
wird unter `<property-id>.unit` gespeichert; interne Werte/Property-IDs bleiben stabil.

Der Katalog bietet getrennte Converter mit jeweils passenden From-/To-Auswahlen:
**Gain Converter** (Faktor, %, dB), **Pitch Converter** (Cent, Semitones, Octaves,
Faktor), **Frequency Converter** (Hz/kHz), **Time Converter** (ms/s) und
**Phase Converter** (Umdrehungen/Grad/Radian). Jede Variante bietet Auto und alle drei expliziten Raten.
Bestehende allgemeine Converter werden beim Laden in die passende Variante überführt;
seltene alte Umrechnungen zwischen Gain- und Pitch-Verhältnissen bleiben kompatibel.
Bei logarithmischen Umrechnungen ist der kleinste positive Bezugsfaktor 10⁻⁸;
dB wird nach unten auf −160 begrenzt.

Der Inspektor übernimmt auch eigene Editoren automatisch über deren `Editor.create`
und Update-Bindung, einschließlich Geräteauswahl, Live-Lautstärke und Pegelanzeige.
Deaktivierte Eigenschaften bleiben sichtbar und deaktiviert. Fehlende Eigenschaften
alter Dokumente erhalten die definierten Defaults, ohne vorhandene Werte zu ersetzen.

Für eigene Definitionen: `row.formats(UnitFormats.PROPORTION)` oder ein eigenes
`UnitFormats(stored, initial, choices)`. Alternativ direkt `Editor.Slider.button(...).formats(...)`.
`.unit(UnitFormat.HZ)` legt dagegen eine feste Einheit fest. Eine Erkennung anhand von Suffix-Strings gibt es nicht mehr.
Node und Inspektor verwenden denselben generischen Core-Slider.

Strg+Entf schließt sichere lineare Lücken exakt auf `nodeEditor.horizontalNodeSpacing` (siehe
`GLNodeEditor.getHorizontalNodeSpacing()`). Verzweigte Ketten und Kollisionen lassen
das Layout weiterhin unverändert. Ein DSP-Threadpool ist noch nicht aktiv; Messungen
für unabhängige Stimmen/Zweige stehen in TODO.md.


## Context Input

`Context Input` liefert ControlRate-Ausgänge: **Channel** (links −1, rechts +1),
**Sample Rate [Hz]** (DSP-Rate, aktuell 48000) und **Block Size [samples]** (aktuelle
Frames pro Kanal im DSP-Block, maximal 256). Abhängige Nodes werden getrennt je Kanal
berechnet; unverbundene Context-Nodes werden wie andere unbenutzte Zweige übersprungen.

Beispiel: Channel → MapRange (From −1…1, To 220…440, Clamp aus) → Oscillator Frequency
in Hz → Audio Output. Links erklingen 220 Hz, rechts 440 Hz. MapRange kann hierfür
mit Auto selbstständig ControlRate verwenden. Channel gibt den Kanal-Kontext wieder und steuert von sich aus
keine Lautsprecherposition; dafür gibt es Pan. Die Blockgröße beschreibt keine gesamte
Geräte-/Routinglatenz und kann bei kurzen letzten Blöcken variieren.


## Automatische Rate und Kanal-Routing

Die Rate-Auswahl **Auto** bleibt im Dokument gespeichert; die effektive Rate wird
bei Anschlussänderungen und vor dem Senden an den Host neu ermittelt, auch durch
verschachtelte Gruppen und nach dem Auflösen von Mute/Bypass. Portformen zeigen
die effektive Rate. Mehrfach instanziierte Gruppendefinitionen zeigen im Editor
die höchste Rate ihrer verwendeten Instanzen. Alte feste Einstellungen bleiben
unverändert; für bestehende Nodes Auto einmal im Inspektor wählen.

**Channel Separate** (Input / Output) zerlegt ein Stereosignal in **Left** und **Right**.
Jeder Ausgang ist ein Mono-Signal und liegt beim weiteren Verarbeiten identisch
auf beiden Kanälen an. Mono am Eingang erscheint an beiden Ausgängen.
**Channel Combine** setzt **Left** und **Right** zu **Stereo Out** zusammen.
Unverbundene Eingänge liefern Null. Falls ein Combine-Eingang selbst Stereo führt,
nimmt Left dessen linken und Right dessen rechten Kanal.

Kanaltausch: Separate Right → Combine Left, Separate Left → Combine Right.
Die Nodes erhalten MIDI-Stimmengruppen; zum Zusammenfassen von Stimmen dienen
weiterhin Voice Merge oder Audio Output. Beide Kanal-Nodes können normal gemutet
werden (Stille).

Der Host berechnet in Graphen mit aktiven Separate-Nodes beide Quellkanäle vor den
Verbrauchern und hält dafür vorbereitete Kanalpuffer vor. Das fügt keine Blocklatenz
hinzu. Mono-Teilgraphen werden weiterhin einmal berechnet; unabhängige/disconnected
Zweige bleiben inaktiv. Graphen ohne kanalübergreifende Verarbeitung behalten den
bisherigen speichersparenden Ablauf. `NodeProcessor.separatesChannels()` kennzeichnet
den kanalübergreifenden Mono-Ausgang, `ProcessContext.rightInputs` stellt dessen
rechten Quellkanal bereit; sämtliche Speicherreservierungen erfolgen vor dem Callback.


### Node type, menu categories and group units

`@RegisterNode.kind` selects the title palette; `category` independently selects the Add-menu folder.
Theme keys are `easyknack.node.AUDIO.title.light` / `.dark` (and VALUE, MIDI, WAVETABLE).
Channel and voice routing is under Routing, unit converters and MIDI-to-value nodes under Converters,
and envelopes under Modulation. Mix sockets use the Value palette.

Inside a group, the Composite interface panel offers **Node type (title color)** and **Add menu category**.
It also offers separate **Insert: input/output** and **Mute bypass: input/output** pairs.
Choose public ports to enable insertion into an existing wire or a mute bypass; `None`
disables insertion or silences the muted group respectively. Only compatible pairs are used.
These settings are stored in the group asset, survive save/load and support undo/redo.
Insertion and Ctrl+Delete use the existing workspace actions and the configured insertion input.
Group default width follows `node.minimumWidth` in the theme, including theme updates;
manually enlarged nodes retain their width.
Select an input and choose Slider to configure its range, presentation and allowed units.
The storage unit is retained; compatible additional units can be enabled. Choose the instance unit
through the slider context menu **Unit**. Local values preserve their physical meaning; incoming
connections are interpreted in the selected external unit and converted to the internal input unit.
This also works through nested groups and survives `.knack` save/load. Conversion nodes only exist
in the executable graph, not in the saved or visible document.

Core owns generic editor compatibility, interface editing, transactions and workspace navigation.
EasyKnack supplies numeric signal compatibility, group title types, unit controls and audio adapters.
The old Wellenform group type is migrated to Wavetable and is no longer offered separately.

Slider mouse events are UI events, not AudioRate computations. EasyKnack coalesces pending graph
updates over 8 ms; the existing host transport publishes the latest state at most every 20 ms.
Only changed nodes are normalized, and automatic-rate results are cached until the topology or
rate selection changes. DSP parameters switch at block boundaries; sample smoothing and connected
AudioRate modulation remain independent of UI update frequency.


### Group fanout and live slider updates

A public group input can target multiple internal inputs. The Core stores every target in
node-document schema 7 (schemas 1–6 remain readable), restores all boundary wires, and expands
fanout during graph compilation. Editor/range/unit changes preserve these bindings. EasyKnack
inserts a separate unit adapter for each target, so targets may use different compatible units.
Removing a dynamic input removes only that target; the public port disappears when its last
previously bound target is removed.

Model projection updates only changed nodes unless topology or the registry changes. Numeric
slider edits avoid remeasuring unchanged node layouts; free sliders also keep one history
transaction throughout a drag. A 100-update regression test includes 80 additional nodes.

Native continuous parameters can opt into `smoothControlInput(port)`. ControlRate signals feeding
such AudioRate inputs use a 5 ms one-pole transition, initialized at the actual first value and
retained across graph swaps. AudioRate signals, event timing, MIDI triggers, discrete ports and
sink inputs retain their existing semantics. Local parameter smoothing remains processor-owned.


### Typed slider units in the Core

```java
// Fixed unit, no unit-selection submenu:
Editor.Slider.button(440, 0.00001, 20000.0).unit(UnitFormat.HZ);
// Explicit pool: stored Hz, selectable Hz/kHz:
Editor.Slider.button(440, 0.00001, 20000.0).formats(UnitFormats.FREQUENCY);
// Stored dB, initially displayed as factor, with factor/%/dB choices:
Editor.Slider.button(-12, -160.0, 6.0).formats(UnitFormats.GAIN_DB);
```

GLSwing provides `GLUnit`, `GLUnitFormats<U>` and `GLUnitSlider<U>`. The generic slider owns
conversion, displayed bounds, reset values, step selection, suffix and context menu. Use
`getStoredValue` / `setStoredValue` for model values and `getValue` / `setValue` for displayed
values. Model synchronization and unit selection do not emit stored-value edits. A unit-selection
listener lets an application persist the selected unit separately. `GLSlider.setUnit(String)`
remains a display-only suffix API for existing Core clients.

EasyKnack defines concrete units and permitted pools, persists stable unit IDs and converts
connected signals in the DSP graph. Its `UnitSlider` is only the node-model adapter.
`bar(double,double,double)` still requires fixed bounds, while `button(double,Double,Double)`
retains optional bounds (`null` means unbounded).


## Parameterverträge, Analyse und Quellenexport

Neue Nodes beschreiben ihre native Schnittstelle mit `ParameterContract.builder()`:
`source(A).value(A).source(B).value(B).value(OPERATION).build()`.
`NativeProcessor.inputs()` wird daraus abgeleitet. Werte, Grenzen und Enum-Auswahl stammen aus
der Node-Definition; `integer`, `curve`, `mapped` und bedingte `source`-Felder decken spezielle
Datentypen und dynamische Ports ab. `mapped` ist für kodierte Gerätekennungen oder sonstige
Domänenabbildungen gedacht. Eigene Strings mit Leerzeichen sind in Nodeklassen nicht mehr nötig.
Maven erzeugt bei `process-classes` `target/native/generated/ParameterContracts.hpp`.
Die C++-Seite liest benannte Felder, z. B. `parameters::MathNodeParameters::read(in)` und `p.a_value`.
Protokoll 10 und Schema-Fingerprints verhindern, dass ein alter Host eine neue Feldreihenfolge
versehentlich akzeptiert. Nach Änderungen Java und Host gemeinsam mit `-Pnative-audio` bauen.
GLSwing bleibt eine externe Maven-Bibliothek; für diese Funktionen wurde keine Core-Quellabhängigkeit ergänzt.

Unter **Analysis** stehen **Oscilloscope** und **Autocorrelation** bereit. Beide analysieren
wahlweise den linken oder rechten Kanal und summieren vorher alle ankommenden Stimmen.
Das Oszilloskop ist ein reiner Analyse-Endpunkt ohne Audioausgang; alte durchgeschleifte
Dokumentverbindungen werden beim Laden auf das ursprüngliche Signal umgelegt.
Unterhalb von Trigger low wird es scharf, beim anschließenden Überschreiten von Trigger high
startet die Aufnahme. Ohne Flanke wird automatisch aufgenommen. Die Triggerlinien lassen
sich per Rechtsklick auf den Graphen ein-/ausblenden; diese Einstellung wird gespeichert.
Autocorrelation zeigt die normalisierte Korrelation über die Verzögerung und liefert Frequenz in Hz
sowie Confidence von 0 bis 1 als ControlRate-Ausgänge. Das 8192-Sample-Fenster entspricht bei
48 kHz etwa 171 ms und wird alle 4096 Samples aktualisiert. Das verzögert nur die Analyse,
nicht das hörbare Signal. Stille liefert 0 Hz / 0 Confidence; bei komplexen Klängen bleiben
Oktavverwechslungen möglich. Beide Graphen wachsen auch vertikal und bleiben bei Hide als
kleine Vorschau sichtbar. Bei mehrfach verwendeten Gruppen zeigt die Anzeige die erste aktive Instanz.

Gruppennavigation und der Inspector-Tab **Group Settings** erscheinen nur innerhalb einer Gruppe.
Die Core-Flächenfarben sind im Core-Theme konfigurierbar: `application.clearColor`,
`nodeWorkspace.background` und `compositeInterface.background`. Ein expliziter Clearcolor-Override
an der Anwendung hat weiterhin Vorrang. Auch Splash, Breadcrumb-Hover, Diagnosemarkierungen,
Farbauswahl und weitere UI-Farbdefaults stammen aus dem Theme. Technische Transparenz,
Shader-Farbmultiplikatoren und eigentliche Farb-/Bilddaten bleiben davon unabhängig.
EasyKnack ergänzt `easyknack.analysis.triggerLow`, `easyknack.analysis.triggerHigh` und
`easyknack.midi.keyboard.focus` in `src/main/resources/easyknack.gltheme`.

Eine Gruppe auswählen und **Nodes → Export group as plugin …** aufrufen. Die `.knackplugin`-Datei
ist ein ZIP mit einer normalen `BasicNode`-Klasse, C++-Header-/Quelldateien, `plugin.properties`,
Bauhinweisen und `source.knack`. Der exportierte Node hat keine Gruppennavigation. Öffentliche
Slider, Fallbacks und Einheiten bleiben veränderbar; interne Einstellungen werden aus der gewählten
Instanz übernommen. Explizite Insert- und Mute-Portpaare werden in die Java-Klasse übernommen.
Installiert wird das Paket über **Plugins → Install source plugin …**; ein Neubau von EasyKnack
oder eine Quellcode-Abhängigkeit zu GLSwing ist dafür nicht erforderlich.

Der erste Import kompiliert im Hintergrund die Java-Klassen und einen passenden Audiohost mit den
installierten nativen Plugin-Rechnern. Ein SDK mit den exakten Hostquellen liegt im Anwendungs-JAR.
Java-Klassen und Host werden erst nach erfolgreichem Build und Parametervertragsprüfung aktiviert.
Während der Installation zeigt eine feste Zeile über dem Nodeeditor den aktuellen Schritt und
die verstrichene Zeit (Cache-Prüfung, Java, C++, Hostprüfung). Der Gruppenname wird als sichtbarer
Plugin-Node-Name und Dateinamenvorschlag übernommen, ebenso die gewählte Katalogkategorie und Titelfarbe.
Neue Nodes erscheinen direkt im Add-Menü; ein laufender Host wird dafür kurz neu gestartet.
Bereits geladene, veränderte Plugins werden erst nach einem Anwendungsneustart aktiviert, damit
bestehende Views und Undo-Einträge nicht unterschiedliche Klassenversionen vermischen.

Quellen und Cache liegen unter `%USERPROFILE%/.easyknack/plugins` (überschreibbar mit
`-Deasyknack.plugins=...`). **Open plugin sources** öffnet die bearbeitbaren Dateien;
**Rebuild plugins** erstellt daraus einen neuen Build. Bestehende kompatible Builds bleiben bei
Fehlern erhalten, und die Fehlermeldung verweist auf `work/.../build.log`. Pluginquellen führen
Java-/C++-Code mit den Rechten der Anwendung aus; es handelt sich nicht um eine Sandbox.

Normale Starts prüfen und laden den Cache. Quellinhalt, Anwendung/GLSwing/SDK, Plattform, Java-Version,
Compilerdateien und Buildoptionen bestimmen den Build. SDK-Änderungen können einen Neubau auslösen;
ein inkompatibler alter Build wird auch bei Fehlern nicht geladen. Es gibt kein allgemeines DLL-ABI:
der separat gecachte Host und alle darin enthaltenen Rechner werden gemeinsam kompiliert.

Die erste Version unterstützt **Windows x64**, benötigt zum Bauen ein **JDK ab 17** und
**LLVM-MinGW x64**. Die vorhandene Toolchain unter `target/tools` beziehungsweise `tools` wird
automatisch gefunden. Alternativ **Configure C++ compiler …** verwenden oder in
`plugins/toolchain.properties` die Pfade `cxx` und optional `javac` hinterlegen; `easyknack.plugin.cxx`
überschreibt den C++-Pfad. Ein automatischer Toolchain-Download und der Import fertiger Binärplugins
sind noch nicht implementiert. Mit aktiven Plugins verwendet Audio den dazugehörigen Cache-Host;
`easyknack.audio.host` gilt für den Betrieb ohne aktive Plugins.

Der Export löst verschachtelte Gruppen und Einheitenadapter auf, entfernt unerreichbare Zweige
und erzeugt statische Aufrufe konkreter C++-Prozessoren mit vorab angelegten Puffern. Das erlaubt
Compiler-Inlining und weitere Optimierungen ohne Laufzeit-Graphinterpreter. Teure DSP-Algorithmen
werden dadurch nicht automatisch so schnell wie eine speziell handgeschriebene Gesamtimplementierung.
Reine Rechenabschnitte mit mindestens zwei im topologischen Ablauf aufeinanderfolgenden
Math-, Mix-, MapRange- oder Unit-Converter-Nodes besitzen
zusätzlich einen fusionierten Audio-Pfad, auch innerhalb gemischter Gruppen: eine Sampleschleife
pro Kanal und lokale Zwischenwerte. Alle außerhalb des Abschnitts verwendeten Werte bleiben im
Buffer erhalten, auch abzweigende Zwischenwerte. Untergruppen werden vorher aufgelöst; ihre Grenzen
verhindern daher keine Fusion. Die Fusionsstufe unterstützt einheitliche Mono- oder Stereo-Abschnitte
ohne interne Stimmenaufteilung. Control-/Event-Raten, Einzel-Sample-Blöcke und nicht unterstützte
Fälle nutzen den ursprünglichen Scheduler. Fallback-Glättung und numerische Schutzregeln bleiben erhalten. Plugin-Builds verwenden
`-O3 -ffp-contract=off` ohne Fast-Math. Der Vergleichstest prüft alle Math-Operationen, Zwischen-
ausgänge, Stereo, Ratenwechsel und Zustandsübernahme und misst zusätzlich die Laufzeit. Ein weiterer
nativer Vergleich deckt verschachtelte Gruppen mit Oszillatoren, Filter, zwei Math-Abschnitten,
abzweigenden Zwischenwerten sowie Channel Combine/Separate ab.
Konstante Ausdrücke werden einmal beim Erzeugen des Prozessors berechnet, exakt gleiche
Ausdrücke innerhalb eines Abschnitts wiederverwendet. Es findet keine algebraische Umordnung
statt. Reine Math-Abschnitte nutzen auf SSE2-Systemen vier Samples gleichzeitig, mit skalarem
Rest und denselben NaN-/Unendlich-/Clamp-Regeln. `EASYKNACK_DISABLE_SIMD` deaktiviert diesen Pfad.
Neue Exportoptimierungen gelten für neu exportierte Plugins; ein Neubauen alter, eventuell
manuell bearbeiteter Quellen schreibt diese nicht um.

Der normale Host teilt kanalübergreifende Sample-Buffer anhand ihrer Lebensdauer. Fanout,
Stimmen und alle noch benötigten Ausgänge bleiben erhalten; die Planung erfolgt vor der
Übergabe an den Audiothread. Generierte Fusionspläne behalten separate Caches, weil deren
Sampleschleifen andere Lebensdauern besitzen. Beim Complex Oscillator berechnet Spreading
unveränderte Obertongewichte nicht mehr neu; der gemeinsame Detuning-Faktor wird nur einmal
pro Spektrumänderung berechnet.

Der Offline-Benchmark unter `native/audio-host/benchmarks` vergleicht serielle Berechnung
mit vier dauerhaften Workern für 1/8/32 Stimmen und 128/256 Frames. Er berichtet Mittelwert,
99. Perzentil, Maximum und Deadline-Verletzungen. Die Worker-Barriere ist ausdrücklich kein
Produktionspfad im Audio-Callback: Durchschnittsgewinne allein rechtfertigen keine zusätzlichen
unbegrenzten Wartezeiten. Details und reproduzierbarer Aufruf: [DSP-Optimierungen](../../native/audio-host/benchmarks/README.md).

MIDI Input/Transpose/Delay/Note/CC, Stimmenallokation, Voice Merge, Wert-Events und
Channel Separate/Combine können innerhalb der exportierten Gruppe liegen. Der erzeugte feste
C++-Aufrufplan verwendet dieselben Event-, Stimmen- und Kanalpuffer wie der Host; vorbereitet wird
er ausschließlich beim Konfigurieren. Interne Stimmen werden einmal berechnet und an der Grenze
weitergegeben oder intern gemergt. Von außen kommende Stimmen behalten getrennten DSP-Zustand.
MIDI-Ausgänge haben jeweils eigene Event-Puffer. Auto-Raten werden aus den internen Abhängigkeiten
abgeleitet; alle numerischen Ausgänge eines exportierten Prozessors verwenden die höchste benötigte
Ausgangsrate. MIDI bleibt davon unabhängig ein Event-Port.

Audio-Geräte-I/O bleibt außerhalb solcher Verarbeitungsgruppen. Unabhängige Stimmen-Domänen vor
gemeinsamen Ausgängen zuerst mergen; ungemergte polyphone Ausgänge und MIDI-Ausgänge benötigen
getrennte Nodes. Interne Gerätewahlen sind fest eingebaut; für eine auswählbare MIDI-Quelle oder
ein Tastaturkeyboard mit Fokusfeld einen öffentlichen MIDI-Eingang exportieren. Gemutete interne
Nodes müssen vor dem Export aufgelöst werden. Binäre Plugin-APIs sind weiterhin eine spätere Ausbaustufe.

## Rate Converter

Unter **Converter → Rate Converter** lässt sich ein numerischer Eingang auf ControlRate, AudioRate
oder Wert-Events umsetzen. Bei ControlRate stehen First sample, Last sample, Mean, RMS, Minimum,
Maximum und Absolute peak zur Wahl. Mean ist der vorzeichenbehaftete Mittelwert; ein symmetrisches
Audiosignal ergibt dabei ungefähr null. RMS und Absolute peak liefern nichtnegative Beträge.
Die Auswertung nutzt die tatsächliche Blocklänge (maximal 256 Samples, bei 48 kHz ca. 5,33 ms),
liest den vollständigen Eingang und hält den ermittelten Wert für diesen Block. Es entsteht kein
zusätzlicher Pufferblock. Die Reduktion ist bei den anderen Ausgangsraten deaktiviert.

AudioRate erhält die einzelnen Samples, einschließlich gehaltener Control-/Event-Werte.
Event erzeugt bei jeder Wertänderung ein Ereignis an dessen Sampleposition. Das ist ein numerischer
Event-Ausgang, kein MIDI-Ausgang; bei bewegtem Audio können entsprechend viele Events entstehen.

## Render-Diagnose

Der Lastwert im Kopfbereich ist anklickbar; alternativ **Audio → Render diagnostics**.
Die GL-Ansicht zeigt ein einsekündiges Fenster mit gewichteter mittlerer Renderlast,
Spitzenlast und p99 der einzelnen Render-Aufrufe, außerdem das Maximum seit Messbeginn.
Der Verlauf hält höchstens 60 Sekunden bzw. 256 UI-Snapshots. Die rote Markierung bedeutet
Zuwachs eines Fehlerzählers, nicht zwingend einen hörbaren Aussetzer.

Die Zeitmessung umfasst `AudioEngine.render` einschließlich Graph-I/O. Die ausgewiesenen
Frames sind die Länge dieses Render-Aufrufs; interne DSP-Blöcke können kleiner sein.
100 % bezeichnet dessen Rechenzeitbudget, nicht die gesamte CPU-Auslastung. Ringbuffer-
Unter-/Überläufe von Capture und Playback, ASIO-Überlastmeldungen sowie Priming-Blöcke
werden getrennt angezeigt. Hardware-Deadline-Verletzungen außerhalb dieses Messbereichs
sind nicht allgemein beobachtbar. Priming wartet auf die Reserve und zählt nicht als Unterlauf.

`Reset measurement` startet eine neue Messepoche, ohne Audio anzuhalten. Host-, Routing-
und Betriebswechsel beginnen ebenfalls neue Abschnitte; die Initialisierung erfolgt bei
der nächsten Diagnoseabfrage. Die Zähler beziehen sich auf die so begonnene Messung.
Bei Pause bleiben die letzten Werte als pausiert sichtbar. Ein neuer Host leert die Historie.
`Save report…` exportiert CSV mit Messwerten, Clock-/Bufferbeschreibung und Buildkennung;
es werden keine Audiodaten gespeichert. Die Farben stehen als `easyknack.diagnostics.*`
in `easyknack.gltheme`.

Die native Telemetrie transportiert Messungen über einen begrenzten SPSC-Puffer (4096
Einträge); ihr einziger Schreiber ist die jeweils aktive Render-Clock. Umschalten stoppt
zuerst den bisherigen Clock-Pfad. Der Protokollthread berechnet die Statistiken. Das
Einsekundenfenster ist zusätzlich auf 65536 Messungen begrenzt. Verluste erscheinen als
`Telemetry lost`; bei Verlust sind Maxima und Perzentile nur für die erfassten Werte gültig.
`STATS` und `IOSTATS` bleiben kompatibel; `DIAGNOSTICS` Version 1 ergänzt strukturierte Daten.
Alte Hosts ohne diesen Befehl behalten die bisherige kompakte Lastanzeige.

## Gruppen lokal backen

Im Inspektor einer Gruppe kompiliert **Bake** die Gruppe mit den bestehenden
Exportoptimierungen. Der Inspektor zeigt den Fortschritt und anschließend `Baked`
oder eine Fehlermeldung (vollständig im Tooltip des Bake-Buttons). **Disable bake**
wechselt zurück zur normalen Graphberechnung. Die Gruppe bleibt editierbar;
Änderungen an ihren DSP-Einstellungen oder Untergruppen deaktivieren ihren Bake.
Ein erneuter Klick auf **Bake** kompiliert den aktuellen Stand. Verschieben und
Ausblenden allein erfordern keinen Neubau.

Der Bau läuft im Hintergrund und verwendet den lokalen, inhaltsabhängigen
Plugin-Buildcache, installiert aber kein Plugin. Beim Aktivieren wird kurz auf den
passenden Audiohost gewechselt; dabei wird Audio unterbrochen und danach mit dem
bisherigen Wiedergabewunsch fortgesetzt. Compilerfehler behalten den ursprünglichen
Graphen. Änderungen während des Baus verhindern die Aktivierung veralteter Ergebnisse.

Die `.knack`-Datei enthält den ursprünglichen Graphen und den aktivierten Bake-Zustand.
Beim Öffnen werden gespeicherte Bakes automatisch aus dem kompatiblen lokalen Cache
wiederhergestellt oder bei fehlendem Cache im Hintergrund neu kompiliert. Native
Binärdateien werden nicht eingebettet. Währenddessen bleibt der normale Graph verwendbar.
Untergruppen sind im Export enthalten. Werden Eigenschaften einer inneren Gruppe
durch die übergeordnete Gruppe gesteuert, muss die übergeordnete Gruppe gebacken
werden. Ein Bake garantiert keinen Geschwindigkeitsgewinn: Die verfügbaren
Optimierungen hängen von den enthaltenen Nodes und deren Verschaltung ab.

## DSP-Referenzprojekte und Baselines

Unter [examples/reference-v1](../../examples/reference-v1/README.md) liegen acht `.knack`-
Referenzdokumente für Math, Synth/Polyphonie, Stereo, Raten, Analyse und Geräte-Routing.
Die opt-in Tests vergleichen Original, exportiertes Plugin und Export ohne Fusion
mit festen MIDI-Sequenzen und prüfen die isolierte Plugininstallation. Ein separater
Routingrunner misst VB-Cable und einen echten Ausgang über längere Zeit, ohne die
Windows-Gerätepegel zu verändern. Wiederholung und Befunde: [DSP-BASELINE.md](../DSP-BASELINE.md).
