# Gezieltes DSP-Inlining im tatsächlichen Export

Neu erzeugte Exportpläne wählen mit `processNodeAs<T,true>` optionale spezialisierte
Blockroutinen. Math entfernt den inneren Ein-Sample-Blockaufruf und verwendet die
gemeinsame `evaluate`-Semantik; Filter wird am typisierten Aufrufort inline eingesetzt.
Der normale Graph bleibt unverändert. Kanal-, Stimmen-, Event- und Glättungslogik
läuft weiterhin durch dieselbe `GraphRuntime`; es gibt keine neue Sampleschleifenfusion.

Die erste Variante enthielt zusätzlich erzwungenes Oszillator-Inlining. Die komplette
Stereo-Referenz wurde damit langsamer. Diese Spezialisierung wurde zurückgenommen;
der einfache und der Complex Oscillator verwenden ihre bisherigen Blockroutinen.
Die Versuchsdaten bleiben unter `build-run` und `oscillator-inline-trial` erhalten.
Sie sind ausdrücklich nicht die Messung des finalen Produktionspfads.

## Vergleichsaufbau

Die Referenzsuite enthält jetzt zusätzlich `osc-gain-filter.knack`: dieselbe einfache
Kette wie der Mikrobenchmark, aber als vollständige Original-/Exportgruppe mit
Monitor-Gain und Ausgang. Insgesamt acht Szenarien, 32 Gleichheitsfälle einschließlich
MIDI, Stimmen, Stereo, Raten und Zustandsübernahme bei Graphwechseln.

Vier Varianten: Original, neuer Export, Export ohne Math-Fusion und ohne spezialisierte
Blockaufrufe (`scheduled-export`) sowie Export mit altem Kopierpfad (`copy-export`).
Bei Synth und Oszillator–Gain–Filter enthält die Kette keinen zusammenhängenden
Math-Fusionsabschnitt; dort isoliert `scheduled-export` den Inlining-Schritt.

Alle Varianten verwenden jetzt `-O3 -ffp-contract=off -static`, entsprechend den
Optimierungsflags des Pluginbaus. Die früheren Referenzmessungen mit `-O2` dürfen
nicht als direkter Vorher-/Nachher-Vergleich benutzt werden. Vier rotierte
Wiederholungen, 400 Messblöcke nach 80 Aufwärmblöcken, 48 kHz, 128/256 Frames.
Die Messung betrifft Renderkosten, keine Treiberlatenzen oder Geräte-Unterläufe.

## Finale Ergebnisse

Separater Lauf nach dem erfolgreichen Build, 256 Frames, Median der vier
Mittelwerte in µs. Vollständige Rohdaten inklusive 128 Frames, p99, Maxima und
Kopierkontrolle: [results.md](results.md), `offline.csv`, `equivalence.csv`.

| Projekt | Original | Export | Export ohne neue Spezialisierung und Math-Fusion |
|---|---:|---:|---:|
| Math | 10,85 | 5,58 | 10,37 |
| Oszillator–Gain–Filter | 15,47 | 15,25 | 15,45 |
| Synth 1 Stimme | 52,86 | 52,04 | 51,97 |
| Synth 8 Stimmen | 578,10 | 549,11 | 573,92 |
| Synth 32 Stimmen | 2124,35 | 2156,34 | 2181,05 |
| Stereo | 20,65 | 20,94 | 20,27 |
| Raten | 13,08 | 14,53 | 13,76 |
| Analyse | 37,22 | 37,29 | 36,96 |

Im ebenfalls archivierten `final-build`-Lauf lag die kleine Filtergruppe 2,5 %
vor dem Original, separat 1,4 %. Beim Acht-Stimmen-Synth waren es 4,1 % bzw. 5,0 %.
Die 32-Stimmen-Ergebnisse wechseln dagegen von 3,3 % schneller auf 1,5 % langsamer.
Auch die Raten-Referenz schwankt deutlich. Die bisherigen Daten begründen deshalb
keinen universellen Geschwindigkeitsgewinn; Unterschiede im kleinen Prozentbereich
sind keine zugesicherte Beschleunigung. Die Math-Beschleunigung stammt überwiegend
aus der bereits vorhandenen Fusion, nicht aus dieser Änderung.

Der Mikrobenchmark-Vorteil von 19 % überträgt sich nicht auf den vollständigen
Ausführungsplan: Dort bleiben Kontextaufbau, Puffer-/Grenzübergaben und Zustandslogik
erhalten; außerdem gelten andere Compiler-Inlining-Entscheidungen. Die engere
Spezialisierung ist jetzt produktiv, eine durchgehend verschmolzene Synth-Engine nicht.

Validierung: vollständiges `mvn -Pnative-audio verify` mit Audio-, GL-, Geräte- und
Referenztests: **129 Tests ohne Fehler oder Auslassungen**, zusätzlich native
DSP-/MIDI-/WASAPI-/ASIO-/Transporttests. Alle **32** Referenzfälle auch im separaten
Lauf bitgleich. Log: `target/export-inline-final-verify.log`. Host, Anwendung und
SDK sind neu gebaut. Keine neue Hardware-Langzeitmessung; ASIO-Prüfung simuliert.

## Plugins aktualisieren

Anwendung, Host und SDK werden gemeinsam neu gebaut. Bestehende Pluginquellen
bleiben unverändert: Um die neue Aufrufstrategie zu erhalten, die ursprüngliche
Gruppe erneut exportieren und das Plugin aktualisieren. Manuelle Quelländerungen
werden dabei nicht automatisch übertragen. Alte Exportquellen bleiben mit dem
SDK kompatibel und verwenden weiterhin den bisherigen Aufrufpfad.

Der isoliert gemessene Gewinn von 19 % ist keine Aussage über vollständige Gruppen.
Diese Integration prüft ausdrücklich, welche Einsparung nach Graph-/Exportaufwand
übrig bleibt. Kleine Unterschiede und schwankende Spitzen sind nicht als garantierte
Echtzeitverbesserung zu interpretieren.
