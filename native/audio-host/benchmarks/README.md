# DSP-Optimierungen und Messungen

Neu: [spezialisierter Kettenexport gegenüber Handcode](specialized-chain-2026-09-18/README.md)
mit vollständiger äußerer Graphverwaltung und separatem Schleifenfusionsvergleich.

Aktueller Produktionsvergleich: [gezieltes Export-Inlining](export-inline-2026-09-18/README.md).
Der vorausgehende [Compiler-Mikrobenchmark](stateful-fusion-2026-09-18/README.md)
ist davon getrennt zu betrachten; dessen 19 % sind kein allgemeiner Gruppengewinn.

## Reproduzieren

Nach `mvn -Pnative-audio verify` (generierte Parameterverträge erforderlich), im Projektordner:

```powershell
& target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe -std=c++17 -O3 -ffp-contract=off -static -I native/audio-host/src -I target/native/generated native/audio-host/benchmarks/DspBenchmark.cpp -o target/dsp-benchmark.exe
& ./target/dsp-benchmark.exe target/dsp-benchmark.csv
```

Compilerpfad gegebenenfalls anpassen. Ohne gleichzeitigen Build messen; Energieprofil,
CPU-Takt, Hintergrundprozesse und thermische Begrenzung beeinflussen insbesondere Maxima.
Der Benchmark verwendet 64 Sinus-Teiltöne pro Stimme und Kanal, 48 kHz, 20 Aufwärmblöcke und 300
Messblöcke je Konfiguration. Die vier Threads entstehen vor den Messungen. Ergebnisse
werden vor der Messung samplegenau mit serieller Berechnung verglichen. Unabhängige
Stimmen verwenden getrennten Zustand. `channels=1` modelliert Mono einschließlich gemeinsam
nutzbarer Stereo-Ausgabe; `channels=2` zwei unabhängig berechnete Kanäle mit unterschiedlichen
Frequenzen. Das ist ein DSP-Mikrobenchmark, kein Audio-Geräte-/Treiberlatenztest.

Die CSV enthält µs pro Block; Deadlines: 2666,67 µs bei 128 und 5333,33 µs bei 256 Frames.
Ein Testlauf beweist keine Garantie für unterbrechungsfreie Echtzeit. Die verwendete
Condition-Variable-Barriere ist ausschließlich für diesen Offline-Vergleich bestimmt.
Sie wird nicht in AudioGraph oder AudioEngine verwendet.

## Bereits im Produktionspfad

- Exportfusion für zusammenhängende Math-/Mix-/MapRange-/Unit-Converter-Abschnitte.
- Vorberechnung konstanter Ausdrücke und Wiederverwendung exakt gleicher Ausdrücke
  innerhalb eines Abschnitts; unveränderte Rechenreihenfolge und Schutzregeln.
- SSE2 für reine Math-Abschnitte, skalarer Rest; konservativer Scheduler-Fallback
  bei Glättung, Control/Event, Polyphonie und ungeeigneter Kanalbelegung.
- Lebensdauerbasierte Wiederverwendung numerischer Kanal-Caches im normalen
  sequentiellen Hostplan. Keine Allokation oder Änderungen der Besitzverhältnisse
  im Callback. Generierte Fusionspläne sind davon ausgenommen.
- Complex-Oscillator-Spektrum: Detuning einmal berechnen, Gewichte nur bei Änderung
  des Falloffs oder der Zugehörigkeit eines Teiltons neu berechnen.

## Vergleichstests

`MathFusionTest`, `MixedMathFusionTest`, `PureFusionTest` vergleichen exportierte
Kernel mit demselben Export bei deaktivierter Fusion. `AdvancedSourceExportTest`
vergleicht außerdem mit dem ursprünglichen Graphen einschließlich MIDI, Stimmen
und Kanalrouting. `OptimizationTest` prüft SIMD-Sonderwerte bitgenau einschließlich
negativer Null, die Spektrumkoeffizienten und geteilte Caches gegen getrennte Caches.
Der vollständige native Hosttest deckt zusätzliche polyphone Kanalgraphen ab.

## Parallelisierung: bewusst noch experimentell

Feste Worker können bei großen unabhängigen Lasten wesentlich helfen. Bei kleinen
Lasten überwiegt oft die Übergabe; auch bei großen Lasten können verzögerte Worker die
schlechteste Blockzeit erhöhen. Vor Produktionseinbau braucht es einen vorgeplanten
Renderpfad mit begrenztem Verhalten bei verspäteten Tasks, sicherer Zustands-/Graph-
Lebensdauer bei Livewechseln und Tests mit realen Geräten. Die serielle Laufzeit
bleibt deshalb der aktive Produktionspfad. Ein Timeout plus paralleles Neuberechnen
wäre kein sicherer Fallback: Beide Threads würden denselben DSP-Zustand verändern.

## Messung am 18.09.2026

Windows x64, LLVM-MinGW, 8 logische Hardwarethreads, kein gleichzeitig laufender
Projektbuild. Rohdaten: [measurements-2026-09-18.csv](measurements-2026-09-18.csv).
Auszug, alle Zeiten in Millisekunden pro Block (seriell → vier Worker):

| Stimmen / Kanäle / Frames | Mittelwert | p99 | Maximum | Deadline-Verletzungen von 300 |
| --- | ---: | ---: | ---: | ---: |
| 1 / 1 / 256 | 0,711 → 0,840 | 1,144 → 1,947 | 1,304 → 3,556 | 0 → 0 |
| 8 / 1 / 256 | 6,156 → 2,312 | 9,047 → 3,513 | 12,457 → 4,321 | 218 → 0 |
| 32 / 2 / 128 | 22,896 → 8,810 | 32,617 → 38,362 | 35,835 → 56,995 | 300 → 300 |

Die absichtlich teure Last zeigt sowohl deutliche Gewinne als auch Übergabekosten
und verschlechterte Ausreißer. Die letzte Konfiguration ist in beiden Varianten
überlastet. Die Ergebnisse begründen weitere Arbeit am parallelen Renderpfad,
aber keine pauschale Aktivierung der Benchmark-Barriere im Host.

Validierung dieser Änderung: vollständiges `mvn -Pnative-audio verify` mit
Audio-/GL-/Gerätetests, 120 Java-Tests ohne Fehler oder Auslassungen; zusätzlich native
DSP-, MIDI-/Kanal-, WASAPI-, ASIO- und Transporttests. ASIO-Hardware war nicht verfügbar;
der simulierte Backendtest lief. Im Cache-Vergleichstest wurden zwölf getrennte
Sample-Buffer auf fünf reduziert, bei bitgleicher Ausgabe (je 1024 Byte Sampledaten).

## Telemetrievergleich (18.09.2026)

```powershell
& target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe -std=c++17 -O2 -ffp-contract=off -static -I native/audio-host/src -I target/native/generated native/audio-host/benchmarks/TelemetryBenchmark.cpp -o target/telemetry-benchmark.exe
& ./target/telemetry-benchmark.exe > target/telemetry-writer.csv
```

Zusätzlich entsteht `target/telemetry-graph.csv`. Drei Wiederholungen, 128/256 Frames,
wechselnde Reihenfolge von bisheriger und neuer Instrumentierung; 300 Messblöcke und
20 Aufwärmblöcke pro Graphfall. Control-Snapshots werden außerhalb des gemessenen
Renderabschnitts im 250-ms-Takt genommen. Der Graphvergleich prüft bitgleiche Ausgaben.
Die Spalte `deadline_misses_of_300` zählt nur Überschreitungen des virtuellen Blockbudgets,
keine tatsächlichen Audiogeräte-Deadlines. Keine parallelen Projektbuilds während der Messung.

Rohdaten: [Schreibkosten](telemetry-writer-2026-09-18.csv) und
[Graphvergleich](telemetry-graph-2026-09-18.csv).
Die zusätzliche Schreibarbeit einschließlich Zeitstempel kostete etwa 104–126 ns je
Aufruf (0,104–0,126 µs). Mittelwerte des Graphen sind ähnlich; p99/Maxima streuen und sind
mit Telemetrie teilweise höher. Es gab einen Budgetausreißer in den sechs Telemetrieläufen,
keinen in den sechs Referenzläufen. Diese kurze Offline-Reihe belegt deshalb ausdrücklich
keine unveränderte Echtzeit-Spitzenlatenz. Reale Routing-Langläufe und weitere Tail-Latency-
Vergleiche bleiben für die Referenzprojektphase offen; keine pauschale Performancefreigabe
für spätere Parallelisierung daraus ableiten.

## Referenzprojekte v1 (18.09.2026)

Sieben Original-/Export-/Scheduler-Szenarien mit 28 Gleichheitsfällen und 126
Offline-Messreihen; zusätzlich isolierte Plugininstallation und reale Routing-Langläufe.
Die DSP-Baseline und ihre Grenzen stehen in [DSP-BASELINE.md](../../../docs/DSP-BASELINE.md),
die Wiederholungsanleitung bei den [Referenzprojekten](../../../examples/reference-v1/README.md).
Rohdaten und Messbedingungen: [reference-2026-09-18](reference-2026-09-18).
`ReferenceHarness.hpp` gehört ausschließlich zum Offline-Test, nicht zum Audio-Callback.
