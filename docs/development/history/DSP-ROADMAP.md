# DSP-Fahrplan: Messbarkeit, Referenzprojekte, Fusion, Parallelisierung

Stand: 18.09.2026. Phase A funktional implementiert und getestet; Referenzsuite und erste
Baseline aus Phase B ergänzt. Phasen C–D stehen noch aus. Ergebnisse und konkrete
Optimierungsprioritäten: [DSP-BASELINE.md](../DSP-BASELINE.md).
Offener A4-Nachweis: unveränderte Spitzenlatenz unter realer Routing-Last. Der kurze
Offline-Vergleich reicht dafür nicht aus; dieser Punkt wird mit den Baselines in B geprüft.
Audit und genaue Messsemantik der Umsetzung: [RENDER-TELEMETRY.md](../RENDER-TELEMETRY.md).
Vom Nutzer bestätigte Reihenfolge: **2 → 1 → 3 → 4** der ursprünglichen Vorschläge.
Jede Phase liefert ein separat prüfbares Ergebnis. Erkenntnisse dürfen die konkrete
Optimierungsauswahl verändern, nicht stillschweigend die Reihenfolge überspringen.

## Ausgangspunkt

- `AudioTiming.hpp` erfasst bereits geglättete Auslastung, bisheriges Maximum und
  Blöcke über dem rechnerischen Zeitbudget. Das ist keine Messung der Gesamtlatenz.
- `STATS` / `AudioLoad` liefern diese Werte; `AudioHostService` fragt sie alle 250 ms ab.
- `IOSTATS` und die Backends zählen bereits transportierte Frames und Ringbuffer-
  Unter-/Überläufe. Diese sind noch keine einheitliche, nach Ursache getrennte Diagnose.
- Exportfusion für Math/Mix/MapRange/Units, Konstantenauswertung, Ausdruckswiederverwendung,
  Math-SIMD und Host-Cache-Wiederverwendung sind vorhanden.
- Der Vier-Worker-Benchmark läuft nur offline. Mittelwertgewinne und verschlechterte
  Ausreißer sind dokumentiert unter `native/audio-host/benchmarks/README.md`.
- EasyKnack bleibt Maven-Anwendung mit externer GLSwing-Bibliothek.

## Phase A — Anzeige und Messgrundlage erweitern (ursprünglich Punkt 2)

### A1. Messbegriffe und Erfassungsstellen festlegen

- Die Windows-/ASIO-Pfade und Vorberechnung prüfen: Wo beginnen/enden DSP-Berechnung,
  Render-Auftrag und Gerätebedienung? Pro Messgröße Einheit, Zeitfenster, Quelle und
  verantwortlichen schreibenden Thread dokumentieren. Bestehende Aufrufstellen von
  `AudioTiming.observe` auf Mehrfachzählung und konkurrierende Schreiber prüfen.
- DSP-Last = DSP-Rechenzeit / Dauer der tatsächlich berechneten Frames. Zusätzlich
  Render-/Callback-Gesamtzeit nur dort erfassen, wo der Messbereich eindeutig ist.
- Getrennte Zähler: DSP-Budgetüberschreitungen; tatsächlich verspätete Render-Ergebnisse,
  sofern im jeweiligen Backend erkennbar; Wiedergabe-Unterläufe; Capture-Unterläufe;
  Capture-/Queue-Überläufe. Unbeobachtbare Werte als nicht verfügbar ausweisen.
- Startup, Priming, Pause und Gerätewechsel unterscheiden. Betriebsfehler getrennt von
  erwartbaren Anlaufzuständen zählen; keine Ereignisse stillschweigend verwerfen.
- Tatsächliche Frames, Samplerate, Backend und Sitzung an Messungen binden. Angaben zu
  Queue-Füllung beschreiben Pufferdauer, nicht automatisch Ende-zu-Ende-Latenz.

### A2. Telemetrie und Protokoll

- Vorab angelegte, begrenzte Telemetrie pro schreibendem Render-/Backend-Thread;
  keine Strings, Dateizugriffe, Heap-Allokationen oder blockierenden Locks im Audiopfad.
- Aggregate außerhalb des Audiopfads zusammenführen. Überlauf der Telemetrie selbst
  erfassen; die Diagnose darf Audio niemals ausbremsen.
- Mittelwert, Fenstermaximum und p99 aus echten Blockmessungen gewinnen, nicht aus
  den vier UI-Abfragen pro Sekunde. Bei Histogrammen Genauigkeit/Buckets dokumentieren.
- Fenster: eine Sekunde für aktuelle Kennzahlen; 60 Sekunden Verlauf mit 250-ms-Buckets,
  deren Maxima kurze Spitzen erhalten. Zusätzlich Maximum und Zähler seit Messbeginn.
- Strukturierten Java-Snapshot statt alleiniger Statuszeichenfolge einführen. Versionierte
  Erweiterung des Diagnoseprotokolls; alte/fehlende Felder erkennen und verständlich behandeln.
- Reset über Sitzung/Epoche beziehungsweise bestätigten Steuerauftrag; GUI schreibt nicht
  direkt in Audio-Thread-Zustand. Host-Neustart und Gerätewechsel markieren neue Abschnitte.

### A3. Oberfläche

- Kompakte bestehende DSP-Anzeige beibehalten; anklickbare Detailansicht ergänzen.
- Detailansicht: mittlere Last, Fenster-Peak, p99/Blockzeit, Sitzungsmaximum, tatsächliches
  Zeitbudget und nach Ursache benannte Fehlerzähler. DSP-Last darf über 100 % erscheinen.
- Verlauf mit 100-%-Linie, Last-/Peak-Darstellung und Ereignismarkern. Farben aus dem
  EasyKnack-Theme; Beschriftungen Englisch über die vorhandene Textabstraktion.
- Aktionen: Messung zurücksetzen und Diagnosebericht speichern. Bericht enthält Messwerte,
  Backend/Frames/Samplerate, Build-/Plugin-Versionen und Messbedingungen; keine Audiodaten.
- Bei gestopptem/getrenntem Host Werte als gestoppt/veraltet kennzeichnen, nicht als neue
  Nullmessungen. Einheitliche Tooltips erklären Budgetüberschreitung versus Unterlauf.
- Vorhandene GLSwing-Komponenten nutzen. Core nur ergänzen, falls eine allgemein nutzbare
  Darstellungskomponente fehlt; Audio-spezifische Daten und Interpretation bleiben in EasyKnack.

### A4. Prüfung und Abschluss

- Deterministische Tests für Zeitbudget, wechselnde Framezahlen, Fensterwechsel, Peak,
  p99, Reset/Epochen, saturierende Zähler sowie getrennte Ereignisursachen.
- Backendsimulation: absichtlich teurer DSP-Block versus ausgehungerter Eingang versus
  leerer Ausgangspuffer. Erwartete Zähler prüfen, nicht bloß eine rote Lastanzeige.
- UI/Protokoll: Polling-Lücken, Host-Neustart, Pause, fehlende Diagnosefelder, History-Limit.
- Telemetrie an/aus mit demselben Graphen vergleichen: mindestens drei Wiederholungen;
  kein reproduzierbarer Rückschritt außerhalb der Streuung bei p99/Deadline-Zählern.
- Ergebnis: nachvollziehbare Diagnoseanzeige, reproduzierbarer Bericht, vollständiger
  Maven-/nativer Testlauf. Erst danach Phase B.

## Phase B — Repräsentative Projekte und Baselines (ursprünglich Punkt 1)

Umsetzung: acht versionierte `.knack`-Dokumente unter `src/test/resources/reference-projects`, sieben
exportierbare DSP-Szenarien, opt-in Maven-/C++-Vergleich mit isolierter Plugininstallation
und ein separater WASAPI-Langlauf. Die Offline-Messung erfasst Original, Export und
Export ohne Fusion sowie Samplebuffer inklusive eingebetteter Graphen. Reproduktion
und Aussagegrenzen sind in [DSP-BASELINE.md](../DSP-BASELINE.md) dokumentiert.

### B1. Versionierte Referenzprojekte

1. Mathematische Kette: Math, Mix, MapRange, Units, Fanout, Konstanten, Untergruppen.
2. Synthesizer: MIDI Note, Envelope, Complex Oscillator, Filter und Merge; 1/8/32 Stimmen.
3. Stereo-/Kanalprojekt: Context, Pan, Combine/Separate, unabhängige Kanäle und Mono-Sharing.
4. Gemischte Raten: Control/Event/Audio, Konverter, MIDI Delay, gehaltene Werte und Glättung.
5. Analyse und Betrieb: Oszilloskop, Autokorrelation, Reglerbewegungen, Mute, Graphwechsel.
6. Geräte-Routing: vorhandenes VB-Cable → Verarbeitung → reale verfügbare Ausgänge;
   getrennte Geräte-/Clock-Pfade gezielt prüfen.

- Zu jedem Projekt Zweck, erwartetes Verhalten, feste Eingangsdaten/MIDI-Sequenz,
  Exportgruppe und automatisierbaren Testablauf ablegen. Zufall nach Möglichkeit mit Seed.
- Benutzerprojekte nur ergänzend; automatische Tests verändern keine Originaldokumente.

### B2. Vergleich und Messablauf

- Originalgraph und neu exportiertes/installiertes Plugin aus exakt derselben Quelle
  vergleichen; Referenz-Scheduler ohne Fusion als zusätzliche DSP-Referenz erhalten.
- Offline-Matrix: 48 kHz, 128/256 Frames, kurze Restblöcke, Mono/Stereo, 1/8/32 Stimmen,
  Ratenwechsel und Zustandsübernahme. Gleiche Eingaben und Ausgangszustände verwenden.
- Numerisch möglichst bitgleich vergleichen; notwendige Float-Toleranzen vor dem Vergleich
  festlegen. MIDI-Offsets, Ereignisreihenfolge und Zustandsübergänge exakt prüfen.
- Echtgeräte: repräsentative Teilmenge, jeweils Aufwärmphase und mindestens drei Messläufe
  mit je zwei Minuten; fünfminütiger Lauf mit Live-Änderungen für den Betriebsfall.
- Keine parallelen Builds während Messungen. CPU/Energieprofil, Backend, Geräte, Compiler,
  Build, Blockgröße, Telemetriemodus und tatsächliche Laufdauer im Bericht festhalten.
- Mittelwert/p99/Maximum, Budgetüberschreitungen, I/O-Unterläufe und Speicherbedarf getrennt
  auswerten. Absichtliche Überlast von nominalem Betrieb unterscheiden.

### B3. Abschluss und Entscheidung

- Reproduzierbare Klang-/Ereignisgleichheit und stabile Livewechsel sind Voraussetzung.
- Nominale Gerätefälle ohne neue Unterläufe in den wiederholten Läufen; das ist eine
  beobachtete Eigenschaft dieses Setups, keine allgemeine Echtzeitgarantie.
- Engpässe nach gemessenem Anteil priorisieren. Falls nötig, zeitlich begrenzten,
  optionalen Block-Profiler für Zweige/Nodeklassen ergänzen; dessen Eigenkosten ausweisen.
- Ergebnis: Testprojekte, Baselineberichte und begründete Auswahl für Phase C.

## Phase C — Verbleibende Fusion und Buffer reduzieren (ursprünglich Punkt 3)

### C1. Control-Pfade

- Control-only-Abschnitte einmal pro Block fusionieren. Explizite Raten bleiben erhalten.
- Konstanten/Held Values und Control→Audio-Glättung getrennt behandeln; Abtastpunkte und
  Zustandsübernahme erhalten. Grenzübergänge zunächst konservativ im Scheduler belassen.

### C2. Event-Pfade

- Gemeinsame Ereignisabschnitte anhand ihrer tatsächlichen Sample-Offsets planen.
- Gehaltene Werte, gleichzeitige Events, Reihenfolge, Trigger und MIDI-Zeitpunkte erhalten;
  kein pauschales Heruntersetzen von Event auf Control.

### C3. Polyphonie und Kanäle

- Geeignete Abschnitte pro Stimme fusionieren; Zustand jeder Stimme/Kanalkombination
  explizit halten. Mono-Sharing und Voice Merge als semantische Grenzen berücksichtigen.
- Auto-Raten-/Kanaländerungen und manuelle Pluginprozessoren konservativ behandeln.
- Nicht benachbarte Abschnitte erst nach Abhängigkeits-/Seiteneffektanalyse umordnen.

### C4. Speicherplanung im Export

- Lebensdauern anhand des tatsächlichen Fusionsplans bestimmen, nicht anhand des alten
  Whole-Node-Schedulers. Externe Eingänge können während der gesamten Sampleschleife leben.
- Unbenötigte interne Caches entfernen; verbleibende sichere Speicherbereiche teilen.
- Fanout, öffentliche Ausgänge, Analysebeobachter, Event-/Stimmenzustand und Fallback-Pfade
  schützen. Planung und Allokation ausschließlich vor Veröffentlichung des Graphen.

### C5. Freigabe pro Teilstufe

- Jede Teilstufe separat gegen die Baselines aus B vergleichen; nach Möglichkeit gezielt
  deaktivierbar für Diagnose. Keine Fast-Math-/Näherungsänderungen in dieser Arbeit.
- Nur nachgewiesene Speicher-/Laufzeitverbesserungen aktivieren; Schwellen anhand Streuung
  der Baseline definieren. Keine systematische Verschlechterung der p99-/Deadline-Werte.
- Gesamten Maven-/nativen Testlauf einschließlich Export/Import und Livewechseln bestehen.
- Ergebnis: aktualisierte Baselines und dokumentierte verbleibende Engpässe vor Phase D.

## Phase D — Produktionsfähige Parallelisierung (ursprünglich Punkt 4)

### D1. Architektur und Eignung

- Auf Basis der aktualisierten Messungen unabhängige Zweige/Stimmen auswählen. Kleine
  Aufgaben bündeln; bei geringer Last seriell bleiben. Mono nicht künstlich verdoppeln.
- Feste Worker vor Audiostart erzeugen, Tasks/Buffer/Abhängigkeiten vorab planen.
- Gemeinsamen Zustand sichtbar machen: Baked-Runtime-Sharing, Analyse, MIDI, Gerätezugriffe
  und Pluginprozessoren dürfen nicht ohne Nachweis parallel aufgerufen werden. Unbekannte
  Prozessoren zunächst seriell; eine spätere Fähigkeitserklärung muss überprüfbar sein.
- Speicherplanung aus C an echte Parallel-Lebensdauern anpassen; keine Wiederverwendung
  allein aufgrund serieller Nodepositionen.

### D2. Verspätete Arbeit und Graphwechsel zuerst lösen

- Die Benchmark-Barriere nicht direkt in den Callback übernehmen. Render-Ahead-Pipeline,
  verfügbares Zeitbudget und maximal erlaubte zusätzliche Pufferlatenz gemeinsam entwerfen.
- Klare Zustandsmaschine für eingereicht, laufend, fertig, verspätet und stillgelegt;
  Graph/Buffer bleiben gültig, bis alle zugehörigen Worker sicher fertig sind.
- Kein Timeout-Fallback, der denselben veränderlichen DSP-Zustand gleichzeitig erneut
  berechnet. Zustandsfortschritt und Verhalten nach verworfenen Ergebnissen definieren.
- Bei nicht rechtzeitig verfügbaren Ergebnissen begrenztes, definiertes Verhalten des
  Audio-Callbacks; Fehler zählen. Kein unbeschränktes Warten oder Freigeben belegter Daten.
- Stop, Neustart und Live-Graphwechsel einschließlich absichtlich verzögerter Worker testen.

### D3. Prototyp hinter Schalter

- Zunächst diagnostisch aktivierbar, Standard seriell. Threadanzahl und Taskgrenzen anhand
  Messungen wählen; vier Threads sind kein festes Produktversprechen.
- Ausgaben/Zustände gegen seriellen Pfad prüfen. Reduktionsreihenfolge bei Merge erhalten
  oder eine unvermeidliche numerische Abweichung ausdrücklich spezifizieren und testen.
- Nominale Last, Überlast, Fremdlast, kurze Blöcke, Pluginwechsel, MIDI-Panic und Gerätewechsel
  wiederholt prüfen. Workerfehler dürfen keine Nutzung freigegebener Graphen verursachen.

### D4. Produktionsfreigabe

- Wiederholbarer Gewinn bei relevanten Referenzprojekten, einschließlich p99/Deadline-
  Verhalten; kleine Graphen behalten günstigen seriellen Pfad.
- Kein unbemerkter Anstieg von Pufferlatenz oder I/O-Unterläufen; gleiche Klang-/MIDI-
  Semantik. Grenzen der getesteten Hardware und Last transparent dokumentieren.
- Erst dann automatische Auswahl geeigneter paralleler Pläne. Andernfalls bleibt der
  Schalter experimentell; gemessene Probleme und nächster Architekturansatz werden festgehalten.

## Konkreter nächster Arbeitsauftrag

Mit B1 beginnen: versionierte Referenzprojekte und feste Audio-/MIDI-Eingaben anlegen.
Danach Original-/Export-Vergleiche und Baselineberichte gemäß B2/B3 erstellen. Die neue
Render-Telemetrie bildet die Messgrundlage; reale Geräte-/Routing-Langläufe stehen noch aus.
