# Zustandsbehaftete Fusion: gezielter Compilervergleich

Untersucht wird ein einfacher Wavetable-Oszillator → Math Multiply (Gain) →
TPT-Filter. Dies ist ein Offline-Prototyp, keine Änderung am Exportgenerator.
Er nutzt die vorhandenen Oszillator-/Filterroutinen; die Gain-Spezialisierung erhält
die vorhandene Glättung und die Sonderbehandlung von Blöcken mit einem Sample.

## Ergebnis

256 Frames, 48 kHz; Median der acht Laufmittelwerte, µs pro Block:

| Unabhängige Zustände | Bisherige Blockaufrufe | Inline, getrennte Schleifen | Inline, gemeinsame Schleife |
|---|---:|---:|---:|
| 1 | 10,335 | 8,128 | 8,322 |
| 8 | 81,532 | 66,094 | 67,278 |
| 32 | 332,487 | 267,880 | 270,981 |

Die explizite Spezialisierung/Inlinierung spart in diesem Versuch rund 19–21 %.
Die zusätzliche Schleifenfusion ist gegenüber der Inline-Kontrolle etwa 1–2 %
langsamer; daraus folgt kein belastbarer allgemeiner Nachteil, aber auch kein
nachgewiesener Zusatzgewinn. Nur Blockfunktionen pro Sample aufzurufen kostet
hingegen deutlich mehr (8 Zustände: 142,411 µs). Diese naive Variante bleibt als
Negativkontrolle im Benchmark.

**Folgerung:** Zuerst inline geeignete skalare DSP-Bausteine und spezialisierte
Blockaufrufe entwickeln, dann Fusion pro Abschnitt messen. Eine universelle
Fusion aller zustandsbehafteten Nodes ist durch diesen Versuch nicht begründet.
Die Geschwindigkeitszahlen gelten nicht für den vollständigen exportierten Synth.

## Maschinencode

Der mit Clang 20.1.0 bei `-O3 -ffp-contract=off` erzeugte Code ist in `kernels.s`
archiviert (Auszug bis vor `sample_calls`). Die ursprüngliche Blockkette enthält
Aufrufe von Oscillator-render, Math-process und Filter-process. In `inline_blocks`
sind diese DSP-Aufrufe entfernt; drei getrennte Schleifen bleiben vorhanden.
`fused_chain` enthält eine gemeinsame Schleife ohne diese DSP-Aufrufe. Mathematische
Bibliotheksaufrufe wie `tan` und `floor` bleiben. Es findet also keine automatische
vollständige Schleifenfusion statt, obwohl alle Definitionen in derselben
Übersetzungseinheit sichtbar sind.

Die Inline-Kontrolle spezialisiert den Gain wie der Fusionsprototyp und ruft
Oscillator-render direkt auf: Dadurch werden nicht irrtümlich verbleibende innere
Funktionsaufrufe als Vorteil der Schleifenfusion gezählt.

## Validierung und Grenzen

48 Vergleichsfälle: vier Wellenformen × vier Blockgrößen (1/17/128/256) × drei
Varianten gegen die originale Blockkette. Je 180 Blöcke mit wechselnder Frequenz,
Gain, Cutoff, Resonanz und Filtermodus. Ausgaben bleiben bitgleich; Phase,
Frequenzglättung, Gain-Zustand und Filterintegratoren/-Mix werden zusätzlich geprüft.
Timing: 128/256 Frames, 1/8/32 unabhängige Zustände, 100 Aufwärmblöcke,
1000 Messblöcke, acht Wiederholungen mit rotierter Reihenfolge, Prüfsumme außerhalb
der Messung. Rohdaten einschließlich p99 und Maximum: `results.csv`.

Keine Geräteausgabe, keine vollständige Graph-/Plugin-Grenze, keine MIDI-Zuteilung,
kein Fanout und keine verbundenen Modulationseingänge oder benutzerdefinierten
Wavetables. Die Zustände modellieren unabhängige Stimmen oder Kanäle; dies testet
nicht deren Routing. Gemessen auf dem bisherigen i5-1135G7-Rechner ohne parallelen
Build. Kein Fast-Math; die Schutzregeln bleiben erhalten. Produktionsdateien und
Anwendungsartefakte werden durch diesen Versuch nicht verändert.

## Wiederholen

Generierte Parameterverträge unter `target/native/generated` werden vorausgesetzt:

```powershell
$cxx = 'target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe'
New-Item -ItemType Directory -Force target/stateful-fusion | Out-Null
& $cxx -std=c++17 -O3 -ffp-contract=off -static -I native/audio-host/src -I target/native/generated native/audio-host/benchmarks/StatefulFusionBenchmark.cpp -o target/stateful-fusion/benchmark.exe
& target/stateful-fusion/benchmark.exe target/stateful-fusion/results.csv
& $cxx -std=c++17 -O3 -ffp-contract=off -S -masm=intel -I native/audio-host/src -I target/native/generated native/audio-host/benchmarks/StatefulFusionBenchmark.cpp -o target/stateful-fusion/benchmark.s
```
