# DSP-Referenzprojekte v1

Diese `.knack`-Dateien lassen sich über **File → Open** öffnen. Die acht
Offline-Projekte haben einen Monitorfaktor von 0,1; Synths verwenden Tastatur-MIDI.
Die Dokumente sind eigenständig und verändern keine persönlichen Projekte.

| Projekt | Prüfzweck |
|---|---|
| `math.knack` | Math, Mix, MapRange, Unit Converter, Untergruppe, Konstanten und Fanout |
| `synth-1.knack`, `synth-8.knack`, `synth-32.knack` | MIDI Note, Envelope, Complex Oscillator mit 16 Teiltönen, Filter pro Stimme, Mean Merge |
| `stereo.knack` | Context → Frequenzmapping → Oscillator, Pan, Separate und vertauschte Combine-Eingänge |
| `rates.knack` | MIDI Delay, Event-Math, Oszillator, Control-RMS und geglätteter Audio-Verbraucher |
| `analysis.knack` | Oszilloskop und Autokorrelation außerhalb der exportierten Oszillatorgruppe |
| `osc-gain-filter.knack` | Oszillator → Gain → Filter als vollständige Gruppe; gezielter Inlining-Vergleich |
| `routing.knack` | Portable Vorlage für virtuellen Capture-Eingang, Verarbeitung, Analyse und stummen Geräteausgang |

`routing.knack` speichert keine rechnerabhängige Geräte-GUID. „Choose virtual cable
capture“ auf **CABLE Output** setzen und das gewünschte echte Ausgabegerät auswählen.
Der letzte Math-Node hält den physischen Ausgang stumm; für eine Hörprobe seinen
Faktor bewusst anheben. Windows-Standardgeräte und Gerätepegel bleiben unverändert.

## Automatischer Vergleich

```powershell
mvn -o -Daudio.cxx=<Pfad-zum-clang++.exe> -Deasyknack.test.references=true -Dtest=ReferenceProjectsTest test
```

Die Suite erzeugt unter `target/reference-v1` frische Java-/C++-Quellen,
`.knackplugin`-Dateien, Hostprotokolle und einen nativen Vergleichsrunner. Alle acht
Plugins werden außerdem gemeinsam in einem isolierten Testverzeichnis installiert,
im passenden Host geladen und auf Cache-Wiederverwendung geprüft. Das persönliche
Pluginverzeichnis wird nicht verändert.

Vier Varianten: Original, Export, Export ohne Math-Fusion/Export-Inlining
(`scheduled-export`) und Export ohne direkte Ausgabe in Kanalpuffer (`copy-export`).
`scheduled-export` deaktiviert auch den spezialisierten Oszillator–Gain–Filter-Plan.
Ein zusätzlicher `SpecializedChainTest` vergleicht diese Kette mit dem bisherigen
Export, einer gemeinsamen Sampleschleife und einem handgeschriebenen Node.
Clang verwendet jetzt `-O3 -ffp-contract=off` wie der Pluginbau; ältere archivierte
Referenzmessungen verwendeten `-O2`.

Gleichheitsprüfung: 48 kHz, 1/17/128/256 Frames, beide Kanäle und 36.000 Samples je
Kanal. Bei Sample 8192/16384/24576, jeweils am nächsten Blockanfang, erfolgen
Parameteränderung, Mute und Wiederherstellung mit Zustandsübernahme. Toleranz höchstens
1e-5 absolut, zusätzlich werden Bitabweichungen ausgewiesen. Nichtfinite oder komplett
stumme Ergebnisse führen zum Fehler. MIDI-Delay-Ereignisse werden nach Offset,
Reihenfolge, Inhalt und Metadaten exakt verglichen.

Die MIDI-Sequenz steht in `ReferenceHarness.hpp`: 32 Note-ons ab Sample 0 im Abstand
von sieben Samples, Pitch Bend bei 10000, Note-offs ab 18000, neue Note-ons ab 24000
und All Notes Off bei 32000. Kleine Stimmenzahlen prüfen dabei auch Stimmenersetzung.

Timing: 128/256 Frames, vier Wiederholungen mit rotierter Variantenreihenfolge,
80 Aufwärmblöcke und 400 Messblöcke; alle Stimmen bleiben gehalten. Nach dem Build
kann `target/reference-v1/references.exe <Ausgabeverzeichnis>` separat messen.
Keine parallelen Builds ausführen. `offline.csv` und `equivalence.csv` enthalten die
Rohdaten; `scripts/summarize-references.py` erstellt daraus einen Bericht. Virtuelle
Blockbudgetüberschreitungen sind keine gemessenen Geräte-Unterläufe.

Die teuren Vergleiche sind opt-in; der normale Testlauf prüft gespeicherte Dokumente.
Dokumente ausschließlich bewusst neu erzeugen:
`-Deasyknack.references.generate=true -Dtest=ReferenceProjectsTest#versionedDocumentsRemainValidAndDeterministic`.
Stabile IDs und feste Positionen stammen aus `ReferenceProjects.java`.

## Echte Routingstrecke

```powershell
python scripts/reference-routing.py
python scripts/reference-routing.py --run --cable-playback '<ID von CABLE Input>' --cable-capture '<ID von CABLE Output>' --output '<ID des echten Ausgangs>'
```

Der erste Aufruf listet Geräte auf. Der zweite startet zwei eigene Hosts: einen
440-Hz-Generator in VB-Cable und einen Capture-/Analyse-Host zur Surface-Ausgabe.
Der physische Ausgang bleibt stumm; die Analyse davor prüft den Ton. Fünf Sekunden
Aufwärmen, drei stationäre Läufe à 120 Sekunden und 300 Sekunden mit bis zu zehn
Graphänderungen pro Sekunde. Ergebnisse: `target/reference-routing`.

Aufnahmezugriff muss erlaubt sein; eine Sandbox kann WASAPI-Capture trotz sichtbarem
Gerät verhindern. Der Test verändert weder Windows-Gerätepegel noch Standardgeräte.
Er ist kein Hörtest und misst keine Ende-zu-Ende-Latenz. Ein fehlender Ringbufferfehler
beweist nicht, dass der Treiber jede physische Deadline eingehalten hat.
