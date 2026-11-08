# EasyKnack: Produkt-Roadmap

Stand: 29.09.2026. Vom Nutzer zur Umsetzung beziehungsweise Aufnahme in die TODOs
beauftragt. Diese Datei plant die Erweiterungen; sie bestätigt keine Umsetzung.

Ziel: vom interaktiven Node-Experiment zum reproduzierbar spielbaren, aufnehmbaren
und teilbaren Patch. Allgemeine Bausteine bevorzugen, Spezialfunktionen als Gruppen
zusammensetzen. EasyKnack bleibt eigenständiges Maven-Projekt mit GLSwing als
externer Bibliothek. Generische UI-Funktionen gehören in den Core.

## Vorhandenes weiterverwenden

- Step Switch ist die Grundlage für Umschalter und einfache Sequenzen; Clock ist
  vorhanden. Ein gemeinsamer Transport ist eine zusätzliche Aufgabe.
- Pitch Detection mit Frequenzbereich, Pitch Hold mit Konfidenzschwelle, Slew,
  Scale Quantizer und Pitch Monitor sind vorhanden: siehe [HARMONIZER.md](../features/HARMONIZER.md).
  Ein Frequenzbereich allein verhindert keine falschen Subharmonischen.
- Analyseansichten bieten Freeze, lineare/logarithmische FFT-Achsen, Messcursor,
  Zoom, Bildexport und CSV: siehe [Analysis-Inspection.md](../features/Analysis-Inspection.md).
- Gruppen, Quellenexport, Plugin-Import/Buildcache und Bake existieren bereits.
  Bibliothek, Presets und komfortablere Verteilung bauen darauf auf.
- Vorhandene Renderdiagnose und Benchmarks erweitern, keine konkurrierende
  Messsemantik einführen: [DSP-ROADMAP.md](DSP-ROADMAP.md),
  [RENDER-TELEMETRY.md](../RENDER-TELEMETRY.md), [DSP-BASELINE.md](../DSP-BASELINE.md).

## 1. Reproduzierbare Audio-Ein- und Ausgabe

Erste Player-Version implementiert: siehe [Audio-File-Player.md](../features/Audio-File-Player.md).
Mehrformat-Decodierung, gepufferte Wiedergabe, Play/Pause, Reset, Seek und Loop sind
vorhanden. Die Phase bleibt wegen Recorder, Offline-Render, relativen Medienpfaden
und weiterer unten genannter Aufgaben offen.

- [x] Datei-Player: WAV, MP3, FLAC, Ogg Vorbis und AAC/M4A unterstützen;
  WAV allein ist kein abgeschlossener Player. Dateiauswahl, Start/Stop/Reset, Position,
  Loopbereich, Mono/Stereo und definierte Sampleratenanpassung.
- [x] Dateien außerhalb des Audiothreads laden/decodieren; große Dateien über
  begrenztes Streaming bereitstellen. Fehlende Dateien und Unterläufe anzeigen.
- [ ] Gemeinsame Decoder-Schnittstelle mit einheitlicher PCM-Ausgabe an den Host.
  Decoder-Bibliothek vor Umsetzung auf Formatabdeckung, Plattformen, Lizenz und
  Weiterverteilung prüfen. Unterstützte Formate im Dateidialog anzeigen;
  beschädigte oder nicht unterstützte Dateien verständlich melden. Seek und
  Loop-Grenzen auch mit komprimierten Formaten prüfen, inklusive Encoder-Padding.
- [ ] Recorder: WAV-Ausgabe eines gewählten Signals, Start/Stop, Pegel und
  Aufnahmedauer. Dateischreiben über begrenzte Queue; Überläufe sichtbar melden.
- [ ] Offline-Render: Zielausgänge, Dauer, Samplerate, Ausgabeformat und optionaler
  Ausklang; Fortschritt, Abbruch und atomarer Abschluss der Zieldatei.
- [ ] Offline-Ausführung ohne Audiogerät und mit eigenem DSP-Zustand. Regeln für
  Live-Geräte, MIDI-Eingaben, asynchrone Analysen und Zufalls-Seeds explizit machen.
- [ ] Medienpfade relativ zum Dokument unterstützen, fehlende Medien neu zuordnen;
  später portable Projekte mit eingesammelten Medien.

Abnahme: Referenzdateien aller unterstützten Playerformate abspielen, aufnehmen
und offline rendern; bei verlustbehafteten Formaten geeignete Toleranzen verwenden.
Länge, Pegel,
Samplerate und Kanäle vergleichen. Feste Seeds und unveränderte Eingaben liefern
reproduzierbare Ergebnisse. Abbruch hinterlässt keine scheinbar fertige Datei.

## 2. Gemeinsame Zeitbasis und musikalischer Transport

- [ ] Play, Stop, Pause, Rücksprung, BPM, Taktart und Loopbereich.
- [ ] Transport-Context mit Sampleposition, musikalischer Position und Laufzustand;
  dazu samplegenaue Clock-/Reset-Trigger. Control bleibt einmal pro Block.
- [ ] Clock, LFO, Delay und Sequenzen optional ans Tempo koppeln.
- [ ] Tempoänderungen und Neustart eindeutig behandeln; Stimmen/Gruppen erhalten
  dieselbe Zeitbasis. Offline-Render nutzt dieselbe Transportlogik.
- [ ] MIDI-Clock senden/empfangen, inklusive Start/Continue/Stop und Ausfallverhalten.

Abnahme: mehrere Sequenzen laufen ohne Drift, auch über Blockgrenzen hinweg;
Tempoänderungen, Schleifen und Live-/Offline-Ausführung vergleichen.

## 3. Wiederverwendbare Werkzeuge und Gruppenoberflächen

- [ ] Lokale Gruppenbibliothek mit Suche, Kategorien, Beschreibung, Vorschau und
  Beispielen; Import als unabhängige Kopie klar von späteren Updates unterscheiden.
- [ ] Parameter-Presets und A/B-Snapshots mit klarer Trennung von Graphstruktur,
  Parameterwerten und Laufzeitzustand; Undo sowie Speichern/Laden unterstützen.
- [ ] Makroregler mit mehreren Zielen, Einheiten, Bereichen und Kurvenzuordnung.
- [ ] Optionale Gruppenoberfläche mit frei angeordneten Reglern, Schaltern und
  Anzeigen; normale Group-IO-Ansicht bleibt als vollständige Bearbeitungsansicht.
- [ ] Versionierung und Migration für Bibliotheksbausteine; keine stillschweigende
  Änderung bereits gespeicherter Patches durch ein Bibliotheksupdate.

Abnahme: Gruppe inklusive Editoren, Einheiten, Presets und Beschreibung zwischen
Projekten übertragen; Verhalten mit/ohne Bake und beim Plugin-Export vergleichen.

## 4. Dynamik und Pitch

- [ ] Envelope Follower als allgemein nutzbarer Baustein mit Attack/Release und
  Peak-/RMS-Verhalten; vorhandene Pegelanalyse auf Wiederverwendung prüfen.
- [ ] Kompressor, Limiter und Gate mit Sidechain; klare Stereo-Kopplung und
  Gain-Reduction-Anzeige. Der bestehende Limiter-TODO wird hier umgesetzt.
- [ ] Lookahead-Latenz explizit ausweisen und später in Phase 5 ausgleichen.
- [ ] Pitch-Erkennung anhand von Sinus, Obertonreihen, Rauschen, Transienten und
  Frequenzsprüngen vermessen; Oktav-/Subharmonischenfehler gezielt verbessern.
- [ ] Bestehende Hold-/Glättungsbausteine im Harmonizer verständlicher präsentieren;
  Rohwert, gehaltener Wert und Gültigkeit unterscheidbar anzeigen.
- [ ] Notenanzeige um Cent-Abweichung ergänzen, soweit noch nicht vorhanden.

Abnahme: bekannte Signale mit dokumentierten Fehler- und Latenzgrenzen; bei Stille
oder unsicherer Erkennung kein unkontrolliertes Springen. Keine implizite Glättung
aller Ports einführen.

## 5. Nachvollziehbare Kosten und Latenzen

- [ ] Optionale Kostenansicht pro Node/Gruppe mit Messaufwand und Messfenster;
  geteilte Berechnungen, Worker und fusionierte Bake-Kerne korrekt zuordnen.
- [ ] Bake-Bericht: fusionierte Bereiche, Fallbacks, Kanal-/Instanzwiederverwendung
  und gemessener Vergleich. Keine pauschale Geschwindigkeitsgarantie geben.
- [ ] Reproduzierbare Referenzsuite aus Phase 1 für Performance und Audioqualität.
- [ ] Prozessorlatenzen deklarieren und parallele Signalwege automatisch ausgleichen.
  Variable Delays, Feedback und asynchrone FFT-Pfade benötigen eigene Regeln;
  gewollte Delay-Effekte nicht versehentlich wegkompensieren.
- [ ] Gezieltes Oversampling für nichtlineare Prozessoren/Gruppen, mit Filtern,
  zusätzlicher Latenz und Kostenanzeige; Alias-Unterdrückung messen.
- [ ] Stereo-Pegelanzeigen mit Peak-Hold und Clipping, auch bei Hide; bestehenden
  Eintrag in TODO.md als dieselbe Aufgabe behandeln.

Abnahme: Impuls-/Phasentests für parallele Wege, dokumentierte Feedbackgrenzen,
Benchmarks mit aktivierter und deaktivierter Diagnose. Reale Unterläufe weiterhin
von rechnerischen Lastspitzen unterscheiden.

## 6. Musikalische und analytische Erweiterungen

- [ ] Noten-/Pattern-Sequencer mit Swing, Wahrscheinlichkeit, Ratchets und
  Patternwechseln; auf Step Switch und Transport aufbauen.
- [ ] Automation aufnehmen, bearbeiten und wiedergeben; MIDI-Learn, Konfliktregeln
  für manuelle Eingriffe und passende Interpolation je Parametertyp.
- [ ] Sampler mit Looping und Stimmenverwaltung; Wavetable-Import/-Editor; MPE.
- [ ] EQ, zusätzliche Filter, Waveshaping/Saturation, Chorus und Reverb. Vorhandene
  Filter/Curve/Mapping/Delay-Bausteine zuerst prüfen und wiederverwenden.
- [ ] Spektrales Freeze, Formantbearbeitung und verbesserte Resynthese; bestehende
  Spectral-/Instance-Gruppen, Phasen- und Timingregeln weiterverwenden.
- [ ] Spektrogramm, Stereo-/Phasenkorrelation, Clipping-Zähler und
  Übertragungsfunktion mit definierten Referenzsignalen.
- [ ] Analysen mit eindeutiger Normierung, Einheiten, Fensterung und Zeitbezug;
  bestehende Viewer und Exporte erweitern statt pro Node neu implementieren.

Abnahme je Funktion: kleines Beispielprojekt, passende DSP-Referenztests,
Speichern/Laden und wo anwendbar Bake-/Plugin-Parität.

## Begleitende Aufgaben

- [ ] Editor: vorhandene Suche/Favoriten prüfen, Kommentare, farbige Bereiche,
  Kabelbeschriftungen und Suche nach Verbindungen ergänzen. Stabile Katalogsortierung
  beibehalten; generische Funktionen im Core umsetzen.
- [ ] Autosave und Wiederherstellung nach Absturz mit rotierenden Sicherungen;
  Benutzerdatei nicht ungefragt überschreiben. Ungespeicherte Dokumente einbeziehen.
- [ ] Plugin-Abhängigkeiten und Versionen verständlich verwalten, portable Pakete
  vorbereiten. Vorgebaute Plugins bleiben die bereits geplante spätere Ausbaustufe.
- [ ] Alle neuen Oberflächentexte Deutsch/Englisch, Farben und Maße über Themes;
  Dateidialoge mit gespeicherten Verzeichnissen und verständlichen Fehlern.

Jede Phase separat implementieren und prüfen. Die bestehende DSP-Roadmap behält
ihre offenen Nachweise; neue Produktfunktionen sind kein Grund, diese als erledigt
zu markieren. Nächster konkreter Einstieg: Bestandsaufnahme des Host-Datei-I/O und
ein Player mit gemeinsamer Decoder-Schnittstelle aus Phase 1; WAV kann als erster
Entwicklungstest dienen, MP3 und die weiteren genannten Formate gehören zum Zielumfang.
