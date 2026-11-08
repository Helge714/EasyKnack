# DSP-Baseline: Referenzprojekte v1

**Spezialisierter Kettenexport:** Für eine geschlossene Oszillator–Gain–Filter-Gruppe
gibt es jetzt einen direkten Blockkern und einen vollständigen Vergleich mit einem
[handgeschriebenen Node](../../native/audio-host/benchmarks/archive/specialized-chain-2026-09-18/README.md).

**Export-Inlining:** Der Produktionsvergleich mit einer zusätzlichen vollständigen
Oszillator–Gain–Filter-Gruppe steht unter
[Export-Inlining](../../native/audio-host/benchmarks/archive/export-inline-2026-09-18/README.md).
Dieser neue Vergleich verwendet einheitlich die Plugin-Optimierungsflags `-O3 -ffp-contract=off`.

**Weitere Optimierung:** Unnötige Export-Kopien und äußere Kanalpuffer entfallen.
Die Synth-Referenzen liegen jetzt ungefähr auf Originalniveau; Math bleibt deutlich
schneller. [Messung und Grenzen](../../native/audio-host/benchmarks/archive/export-overhead-2026-09-18/README.md).

**Nachtrag:** Die nachfolgend dokumentierte doppelte Synth-Rechenzeit wurde auf die
Kanalbehandlung an Exportgrenzen zurückgeführt und korrigiert. Neue Messergebnisse:
[Kanal-Korrektur](../../native/audio-host/benchmarks/archive/channel-fix-2026-09-18/README.md).
Die ursprünglichen Messwerte bleiben hier als Vergleichsbasis erhalten.

Die Referenzsammlung liegt unter [examples/reference-v1](../../src/test/resources/reference-projects/README.md).
Sie umfasst sieben exportierbare DSP-Projekte und eine portable Routingvorlage.
Generator, gespeicherte Dokumente und Vergleichsrunner gehören zum Projekt; persönliche
Dokumente und die persönliche Plugininstallation werden nicht verändert.

## Vergleich und Wiederholbarkeit

- Originalgraph, frisch generierter Export und Export ohne Math-Fusion bekommen dieselben
  samplegenauen MIDI-Daten. Die generierten Java-Klassen werden kompiliert, ihre Verträge
  mit C++ abgeglichen und alle sieben Klassen gemeinsam als Quellplugin installiert.
- 28 Gleichheitsfälle: sieben Projekte × 1/17/128/256 Frames, 48 kHz und beide Kanäle.
  Mono-Signalgraphen und ein explizites Stereo-Kontextprojekt sind enthalten. Kein separater
  Benchmark eines einkanaligen Hardwaregeräts. Parameteränderung, Mute und Wiederherstellung
  übernehmen den laufenden Zustand; MIDI-Delay-Offsets und -Reihenfolge werden exakt geprüft.
- Vorab festgelegte Toleranz: 1e-5 absolut, nichtfinite Ergebnisse verboten. Zusätzlich
  werden Bitabweichungen gezählt. In der abgelegten Messung: **keine Bitabweichung**.
- Offline-Laufzeit: drei Wiederholungen mit rotierter Reihenfolge, je 80 Aufwärm- und 400
  Messblöcke. Das ist eine Startbaseline, kein statistischer Nachweis einer Echtzeitgarantie.
- Speicherzahl: tatsächlich vorhandene Float-Samplebuffer einschließlich Stimmen,
  Kanal-Caches und eingebetteter Graphen, geteilte Speicherbereiche nur einmal. Sie beschreibt
  weder gesamten Heap noch Prozess-RAM; Prozessorzustand und MIDI-Speicher fehlen ausdrücklich.

Die zugehörigen Messbedingungen, Binär-/Quellhashes und Rohdaten werden unter
[reference-2026-09-18](../../native/audio-host/benchmarks/archive/reference-2026-09-18) abgelegt.
Der aus CSV erzeugte [Messbericht](../../native/audio-host/benchmarks/archive/reference-2026-09-18/results.md)
führt die einzelnen Konfigurationen auf. Die Skripte überschreiben nur ihre explizit
gewählten Ergebnisverzeichnisse; alte Baselines separat aufbewahren.

Abschlussprüfung: vollständiges Maven `verify` mit Audio-, GL-, Geräte- und
Referenztests: **129 Tests, keine Fehler, keine ausgelassenen Tests**. Die Referenzsuite
wurde dabei erneut ausgeführt. Log: `target/reference-full-verify.log`. Der finale
Routingrunner wurde zusätzlich mit kurzen Durchläufen geprüft. GLSwing bleibt eine
externe Maven-Abhängigkeit; diese Arbeit benötigt keine Core-Änderung.

## Was die Offline-Messung bereits zeigt

Die Math-Gruppe profitiert von Fusion: bei 256 Frames sank der Median der Mittelwerte
von rund **9,37 auf 6,54 µs**. Die Synth-Exporte brauchen dagegen ungefähr doppelt so lange
wie ihre Originalgraphen: bei acht Stimmen und 256 Frames **418 auf 834 µs**. Auch der
Export ohne Fusion zeigt diese Mehrkosten. Damit liegt der erste Prüfpunkt vor weiterer
arithmetischer Fusion an den Export-/Schedulergrenzen.

Die 32-Stimmen-Konfiguration ist bewusst anspruchsvoll. Bei 128 Frames überschritt der
Export in 107 von 1200 Offline-Blöcken das virtuelle Budget; beim Original waren es null.
Das sind **keine gemessenen Geräte-Unterläufe**. Die erheblich verschiedenen Ausreißer
zwischen Export und Export ohne Fusion zeigen außerdem die Streuung unter Windows.

Der gezählte Samplebuffer-Speicher stieg beim Acht-Stimmen-Synth von **191 auf 334 KiB**,
bei 32 Stimmen von **743 auf 1270 KiB**. Das belegt Verbesserungspotenzial der Speicher-
und Kanalplanung, ohne bereits eine konkrete Einsparung zu versprechen.

## Priorität für Phase C

1. Kanalbehandlung an Exportgrenzen prüfen. `BakedInput` behandelt auch MIDI-Grenzen
   konservativ als stereo; `BakedGraphProcessor` reicht noch keine Information über
   identische Ausgangskanäle an den äußeren Graphen weiter. Das ist ein aus dem Code
   abgeleiteter Verdacht für doppelte Berechnung, noch kein isolierter Ursachennachweis.
   Kontext-/Pan-/Separate-Fälle müssen dabei ihre Kanalunterschiede behalten.
2. Die bereits geplante Control-/Event-Fusion gegen diese Baseline umsetzen. Eine Änderung
   darf weder MIDI-Offsets noch Glättung, gehaltene Werte oder Voice-Stealing verändern.
3. Samplebuffer anhand des tatsächlich erzeugten Plans wiederverwenden. Fanout,
   externe Eingänge, Analysebeobachter und Fallback-Pfade bleiben Lebensdauergrenzen.
4. Erst danach Produktionsparallelisierung angehen. Ein Mittelwertgewinn allein reicht
   nicht, wenn p99, Speicherbedarf oder Fehlerzähler schlechter werden.

## Aussagegrenzen der Geräteprüfung

Die drei stationären Läufe à zwei Minuten blieben ohne Ringbuffer-Unter-/Überläufe
und ohne Renderbudget-Überschreitungen. Im fünfminütigen Live-Lauf wurden **2444
Graphänderungen** verarbeitet. Auch dort gab es keine Ringbufferfehler; **ein** Renderblock
benötigte **2748,9 µs** bei **2666,7 µs** Budget. Der Prüfton wurde in jeder der 2444
Live-Analyseabfragen erkannt. Rohverlauf und Gerätemetadaten liegen in `routing.zip`
neben dem Messbericht; es werden keine aufgenommenen Audiosamples gespeichert.

Der Routingrunner verwendet einen eigenen Generator-Host in VB-Cable und einen eigenen
Capture-/Analyse-Host zur Surface-Ausgabe. Der letzte DSP-Schritt setzt den physischen
Ausgang auf null. Frequenz und Konfidenz werden davor geprüft. Windows-Gerätepegel,
Standardgeräte und Benutzerprojekte bleiben unverändert.

Die Aufwärmphase wird vor dem Messreset abgeschlossen; Rohdaten enthalten den gesamten
anschließenden Verlauf. Das berichtete p99 ist das größte abgefragte Ein-Sekunden-p99,
kein über die gesamte Sitzung berechnetes Quantil. Maximum und Fehlerzähler gelten seit
Reset. Tatsächliche Framezahl und Samplefrequenz stehen in jedem Diagnosesnapshot.

Ein nominaler Langlauf mit aktiver Telemetrie ersetzt **keinen Telemetrie-an/aus-Vergleich**.
Der offene A4-Nachweis unveränderter Echtzeit-Spitzenlatenz bleibt deshalb getrennt
ausgewiesen. Ende-zu-Ende-Latenz, physische Treiber-Deadlines und eine Hörprüfung werden
durch diesen Lauf nicht gemessen. ASIO-Hardware ist auf diesem Rechner weiterhin nicht
verfügbar. Die beiden eigenen WASAPI-Hosts prüfen getrennte Geräte-/Capture-Clocks.
