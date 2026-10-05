# Später

## Core / application ownership

- [x] Extract shared label tooltips, image capture/encoding/preview, remembered
  dialogs, editor capabilities/templates and nested graph copying into Core.
- [x] Separate inspector coupling and graph diagnostics within EasyKnack.
- [ ] Resolve the seven baseline Core test failures documented in the audit
  (theme/layout expectations and node exclusion spacing).

- Architecture classification and ordered extraction backlog: [Core boundary audit](docs/CORE-BOUNDARY.md).
- Generic mechanisms now live in GLSwing; application rate/unit/DSP policies remain in EasyKnack.

## Automatische Build-Kennung und Versionspflege

Dauerhafte Arbeitsregeln: [AGENTS.md](AGENTS.md). Core und EasyKnack unabhängig
versionieren; Produktversion nach kumuliertem Änderungsumfang seit dem letzten
Release pflegen, nicht pro Kompilierung erhöhen.

- [x] Build-Metadaten aus Maven-Version, Git-Commit und Dirty-Status generieren.
  Offizielle Paket-Builds mit CI-Laufnummer plus Wiederholungskennung versehen;
  lokale Paket-Builds eindeutig als lokal kennzeichnen. Keine Versionsdateien
  durch Eclipse-Inkrementalbuilds verändern und keine global eindeutige Nummer
  durch einen bloßen lokalen Zähler vortäuschen.
- Implementiert für Maven-Paket-Builds beider Projekte; Diagnose-CSV zeigt beide
  Kennungen. IDE-Starts ausdrücklich als ungepackt markieren. Details:
  [Build identity](docs/BUILD-IDENTITY.md). Release-CI selbst ist noch einzurichten.
- [ ] Zentrale Versionsanzeige für Über-Dialog und Diagnose-/Fehlerexporte nutzen;
  bisherige hartcodierte Fallbacks prüfen. Installer-kompatible Version getrennt
  von ausführlicher Anzeige und SemVer-Build-Metadaten ableiten.
- [ ] Englische Release-Notizen und einen klaren Release-Ausgangspunkt etablieren;
  Git-Tags bei autorisierten Releases setzen. Bestehende Releases unveränderlich
  lassen; Format-, ABI- und Protokollversionen separat prüfen.

## Eigenständig nutzbare Windows-Auslieferung

Stand: Pakettests mit gebündelter Laufzeit und eingeschränktem PATH bestehen
(Host, MP3, Formula, Gruppenexport/Plugin-Build, Cache-Neustart, Dokumente und
EXE-Fenster). Keine Abnahme auf einer frischen Windows-Installation.

Ziel: Installieren und `EasyKnack.exe` starten, ohne Java, Maven, Eclipse oder
Compiler manuell einzurichten. Zuerst einen portablen Anwendungsordner prüfen,
danach einen EXE-/MSI-Installer mit `jpackage` erstellen. Die Launcher-EXE darf
zugehörige Laufzeitdateien im Paket benötigen; keine einzelne Monolith-Datei nötig.

- [x] Lokalen Windows-Paket-Build ergänzen: EasyKnack, externe GLSwing-JAR,
  passende Java-Laufzeit, vorgebauter Audiohost, native Grafik-/Audio-/Medienbibliotheken,
  Plugin-SDK, Themes, Sprachen und Beispiele vollständig bündeln.
- [x] Bake, Formula und Quellcode-Plugins ohne Entwicklungsinstallation ermöglichen:
  passende C++-Toolchain einschließlich benötigter Header, Linker und Bibliotheken
  sowie Java-Compiler-Unterstützung mitliefern. Der aktuelle Plugin-Build erwartet
  `javac.exe`; eine reine Minimal-Java-Laufzeit genügt dafür nicht unverändert.
- [x] Toolchain und Ressourcen relativ zur Installation finden; Abhängigkeiten von
  `target/tools`, Eclipse, lokalem Maven-Cache, PATH und aktuellem Arbeitsverzeichnis
  beseitigen. Start aus Pfaden mit Leerzeichen und Nicht-ASCII-Zeichen prüfen.
- [ ] Schreibgeschützte Installation unterstützen: Einstellungen, Plugins, temporäre
  Builds und versionierte Caches im Benutzerverzeichnis speichern. Updates dürfen
  Nutzerdaten nicht überschreiben; inkompatible Caches zuverlässig invalidieren.
- [x] Normalstart mit vorgebautem Host ohne Kompilierung sicherstellen. Nur bei
  tatsächlichem Bedarf für Plugins, Bake oder Formula kompilieren; Fortschritt und
  Fehler verständlich anzeigen. Funktionierende Builds bei Fehlern erhalten.
- [x] Portables Windows-Paket mit `jpackage --type app-image` erstellen und testen.
  Build und isolierte Prüfungen: [Windows packaging](docs/WINDOWS-PACKAGE.md).
- [ ] Installer auf sauberem Windows installieren/deinstallieren, Updates prüfen;
  eigenes Icon, Signierung und optionale `.knack`-Dateizuordnung ergänzen.
  EXE-Installer mit Version aus Maven und Startmenü ist gebaut; MSI-Option
  vorhanden, aber noch nicht separat geprüft.
- [ ] Lizenz-/Weitergabeanforderungen der gebündelten Laufzeit, Toolchain und
  Bibliotheken prüfen und erforderliche Lizenztexte/Quellcodehinweise beilegen.
- [ ] Abnahme auf sauberem Windows ohne JDK, Maven oder C++-Werkzeuge: Start,
  Audioausgabe, MP3-Import, Formula, Bake, Plugin-Installation, Speichern/Laden und
  Neustart mit Cache. Unterstützte Windows-Version und Architektur dokumentieren.
- [ ] Externes Systemaudio-Routing separat dokumentieren: Eine erforderliche
  virtuelle Audiogeräte-/Treiberinstallation wie VB-Cable ist nicht automatisch
  Bestandteil des Anwendungspakets.

## Nutzerdokumentation grundlegend überarbeiten

- [ ] Die gesamte Nutzerdokumentation auf Englisch verfassen; Terminologie und
  Beispiele durchgehend konsistent halten.
- [ ] README und Nutzerdokumentation professionell und konsequent aus Sicht der
  Anwender neu strukturieren: Einstieg, Installation/Start, grundlegende Bedienung,
  typische Arbeitsabläufe und eine klar getrennte Referenz zum Nachschlagen.
- [ ] Nur Informationen aufnehmen, die für die Nutzung relevant sind. Historische
  Architekturentscheidungen, behobene Entwicklungsprobleme und interne
  Implementierungsdetails aus den Nutzerseiten entfernen; weiterhin benötigte
  Entwicklernotizen separat ablegen und nicht mit der Anleitung vermischen.
- [ ] Allgemeine Anleitung unabhängig von Eclipse formulieren. IDE-/Build-Anweisungen
  gehören in eine gesonderte Entwicklerdokumentation.
- [ ] Zu spezielle Node-Details aus Einstieg und allgemeinen Anleitungen in eine
  gezielt verlinkte Node-Referenz verschieben. Dort Zweck, Anschlüsse, Einstellungen
  und nutzungsrelevante Grenzen erläutern, nicht die Entstehungsgeschichte.
- [ ] Wichtiges priorisieren, Wiederholungen und überholte Aussagen entfernen,
  Begriffe und Sprache vereinheitlichen; kurze, nachvollziehbare Beispiele verwenden.
- [ ] Abnahme: Ein neuer Nutzer findet ohne Entwicklungswissen den Weg vom Start
  zum ersten funktionierenden Graphen und kann Detailfragen gezielt nachschlagen.
  Sämtliche beschriebenen Schritte mit dem aktuellen Programm prüfen.

## Wiedereinstieg: Core und Formula-Editor (02.10.2026)

### Zuletzt umgesetzt und geprüft

- [x] Formula-Editor mit expliziter Sprache konstruieren; die Vorbereitung des
  ersten Editors erfolgt im Ladefenster mit lokalisiertem Statustext.
- [x] Monospace-Schriftatlas ebenfalls im Ladefenster vorbereiten. Messungen in
  separaten JVMs: Java-Öffnungsvorbereitung ca. 813 → 43 ms; erster Atlas-Aufbau
  ca. 3667 ms, Wiederverwendung ca. 77 ms. Keine Messung der gesamten sichtbaren
  Öffnungszeit; Nutzer meldet deutlich bessere Reaktion.
- [x] Unbehandeltes ESC aus dem Codeeditor an die Gruppennavigation weiterreichen;
  Vervollständigung/Umbenennung behalten Vorrang, normale Text-Shortcuts bleiben geschützt.
- [x] Popup-Tastaturauswahl scrollt den gewählten Eintrag ins Sichtfeld, auch beim
  Wechsel vom letzten zum ersten Eintrag. Normale GLList hatte dies bereits.
- [x] Core als externe Maven-Bibliothek lokal installiert und `target/lib` aktualisiert.
  Keine Quellcode-/Projektabhängigkeit zu GLSwingNext hinzugefügt.

Prüfstand: vollständiger EasyKnack-Maven-Package-Lauf am 01.10. erfolgreich:
383 Tests, 0 Fehler, 106 übersprungen (`target/formula-startup-app.log`).
Die danach erfolgten ESC-/Schriftänderungen wurden mit gezielten Core- und
Formula-Tests sowie EasyKnack-Package geprüft (`target/formula-escape-core.log`,
`target/formula-font-app.log`). Abschließend bestanden `GLPopupKeyboardScrollTest`
und `MavenSmokeTest#codeEditor` mit Core-Install (`target/popup-scroll-core.log`).
Nach der letzten Popup-Änderung wurde kein vollständiger Testlauf wiederholt.
Logs unter `target` können beim Clean verschwinden; die offenen Befunde stehen deshalb hier.

### Nächster Arbeitsschritt: sieben Core-Testfehler untersuchen

- [ ] Zuerst den vollständigen Core-Testlauf reproduzieren, danach die betroffenen
  Tests isoliert ausführen. Theme-/Ressourcen-Konfiguration und Testreihenfolge
  prüfen. Nicht ungeprüft Sollwerte anpassen: Produktfehler, veraltete Erwartung
  und Testisolation unterscheiden. Die Ursache ist bisher nicht nachgewiesen.

Letzter vollständiger Core-Lauf: 104 Tests, 7 fehlgeschlagen
(`target/formula-startup-core.log`, 01.10.). Die späteren gezielten erfolgreichen
Läufe ersetzen diesen Befund nicht und überschreiben Teile der Surefire-Berichte.

| Test | Fehlermeldung |
| --- | --- |
| `GLNodeInsertionTest.exclusionReconnectsAndOnlyClosesSimpleChains` | `expected: <320.0> but was: <200.0>` |
| `MavenSmokeTest.controlsAndFocus` | `Default button height is not 14 pixels` |
| `MavenSmokeTest.configurationEditors` | `Theme editor did not save a minimal override file` |
| `MavenSmokeTest.localizationAndThemes` | `Bundled themes were not loaded as resources` |
| `MavenSmokeTest.lists` | `Visible cell range did not follow the viewport` |
| `MavenSmokeTest.menus` | `Oversized popup did not fill its available viewport` |
| `MavenSmokeTest.themeCodeSupport` | `Built-in theme is rejected by GLTheme language: default` |

- [ ] Nach den Korrekturen vollständige Core- und EasyKnack-Testläufe durchführen;
  übersprungene Tests und verbleibende Einschränkungen separat berichten.

### Kurzer manueller Regressionstest

- [ ] Frischer Programmstart: Ladeeinträge für Editor und Schriftatlas sichtbar;
  erste Formula-Gruppe und eine weitere Gruppe ohne auffällige Pause öffnen.
- [ ] ESC direkt nach Öffnen, nach Klick in den Code und nach Klick außerhalb:
  zurück zur Gruppe. Bei offener Vervollständigung zuerst diese schließen;
  bei Inline-Umbenennung zuerst diese abbrechen.
- [ ] Im Editor `N`, Entf, Strg+S und Strg+O testen: keine unbeabsichtigten
  Workspace-Aktionen; Texteingabe, lokale Bearbeitung und Undo bleiben intakt.
- [ ] Lange Autovervollständigung mit Pfeil runter/hoch durchlaufen: Auswahl
  vollständig sichtbar, Scrollen in beide Richtungen, letzter ↔ erster Eintrag,
  kein Scrollsprung innerhalb des sichtbaren Bereichs; Enter übernimmt richtigen Vorschlag.
- [ ] Dieselbe Navigation bei kleiner Fensterhöhe prüfen und anschließend mit
  Mausrad/Maus auswählen; normale Listen und Popup-Untermenüs gegenprüfen.

## Produkt-Roadmap (29.09.2026)

Die gemeinsam besprochenen Erweiterungen mit Reihenfolge, Abhängigkeiten und
Abnahmekriterien stehen in [docs/PRODUCT-ROADMAP.md](docs/PRODUCT-ROADMAP.md).
Dies ist der Einstieg für neue Produktfunktionen; bestehende DSP-Aufgaben und
offene Nachweise bleiben in ihrem eigenen Fahrplan erhalten.

- [ ] 1. Audio-Datei-Player (WAV, MP3, FLAC, Ogg Vorbis, AAC/M4A), Recorder und Offline-Render.
  Player als erste Version umgesetzt: [Bedienung und Grenzen](docs/Audio-File-Player.md).
  Recorder, Offline-Render und portable Medienverwaltung bleiben offen.
- [ ] 2. Transport, Tempo und synchronisierte Zeitsteuerung.
- [ ] 3. Gruppenbibliothek, Presets und Makroregler.
- [ ] 4. Dynamik-Nodes und Verbesserung der vorhandenen Pitch-Werkzeuge.
- [ ] 5. Performance-Ansicht und Latenzausgleich.
- [ ] 6. Automation, Instrumente, spektrale Werkzeuge und weitere Analysen.
- [ ] Begleitend: Editor-Komfort, Wiederherstellung und Plugin-Verwaltung.

Die Einträge sind geplant, nicht als implementiert zu verstehen. Vor jeder Phase
den aktuellen Code und die vorhandenen Beispiele prüfen; vorhandene Funktionen
erweitern statt parallel neu bauen.

## Oszillator-Reset

- [x] Oscillator und Complex Oscillator: samplegenauer Reset-Trigger-Eingang mit manuellem Button, gemeinsamer Reset aller Teiltöne, Phase einmal als Offset; Live, Sample-Feedback und Bake-Kerne angepasst. Details und Tests: docs/OSCILLATOR-RESET.md.

## Gruppen: wartet auf manuellen Test durch den Nutzer

- [ ] Harmonizer mit den neuen Group-IO-Einstellungen prüfen: Inputs/Reihenfolge,
  Slider, Combobox-Modelle, Copy Editor ohne zusätzliche Vorlagen, Trigger-Buttons,
  versteckte äußere Anschlüsse, Undo, Speichern/Laden und Bake.
- [x] Plugin-Export: Reihenfolge, Combobox-/Copy-Modelle, Einheiten,
  Checkboxen, versteckte Anschlüsse und Trigger-Buttons werden übernommen.
  Generierte Java-Klassen und native DSP-Vergleiche sind getestet;
  der oben aufgeführte manuelle Harmonizer-Test bleibt offen.
- [x] Samplegenaues Feedback: Sample Delay, minimale zyklische Teilgraphen,
  explizit unterstützte Prozessoren, Control-/MIDI-Timing, Stereo und Stimmen.
  Weiteres und erste Laufzeitmessungen: `docs/SAMPLE-FEEDBACK.md`.
- [x] Universal Delay: replaces the three catalog entries, migrates old documents,
  Control Time input, automatic sample/chunk/block feedback scheduling, shared
  islands and fixed block-only processor support. See `docs/SAMPLE-FEEDBACK.md`.
- [x] Block/Sample/Universal Delay in Bake/Plugin-Export: gemeinsamer
  Read/Compute/Commit-Plan, auch in verschachtelten Gruppen. Numerische Vergleiche
  mit Live-Graphen, Stereo, Teilblöcken und Zustandsübernahme.
  Feedback wird korrekt geplant, derzeit nicht zusätzlich fusioniert.


## Stand der Grundlagen-Überarbeitung (23.09.2026)

Die nachfolgende Anforderungsliste ist als Referenz erhalten. Umsetzung, neue Nodes,
Verhalten der asynchronen Analyse und Prüfprotokoll stehen in
[docs/FOUNDATION-REFORM.md](docs/FOUNDATION-REFORM.md).

Die Analyse-/History-Ressourcen ergänzen die bisherigen Analyzer. Ein langfristiger,
vergleichbarer Geräte-A/B-Nachweis für Diagnose-Polling bleibt eine eigene Messaufgabe;
ein DSP-Peak allein ist weiterhin kein Nachweis eines Audioaussetzers.

## am besten sofort umsetzen {

- Allow mixed ProcessingRates on outputs of the same processor.
- Example: DigitalNoiseNode needs Factor = AudioRate and Trigger = EventRate.
- Resolve/schedule rates per output without executing the processor redundantly.

## 1. DSP lifecycle / realtime safety

### Add prepare lifecycle

* Add `prepare(sampleRate, blockFrames)` to `NodeProcessor`.
* Use it for sample-rate/block-size dependent setup outside the audio thread.
* Remove hardcoded sample rate from `DelayNode`.
* Stateful DSP buffers/resources must be allocated/prepared outside `process()`.

### Do not rebuild processors for parameter changes

* Slider/property changes must update live processor parameters without rebuilding the graph.
* Stateful nodes must retain DSP state while parameters change.
* Real graph rebuilds should happen only for structural graph/layout changes.
* If state transfer is required, large DSP state must not be copied on the audio thread.

---

## 2. Analysis architecture

Refactor analyzers into reusable analysis resources instead of combining analysis + GUI in one node.

Target direction:

```text
Audio
  ↓
Analysis Window / History
  ↓
FFT / Spectrum Resource
  ├─ Magnitude
  ├─ Phase
  ├─ Power Spectrum
  └─ Autocorrelation
         ↓
   Viewer / Peak / Statistics / Export
```

Requirements:

* Typed analysis resources.
* No large buffer copies through normal Audio/Value/Event ports.
* Reuse/cache FFT results instead of recomputing them downstream.
* Support derived/lazy analysis views.
* Separate mathematical analysis from rendering.
* Generic `AnalysisViewer`.
* Autocorrelation resource should expose full positive lags plus symmetric metadata.
* Metadata should include sample rate, lag spacing/window size and estimator type.
* Analysis resources should also be consumable by statistical nodes such as Peak, Mean, RMS, Integral, Periodicity and Correlation Time.
* Expensive analysis should eventually run on a reusable analysis worker, never block the audio thread.
* Preserve current analyzer nodes during migration.

---

## 3. Resource connections / shared DSP state

Introduce typed resource ports for large shared state.

Initial use case:

```text
HistoryBuffer → RESOURCE<HISTORY> → HistoryReader
```

Requirements:

* Resource ports are separate from Audio/Value/Event/MIDI.
* Connections carry handles/references, not copied buffers.
* Resource lifetime owned by compiled graph.
* Allocate/prepare resources outside realtime processing.
* Resolve connections to direct pointers/handles during graph compilation.
* No allocation or string lookup inside `process()`.
* Multiple readers may share one resource.
* Preserve resources across graph rebuilds where possible.
* Serialize resource connections.
* Type-check resource connections.
* Render resource cables distinctly.

Initial type:

* `HISTORY`

Future examples:

* `WAVETABLE`
* `SAMPLE`
* `IR`
* analysis resources

Initial nodes:

* `HistoryBufferNode`: Audio → HISTORY resource
* `HistoryReaderNode`: HISTORY + speed + offset → Audio, fractional interpolation

---

## 4. Property/default model

### Track automatic vs explicit property values

Properties need provenance.

Example:

* New Multiply: B=1 automatic.
* Switch to Power: B becomes 2 if still automatic.
* User edits B=3: mark explicit.
* Switching operations must preserve explicit B=3.
* Hidden operands retain explicit values; automatic ones may adopt the new default when shown again.

Requirements:

* Persist automatic/explicit state.
* User edits mark explicit.
* Defaults/migrations do not.
* Dynamic layouts may update only automatic values.
* Support reset-to-auto.
* Implement generically in the property/model layer.

### Separate structural variants from editor metadata

`NodeRegistry` must not create new node variants because presentation/default metadata changes.

Structural variant:

* ports
* routing
* actual editor structure/capability

Not structural:

* labels
* tooltips
* default values
* slider min/max
* other presentation metadata

Unary/binary Math changes may still require variants because the port layout changes.

Add regression coverage for:
`Multiply → Power → Add` without unnecessary node variants.

---

## 5. Editor / ComboBox improvements

Add proper presentation structure to ComboBox editors:

* separators
* submenus/groups
* selectable items remain separate from presentation rows

Use `MathOperation.group()` to group Math operations.

Do not fake separators or groups as enum values.

---

## 6. Analysis rendering

Make `AnalysisEditor` rendering viewport/zoom aware.

Current behavior:

* large datasets are rendered with Min/Max bucketing
* bucket resolution currently follows local control width rather than actual visible screen resolution
* zooming therefore exposes coarse bucket columns

Requirements:

* derive effective visible pixel resolution from GLSwing canvas/viewport zoom
* adapt `drawLinear()` and `drawSymmetric()`
* use Min/Max buckets when multiple data points map to one screen pixel
* draw individual samples when sufficient screen resolution exists
* never reduce the underlying `AnalysisFrame` data
* preserve exact symmetric autocorrelation data

---

## 7. Diagnostics / telemetry

Periodic diagnostics refresh causes large reported DSP spikes, although no audible underruns are currently observed.

Investigate:

* distinguish render-time spikes from actual audio xruns/underruns
* add explicit underrun/dropout counters
* ensure diagnostics aggregation does not interfere with realtime audio
* reduce unnecessary allocations/sorting in diagnostics snapshots if needed

Do not treat reported DSP peak alone as an audio dropout.

---

## Suggested order

1. `prepare()` lifecycle
2. parameter updates without graph rebuild
3. automatic vs explicit property provenance
4. structural vs presentation metadata / node variants
5. ComboBox separators/submenus
6. typed Resource connections
7. reusable analysis resources + AnalysisViewer
8. analysis worker
9. zoom-aware analysis rendering
10. diagnostics cleanup

## }


## Nächster geplanter Arbeitsablauf

Verbindliche Reihenfolge der nächsten DSP-Arbeiten: **Messanzeige → Referenzprojekte → weitere Fusion → Produktionsparallelisierung** (ursprüngliche Punkte **2 → 1 → 3 → 4**).
Der ausführliche Plan mit Teilaufgaben, Messbedingungen und Freigabekriterien steht in [docs/DSP-ROADMAP.md](docs/DSP-ROADMAP.md).
Phase A ist umgesetzt: strukturierte Render-Telemetrie, aufklappbare GL-Diagnose, Verlauf, getrennte Ringbuffer-/ASIO-/Priming-Zähler, Reset und CSV-Bericht. Audit und Messsemantik: [docs/RENDER-TELEMETRY.md](docs/RENDER-TELEMETRY.md).
Phase B: versionierte Referenzprojekte, Original-/Plugin-/Scheduler-Vergleiche und ein reproduzierbarer Geräte-Langlauf sind ergänzt. Anleitung: [examples/reference-v1/README.md](examples/reference-v1/README.md); Baseline und nächste Prüfpunkte: [docs/DSP-BASELINE.md](docs/DSP-BASELINE.md). Als Nächstes folgt Phase C, insbesondere die durch Messung auffällige Kanalbehandlung an Exportgrenzen vor weiterer Fusion. Produktionsparallelisierung folgt danach.
Dabei bleibt der A4-Nachweis unveränderter Spitzenlatenz unter realer Last offen: die kurze Offline-Telemetriemessung zeigt geringe Schreibkosten, aber schwankende p99-/Maximalwerte und einen Budgetausreißer. Auch nominale Geräte-Langläufe mit aktiver Telemetrie ersetzen keinen direkten An/Aus-Vergleich. Details und Rohdaten stehen unter `native/audio-host/benchmarks`.

## Weitere Aufgaben

- Stereo-Pegelanzeigen in Ein- und Ausgangsnodes: Peak-Hold, Übersteuerungsanzeige und Sichtbarkeit bei Hide.
- Limiter-Node zum Abfangen lauter Filmstellen.

- DSP-Parallelisierung anhand Messungen entscheiden: Renderzeit pro Zweig und Stimme, Deadline-Maxima bei 128/256 Frames, 1/8/32 Stimmen sowie Stereo erfassen. Erst bei messbarem Gewinn feste Worker und vorgeplante unabhängige Aufgaben verwenden; kein Thread-Erzeugen, Allokieren oder unbeschränktes Warten im Callback. Mono-Kanalwiederverwendung erhalten; Deadline-Verhalten und CPU-Last vergleichen.

## Erledigt: Java/C++-Parametervertrag und Analysen

- `ParameterContract` deklariert Quellen und Werte über `PropertyId`; keine handgeschriebenen Payloads oder separat gepflegten Eingangslisten in den Built-in-Nodes.
- Maven generiert benannte C++-Parameterklassen inklusive Grenzen, Enums und Vertragskennung. Hostprotokoll 10 prüft Version und Schema. Native Legacy-Leser bleiben für ältere Test-/Diagnose-Payloads erhalten.
- Oszilloskop mit Low-/High-Trigger und Zeitfenster; Autokorrelation mit Graph, Hz und Konfidenz. Live-Anzeigen verwenden begrenzte SPSC-Puffer.
- Gruppenquellenexport über den Dateidialog: normale Java-Nodeklasse, statischer C++-Prozessor und editierbare Quelldatei. Verschachtelung, öffentliche Slider/Einheiten und tote Zweige sind berücksichtigt.
- Rate Converter mit First/Last, Mean, RMS, Minimum/Maximum und Absolute peak für Audio → Control sowie Audio-/Wert-Event-Ausgängen.
- Quellenexport mit MIDI-Verarbeitung, interner/externer Polyphonie, Merge und kanalübergreifendem Routing. Fester typisierter Aufrufplan verwendet die gemeinsame Host-Laufzeit für Event-/Stimmen-/Kanalpuffer; native Vergleichstests prüfen Original und Export.

## Umgesetzt: erste Version des Plugin-Systems

- Ein einziges öffentliches Quellpluginformat `.knackplugin` mit Java-/C++-Quellen und Metadaten. Exportierte Gruppen können darüber als feste Nodes installiert werden; Quellen bleiben manuell bearbeitbar.
- Beim Import einmal im Hintergrund kompilieren und nach erfolgreichem Build registrieren. Programmstarts laden gültige Builds direkt aus dem Cache, ohne erneute Kompilierung.
- Cache anhand von Quellinhalt, Zielplattform, Compiler-/Buildoptionen und relevanter SDK-/Schnittstellenversion prüfen. Bei ungültigem Cache neu bauen; vorhandene inkompatible Builds nicht laden.
- Explizite Aktion „Plugin neu bauen“ für bearbeitete Quellen. Fehlgeschlagene Builds ersetzen keinen bisherigen kompatiblen Build; verständliche Builddiagnosen anzeigen.
- Aktuell Windows x64 mit JDK und LLVM-MinGW. Vorhandene Toolchains werden gefunden; C++-Pfad ist über das Plugins-Menü konfigurierbar. Der Cache enthält gemeinsam kompilierten Host und Rechner statt einer DLL-Schnittstelle; SDK und Parameterverträge werden geprüft. Neue Nodes sind direkt verfügbar, Änderungen bereits geladener Nodes werden nach Neustart aktiviert.
- Später: Toolchain mitliefern oder einmalig herunterladen, Cache-Bereinigung, Plugin-Deinstallation und deklarierte Abhängigkeiten zwischen Plugins. Aktuell benötigte Pluginquellen gemeinsam installieren, bevor davon abhängige Gruppen gebaut werden.
- Später: zusätzlich bereits fertig kompilierte Plugins importieren können, ohne lokale Kompilierung. Dies ist keine Voraussetzung für die erste Version und kein zweiter Installationsweg in deren Oberfläche.

## Weitere Export-Ausbaustufen

- Geschlossene Oszillator → Multiply-Gain → Filter-Gruppe erhält einen spezialisierten Plan ohne innere Scheduleraufrufe; gemeinsame Sampleschleife mit einmal pro Block vorbereiteten Glättungskoeffizienten, direkte Ausgabe in den Gruppenpuffer, gleiche DSP-Zustände und Fallback bei abweichender Rate/Kanalbelegung. Vergleich gegen Original, bisherigen Export, getrennte Schleifen und beide handgeschriebenen Varianten unter `native/audio-host/benchmarks/specialized-chain-2026-09-18`. Noch offen: sichere Erweiterung auf verbundene Modulationsports, Teilabschnitte, Fanout, weitere Prozessoren und Stimmen; den gemessenen Gewinn nicht auf solche Graphen übertragen.

- Exportpläne nutzen jetzt optionale inline spezialisierte Math-/Filter-Blockroutinen; Oszillator-Inlining wegen Stereo-Regression zurückgenommen. Vollständige Oszillator–Gain–Filter-Gruppe zur Referenzsuite ergänzt. Produktionsmessung und Grenzen: `native/audio-host/benchmarks/export-inline-2026-09-18`. Der Mikrobenchmark-Gewinn ist nicht pauschal auf vollständige Gruppen übertragbar; weitere Spezialisierung nur mit vollständigem Graphvergleich.

- Zustandsbehaftete Fusion untersucht: Oszillator → Gain → Filter gewinnt im isolierten Versuch durch Spezialisierung/Inlining etwa 19–21 %, zusätzliche Schleifenfusion bringt gegenüber der Inline-Kontrolle keinen Gewinn. Erste Produktionsintegration und vollständiger Graphvergleich siehe oben; gemeinsame skalare DSP-Bausteine bleiben eine weitere Ausbaustufe, kein pauschaler Fusionszwang. 48 bitgleiche Vergleichsfälle und Maschinencode unter `native/audio-host/benchmarks/stateful-fusion-2026-09-18`.

- Export-Restaufwand reduziert: direkte Ausgabe in eigene Kanal-Caches und kein erzwungener äußerer Cross-Channel-Plan bei ausschließlich MIDI-/unverbundenen Eingängen. Vier Vergleichsvarianten einschließlich Kopierpfad bleiben bitgleich. Synth-Referenzen ungefähr auf Originalniveau; echte Synth-Beschleunigung durch weitere Fusion weiterhin offen. Messwerte: `native/audio-host/benchmarks/export-overhead-2026-09-18`.

- Kanalbehandlung an Exportgrenzen korrigiert: MIDI-Eingänge werden intern gemeinsam berechnet; bekannte Mono-Ausgänge bleiben auch außerhalb des Plugin-Nodes Mono. Referenzvergleiche bleiben bitgleich. Noch offen: äußere Mono-/Stereo-Belegung numerischer Eingänge in den inneren Exportplan übernehmen; diese bleiben derzeit konservativ Stereo. Messwerte unter `native/audio-host/benchmarks/channel-fix-2026-09-18`.

- DSP-Fusion umgesetzt: aufeinanderfolgende Math-Abschnitte in reinen und gemischten Gruppen verwenden eine gemeinsame Sampleschleife mit lokalen Zwischenwerten; nach außen benötigte Zwischenwerte bleiben erhalten. Untergruppen werden vorab aufgelöst, uniforme Mono-/Stereo-Audio-Abschnitte sind unterstützt. Vergleichstests gegen den Scheduler für Ratenwechsel, kurze Blöcke, Stereo, numerische Sonderfälle, Zustandsübernahme, Untergruppen, Filter, Kanaltrennung und Fanout; synthetischer Benchmark ohne garantierten Mindestgewinn.
- Exportgrenzen weiter ausbauen: mehrere unabhängige Stimmen-Domänen an einem Node, Audio-Geräte-I/O und automatische Auflösung gemuteter interner Nodes. Aktuell müssen unabhängige öffentliche Stimmen vorher gemergt werden; unterschiedliche numerische Ausgangsraten werden inzwischen portweise erhalten (siehe FOUNDATION-REFORM.md).
- Weitere Compileroptimierungen und vergleichbare Benchmarks gegen handgeschriebene DSP-Knoten. Konkrete native Aufrufe erlauben Inlining; eine identische Laufzeit wird nicht vorausgesetzt.
- Weitere Fusions- und Speicherplanungsschritte siehe den aktuellen DSP-Optimierungsstand unten. Glättung, numerische Schutzregeln, MIDI-Zeitpunkte sowie Stimmen-/Kanalzustände weiterhin erhalten.

## Umgesetzt: weitere DSP-Optimierungen (September 2026)

- Exportfusion auf Math, Mix, MapRange und Unit Converter erweitert; konstante Ausdrücke vorab berechnen und identische Ausdrücke innerhalb eines Abschnitts wiederverwenden. Keine algebraische Umordnung; Scheduler-Fallback für Glättung, Control/Event und Stimmen erhalten.
- Reine Math-Abschnitte verwenden SSE2 für vier Samples gleichzeitig mit skalarem Rest. Sonderwerte einschließlich negativer Null gegen skalare Auswertung getestet.
- Numerische Kanal-Caches des normalen Hostplans anhand ihrer Lebensdauer wiederverwenden, einschließlich Fanout und Stimmen. Generierte Fusionspläne behalten eigene Caches.
- Complex-Oscillator-Spektrum: gemeinsamen Detuning-Faktor einmal berechnen und unveränderte Obertongewichte wiederverwenden.
- Reproduzierbarer Offline-Vier-Worker-Vergleich für 1/8/32 Stimmen, 128/256 Frames, Mittelwert/p99/Maximum/Deadline-Verletzungen unter `native/audio-host/benchmarks`. Noch keine Produktionsparallelisierung: begrenztes Verhalten bei verspäteten Workern, sichere Zustands-/Graph-Lebensdauer und reale Audiogerätetests sind offen. Ein günstiger Durchschnitt allein reicht nicht.
- Noch offen: Fusion nicht benachbarter Abschnitte durch sichere Umordnung, gemischte Kanalbelegung, Control-/Event-/Stimmenfusion und Speicherplanung innerhalb generierter Fusionskerne. Neue Compileroptimierungen erfordern einen neuen Gruppenexport; manuell bearbeitete Pluginquellen werden beim Neubauen nicht überschrieben.


## Spectral Groups: weitere Ausbaustufen

- Spectral Groups bleiben ein eigener Ausführungskontext mit einem Spectrum-Eingang und optionalem Spectrum-Ausgang. Numerische Audio-/Control-Eingänge sind möglich (maximal 16); Audioausgänge über Instance Combine sind ebenfalls möglich.
- Umgesetzt: externe Control-Eingänge übernehmen den Blockwert am Ende des FFT-Fensters und halten ihn für alle Bins.
- Umgesetzt: Audio-Modulation übernimmt das letzte Sample des FFT-Fensters; vorab reservierte Historie (131072 Samples pro benutztem Eingang) ordnet auch asynchrone Fenster zeitlich zu. Zu alte Fenster werden verworfen. Später weitere Abtastoptionen wie Mittelpunkt oder Mittelwert anbieten.
- Mehrere Spectrum-Eingänge für spektrales Mischen/Modulieren: Fenstergröße, Hop, Sampleposition, Kanal- und Stimmenzuordnung sowie Verhalten bei fehlenden Frames definieren.
- Umgesetzt: Ausführungsbereiche Selection, FFT / Bin, Audio / Bin, Combine und Shared im Editor kennzeichnen. Die Port-Raten selbst sind weiterhin Audio/Control; eine eigene visuelle Bin-Rate bleibt offen.
- Umgesetzt (27.09.2026): automatische Bin-Anzahl, persistente Oscillator-/Noise-/Digital-Noise-Stimmen, mehrere Instance Combines und gemeinsame Nachverarbeitung. Bin Discard prüft zuerst seine Abhängigkeiten, überspringt übrige Frame-Arbeit und vollständig ausgeblendete Audiostimmen. Bin Read liefert zusätzlich normierte Amplitude/Frequenz. Editierbares Beispiel im Dateimenü; Laufzeit-/Exportvergleich, Zeitzuordnung, Normierung und Zustandsübergabe getestet. Details: `docs/SPECTRAL-GROUPS.md`.
- Offen: spezialisierte Fusion des spektralen Audiokerns für Bake/Export, weitere Zustandsprozessoren mit klaren Discard-/Tail-Regeln und eine wiederverwendbare Hysterese für Schwellwertentscheidungen. Aktuell teilen Bake und Live denselben vorbereiteten Spektralkern; keine pauschale Beschleunigung durch Bake versprechen.
- Phasentreue additive Resynthese: FFT-Phase vom Fensterbeginn zum Renderzeitpunkt fortschreiben, Sinus-/Kosinuskonvention berücksichtigen und Frameübergänge kontinuierlich behandeln. Der normale Oscillator hat jetzt einen generischen Phase-Offset-Eingang; die direkte Verbindung von Bin Read.Phase allein ersetzt diese Synchronisation nicht.


## Multiple Groups: weitere Ausbaustufen

- Numerische Multiple Groups sind umgesetzt: eigene Instanzzustände, Stereo, fester Count-Slider außerhalb der Gruppen-IOs, vorbereitete Kapazität, mehrere Instance-Combines (Sum/Mean/Weighted Mean), gemeinsame Nachverarbeitung und Ein-/Ausblenden; siehe docs/MULTIPLE-GROUPS.md.
- Verschachtelte Multiple-/Spectral-Ausführungskontexte mit eindeutiger Context-Zuordnung, Gesamtbudget und Zustandsübergabe definieren. Normale Untergruppen sind bereits möglich.
- MIDI-/Ressourcen-Gruppenports und Ereignis-Ausgänge brauchen eigene Verteil-/Merge-Regeln statt numerischer Summierung.
- Rate- und Mono-Weitergabe im Instanzruntime korrigiert (MULT3, ältere Programme bleiben lesbar); Reduktionsschleifen vereinfacht. Reproduzierbarer Benchmark: `InstancePerformanceTest` und `native/audio-host/benchmarks/InstanceBenchmark.cpp`.
- Teilweise umgesetzt: `InstanceSourceSpecialization` erzeugt für direkt exportierbare numerische Instance-Gruppen denselben spezialisierten C++-Kern für Bake und Export, ohne äußeren Boundary-Graph. Math-/Unit-/Context-Control-Ausdrücke werden mit festen Operationen und lokalen Zwischenwerten in die konkrete Instanzklasse kompiliert; die gemeinsame Reduktionsschleife wird dafür templatisiert. Noch offen: beliebige Instanzprogramme, Audio-Modulation, weitere Prozessoren und instanzunabhängige Teilberechnungen gemeinsam ausführen.
- Umgesetzt: konservativer nativer Instance-Reduktionskern für Mono-Oszillator → einzelnes Instance Combine mit Control-Vorberechnung. Keine Audio-Zwischenpuffer, gemeinsame Lookup-Funktion, lokale Phase, Gewichtung/Fades direkt in die Summe; live und Bake profitieren. Audio-Modulation, Stereo, mehrere Combines, Feedback und andere Zustandsprozessoren bleiben im Scheduler. Vergleichstest deckt Pfadwechsel und Nullgewicht ab; Messung siehe `native/audio-host/benchmarks/instance-reduction-2026-09-26`.
- Discard: im Reduktionskern spart exakt null Weight den Lookup bei weiterlaufender Phase. Manuelle Schwelle über Comparator/Math möglich. Allgemeines Überspringen leiser Instanzen erst mit definierten Signalgrenzen, Summen-/Normierungsfehlerbudget und Zustands-/Tail-Regeln; ein kleiner Weight-Wert allein reicht nicht. Instanzübergreifendes Teilen unabhängiger Control-Ausdrücke bleibt offen.

## Bake-Kernel (26.09.2026)

- Noise und Digital Noise: automatischer Seed pro logischer Node-ID statt pro Kanal-/Instanzkopie; normaler Noise hat einen Control-Seed-Port und ist ohne kanalabhängige Eingänge mono. Instance-Gruppen teilen Noise-Blöcke nur bei exakt gleichem Generatorzustand, Parametern und Eingängen (inklusive Audiomodulation bei Digital Noise). Sample-Feedback bleibt getrennt. Dies teilt die Generatorberechnung, nicht automatisch den gesamten vorgeschalteten Graphen.
- Instance-Gruppen ohne externe Verbindungen geben jetzt ihr tatsächliches internes Mono-Layout weiter (live und Bake); verbundene Grenzen bleiben konservativ stereo. Bake-Control-Ergebnisse werden anhand von Eingangswerten, Instanzkontext und Samplerate zwischengespeichert; reine Math-Teilrechnungen teilen einen operandenabhängigen Cache über Instanzen. Der Reduktionsplan wird bei stabiler Belegung wiederverwendet. Fallback, prepare und Zustandsübernahme invalidieren die betroffenen Caches. Die echte Audiohost-Auslastung weiterhin gegen die Nutzermessung prüfen.
- Offen: Nutzer misst weiterhin ca. 4 % für Instance (mit/ohne Bake) gegenüber ca. 2 % Complex. Die letzten lokalen Cache-Quellen enthalten den neuen Audiokern; der Offline-Benchmark erklärt die Echtzeitwerte bisher nicht. Als Nächstes den tatsächlich gestarteten Host samt Renderpfad/Fallback-Grund unter identischer Geräte-/Blockkonfiguration messen, statt weitere Gewinne aus dem isolierten Benchmark abzuleiten. `Force rebuild` erzeugt jetzt eine frische Build-/Host-Identität; Mute/Unmute invalidiert und regeneriert im Regressionstest korrekt.
- Instance-Bake/Export verwendet zusätzlich `CompiledInstanceAudio`: kompakte Phasen-/Increment-/Gewichtsarrays, eine gemeinsame Sample-/Teiltontabelle-Schleife, ein Mono-Summenpuffer und blockkonstante Normierung. Kanäle/Raten und Control-Ausdrücke werden weiterhin blockweise vorbereitet; der allgemeine Pfad bleibt für Count-Fades, Audio-/Stereo-Modulation und nicht unterstützte Graphen. Kein Versprechen einer vollständigen Spezialisierung beliebiger Instance-Gruppen.
- Gemischte Control-/Audio-Math-Regionen rechnen Blockwerte einmal; SIMD nur für implementierte Operationen.
- Lineare numerische Ketten können vorhandene native Sample-/Prozessorkerne direkt in einer Schleife aufrufen. Math/Mix erhalten dabei konstante Operationsargumente; Oszillatoren bereiten konstante Frequenzen einmal vor. Details und Einschränkungen: `docs/DSP-FUSION.md`.
- Weitere Kostenplanung anhand reproduzierbarer Benchmarks: nicht jede Schleifenfusion ist schneller. Insbesondere unabhängige Zweige, Fanout, SIMD und zustandsbehaftete Berechnungen nicht pauschal in eine große Sampleschleife zwingen.


## Bestehende Core-Testabweichungen (25.09.2026)

- Der vollständige Core-Testlauf hat sieben bereits zuvor vorhandene Fehler:
  `GLNodeInsertionTest.exclusionReconnectsAndOnlyClosesSimpleChains` sowie
  `MavenSmokeTest.controlsAndFocus`, `configurationEditors`, `localizationAndThemes`,
  `lists`, `menus` und `themeCodeSupport`. Dieselben Fehler wurden in einer separaten
  Kopie ohne die neuen applicationManaged-Portänderungen reproduziert.
- Theme-/Layout-Erwartungen mit den aktuellen manuellen Core-/Themeänderungen abgleichen;
  dabei die gewünschten Theme-Werte nicht einfach für alte Testannahmen zurücksetzen.
  Vergleichslogs: `target/instance-core.log`, `target/instance-core-baseline.log`.
## Spectral-Performance: weitere Schritte (29.09.2026)

- Umgesetzt: vorbereitete Bin-Kontexte und Typauflösung, einmalige Berechnung
  binunabhängiger Ausdrücke pro Frame, „Skip empty input bins“ und typisierte
  Bin-Schleifen für statische Frame-Programme in Bake und Export. Messungen für
  FFT-Noise stehen in docs/SPECTRAL-PERFORMANCE.md.
- Umgesetzt: reines FFT-Frame-Processing auf zwei unabhängige Bin-Bereiche verteilen,
  ein gemeinsamer dauerhafter Worker, vorbereitete Zustände, Lastschwelle und
  sofortiger serieller Fallback bei belegtem Worker. Group-General erlaubt das
  Abschalten; Bake nutzt denselben Bereichskern. Siehe docs/SPECTRAL-GROUPS.md.
- Weiter offen: allgemeiner paralleler Graph-Scheduler mit gemeinsamen Abhängigkeiten,
  Mono-Wiederverwendung und zeitlichen Stimmenzuständen. Langzeittest unter realer
  Treiberlast/konkurrierender CPU-Last und adaptive Schwellen für andere CPUs.
- Spektrale Codefusion auf dynamische Editorvarianten und zeitliche Per-Bin-Stimmen
  erweitern. Diese nutzen bisher den optimierten allgemeinen Ausführer.
## Formula-Gruppe

- Umgesetzt: Gruppen-Inspector/IOs, Codeeditor, Einheiten und Editorvorlagen, stabile Port-IDs und umbenennbare Codebezeichner, gespeicherte Quellen/Entwürfe und Undo.
- Umgesetzt: geprüfte Float-/Bool-Sprache, Trigger, Auto/Audio/Control, unmittelbare Interpreter-Vorschau, isolierte native Hintergrundkompilierung und Wechsel an Blockgrenzen. Fehler behalten die letzte gültige Fassung.
- Umgesetzt: Bake und Plugin-Export mit generiertem C++ statt Abhängigkeit von lokalen DLLs.
- Später: optionale Überblendung beim Codewechsel sowie repräsentative Performancevergleiche komplexer Formeln.
- Bedienung und Grenzen: [Formula-Gruppen](docs/FORMULA-GROUP.md).
- Formula-Kontext umgesetzt: sampleRate, blockSize, sampleIndex, samplePosition, reproduzierbares rand(min,max[,seed]) und explizite Buffer-Eingänge mit read(index[,wrap[,interpolate]])/size. Vorschau, DLL und Export einschließlich Feedback-Teilblöcken geprüft. Später: begrenzte History für normale Audio-Eingänge.

- Formula-Zustände umgesetzt: state bool/int/uint/long/ulong/float/double mit Literalinitialisierung, maximal 64 Variablen, getrennte Prozessorzustände und Erhalt bei kompatiblen Live-Änderungen (Name/Typ/Anfangswert). Auto rechnet mit Audio-Rate; explizites Control aktualisiert einmal pro Block. Siehe docs/FORMULA-GROUP.md.

- Formula-Typsystem umgesetzt: feste 32-/64-Bit-Typen, C-artige Casts, Bool-Arithmetik, exakter ulong-Samplezähler, definierte Überläufe/Konvertierungen, gemeinsame native Rechenhelfer für Vorschau und DLL/Export; neue Gruppen verwenden Sprachversion 3, alte Formeln bleiben kompatibel.
- Formula sineWave(phase) umgesetzt: vorhandene 2048-Punkte-Sinustabelle, lineare Interpolation, Phase in Perioden, Vorbereitung außerhalb des Audiothreads; sin bleibt mathematisch unverändert.
- Formula-Editorhilfe umgesetzt: Gültigkeitsbereiche und Deklarationsreihenfolge, Typ-/State-Hover, empfängerabhängige Memberhilfe, Funktionssignaturen mit aktivem Parameter, Klammervervollständigung; auch für unvollständigen Code.
- Nächste Formula-Ausbaustufen: eigene Hilfsfunktionen ohne Rekursion; begrenzte state-Arrays und Schleifen mit fester Obergrenze für Filter und kleine Delays.
