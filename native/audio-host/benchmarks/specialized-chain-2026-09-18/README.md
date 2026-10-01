# Spezialisierter Export gegenüber einem handgeschriebenen Node

## Produktionsänderung

Der Export erkennt eine geschlossene, dreigliedrige Kette aus einfachem Oszillator,
Math Multiply (ohne Clamp) und Filter. Sie darf keine externen Eingänge, zusätzlichen
Nodes oder Zwischenabgriffe haben; ihr einziger Ausgang muss der Filterausgang sein.
Für diese Form wird ein spezialisierter Kern mit gemeinsamer Sampleschleife erzeugt. Er überspringt die
allgemeine Kontext-/Port-/Stimmenverwaltung pro innerem Node und schreibt das letzte
Ergebnis direkt in den Gruppenausgang. Zwischenwerte bleiben lokale Variablen;
die weiterhin für den Fallback angelegten Zwischenpuffer werden im Spezialpfad
nicht beschrieben. Die Speicherallokation des inneren Graphen wird noch nicht reduziert.

Glättungskoeffizienten werden einmal vor der Sampleschleife berechnet. Die erste
Fusionsvariante rief dagegen `exp` mehrfach pro Sample auf und war deutlich langsamer.
Diese Zusatzarbeit war im erzeugten Maschinencode sichtbar. Gemeinsame vorbereitete
DSP-Routinen (`renderPrepared`/`processPrepared`) beheben das, ohne Schutzregeln oder
Zustandsübergänge zu verändern. Die normalen Nodes verwenden dieselben Routinen.

Die Prozessorinstanzen und deren Zustände bleiben erhalten. Frequenz-/Gain-/Filter-
Glättung, Wavetable-Auswahl, Filtermodusübergänge, numerische Schutzregeln und die
Ein-Sample-Sonderbehandlung sind identisch. Graphwechsel verwenden weiterhin die
gewöhnliche Zustandsübernahme. Ein Laufzeitcheck verlangt aktive Mono-Nodes mit
einer Stimme und Audiorate; bei Abweichungen läuft der bisherige Scheduler.
Andere Gruppen (insbesondere MIDI-/Complex-Oscillator-Synths) bleiben unverändert.

Neue Quellexporte verwenden den Pfad automatisch. Bereits exportierte Pluginquellen
müssen erneut exportiert und aktualisiert werden; manuelle Änderungen werden nicht
automatisch übertragen. Es gibt keine allgemeine Fusion beliebiger DSP-Graphen.

## Vergleich

Alle sechs Varianten laufen in derselben äußeren AudioGraph-Struktur mit Monitor-Gain
und Ausgang, einschließlich beider Kanalaufrufe:

- Originalgruppe mit normalen Nodes.
- Bisheriger Export einschließlich Math-/Filter-Inlining, ohne neuen Spezialpfad.
- Neuer Export mit getrennten spezialisierten Schleifen (Vergleich).
- Neuer Export mit einer gemeinsamen, vorbereiteten Sampleschleife (Produktionspfad).
- Handgeschriebener Ein-Node-Rechner ohne inneren Rendergraphen.
- Zusätzlich eine handgeschriebene Fusion mit ebenfalls vorbereiteten Koeffizienten.

Der handgeschriebene Node teilt die vorhandenen Oszillator-/Filter-DSP-Routinen und
Math-Schutzregeln, verwendet aber nicht den neuen Kettenkernel. Er besitzt eigene
inline gespeicherte Zustände und zwei Zwischenpuffer (die Handcode-Fusion verwendet
lokale Zwischenwerte). Nur die Konfiguration nutzt den erzeugten Parser;
der temporäre Konfigurationsgraph wird vor dem Rendern verworfen. Das ist eine
faire konkrete Handcode-Referenz, kein Beweis für das theoretische Optimum eines
beliebig weiter optimierten DSP-Algorithmus.

20 bitgleiche vollständige Graphvergleiche: fünf Varianten gegen das Original bei
1/17/128/256 Frames, je 160 Blöcke und beide Kanäle. Frequenz, Wellenform, Gain,
Cutoff und Filtermodus wechseln; ein Graphersatz übernimmt den Zustand. Zusätzliche
Vergleiche prüfen den Scheduler-Fallback bei geänderter Operation und Clamp.

Zeitmessung: Clang 20.1.0, `-O3 -ffp-contract=off -static`, 48 kHz, zwölf rotierte
Wiederholungen je Variante und Blockgröße, 100 Aufwärmblöcke und 1500 Messblöcke.
Die äußere Graphverwaltung ist enthalten, Hardware-/Treiberkosten nicht. Keine
Parallelisierung. Die Gruppen enthalten eine Mono-Kette; separate Polyphonie und
echte Stereo-Verarbeitung sind keine Aussage dieses Spezialvergleichs.

## Wiederholen

```powershell
mvn -o -Daudio.cxx=<clang++.exe> -Deasyknack.test.audio=true -Dtest=SpecializedChainTest test
target/specialized-chain/comparison.exe target/specialized-chain/standalone.csv
```

Der Test erzeugt Java-/C++-Exportdateien sowie den nativen Vergleich unter
`target/specialized-chain`. Separaten Messlauf ohne gleichzeitigen Build ausführen.
`initial.csv`, `build-run.csv` und `before-prepared.csv` dokumentieren frühere Versuche
vor der Koeffizientenvorbereitung; abschließende Ergebnisse stehen unten.

## Abschlussmessung

Median der zwölf Laufmittelwerte, µs pro vollständigem äußeren Graphblock:

| Variante | 128 Frames | 256 Frames |
|---|---:|---:|
| Originalgruppe | 5,891 | 11,651 |
| Bisheriger Export | 5,863 | 11,446 |
| Spezialisierte getrennte Schleifen | 5,403 | 10,805 |
| Spezialisierte vorbereitete Fusion | **4,916** | **9,673** |
| Handgeschriebene Blockvariante | 5,247 | 10,610 |
| Handgeschriebene vorbereitete Fusion | 5,159 | 10,311 |

Damit benötigt der neue Export in dieser Messung etwa **17 % weniger Renderzeit**
als die Originalgruppe und etwa **15,5 % weniger** als der bisherige Export bei
256 Frames. Er liegt auf dem Niveau der konkreten Handcode-Referenzen, hier sogar
etwas darunter. Das ist kein Nachweis, dass generierter Code grundsätzlich schneller
als Handcode wäre: Speicherlayout, Kontextübergabe und Compilerentscheidungen
unterscheiden sich. Alle Varianten verwenden dieselben numerischen DSP-Regeln.
Rohwerte einschließlich p99/Maximum: `results.csv`. Messung ohne parallelen Build,
gleicher i5-1135G7-Rechner wie die bisherigen Referenzen; keine Treiberlatenzmessung.

`kernels.s` enthält Auszüge aus dem finalen Maschinencode. Sowohl im erzeugten
Fusionskernel als auch in der handgeschriebenen Fusion steht `exp` vor der Schleife;
im Schleifenkörper bleiben die sampleabhängigen `floor`-/`tan`-Aufrufe. Die getrennte
Variante enthält weiterhin getrennte Schleifen. Der Compiler hat diese Vorbereitung
und Schleifenfusion nicht selbst aus den bisherigen Blockaufrufen hergestellt.

Vollständiger Build: `target/specialized-chain-final-verify.log`, **130 Tests ohne
Fehler oder Auslassungen**, zusätzlich native DSP-/MIDI-/WASAPI-/ASIO-/Transporttests.
Alle **32** allgemeinen Referenzfälle bleiben bitgleich. Der separate finale
Sechsfachvergleich nach der Umstellung der Handcode-Zustände auf Inline-Speicherung
bestätigt nochmals **20** bitgleiche Graphvergleiche plus beide Scheduler-Fallbacks.
Host, Anwendung und Plugin-SDK sind neu gebaut. ASIO wurde simuliert geprüft;
für diese Änderung wurde kein neuer Geräte-Langlauf ausgeführt.
