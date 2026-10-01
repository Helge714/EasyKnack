# Export-Restaufwand

Nach der Kanal-Korrektur werden zwei weitere unnötige Arbeiten vermieden:

- Exportpläne schreiben Prozessorergebnisse direkt in ihre eigenen, disjunkten
  Kanalpuffer. Der normale Hostplan mit wiederverwendeten Puffern bleibt unverändert.
- Ein Plugin ohne angeschlossene numerische Eingänge erzwingt im äußeren Graphen
  keine kanalübergreifende Ausführung mehr. Seine interne Stereo-Verarbeitung bleibt
  erhalten; angeschlossene numerische Grenzen benötigen weiterhin den konservativen Plan.

Vergleich bei 256 Frames und 48 kHz, Median aus vier Mittelwerten in µs:

| Projekt | Original | Export | Veränderung |
|---|---:|---:|---:|
| Math | 10,56 | 5,11 | −51,6 % |
| Synth, 1 Stimme | 53,22 | 53,71 | +0,9 % |
| Synth, 8 Stimmen | 414,92 | 424,31 | +2,3 % |
| Synth, 32 Stimmen | 1642,23 | 1653,86 | +0,7 % |
| Stereo-Kontext | 17,72 | 15,66 | −11,6 % |

Der Export ist damit für die Synth-Referenzen ungefähr gleich schnell, aber noch
nicht schneller. Oszillatoren und Filter verwenden dieselben DSP-Kerne wie der
Originalgraph; die bisherige Math-Fusion beschleunigt diese Kerne nicht automatisch.
Die zuvor gemessenen 11 % Restaufwand sind in diesem Lauf nicht mehr vorhanden.
Kleine Unterschiede sind keine belastbaren allgemeinen Geschwindigkeitsgarantien.

`copy-export` deaktiviert nur das direkte Schreiben, im selben Messprogramm.
Beim Acht-Stimmen-Synth ist sein Mittelwert mit 424,45 µs praktisch identisch zum
Export. Das Entfernen der Kopie allein erklärt daher keinen stabilen Synth-Gewinn.
`scheduled-export` deaktiviert die Math-Fusion. Alle vier Varianten erhalten dieselben
Eingaben; die Reihenfolge wird über vier Wiederholungen rotiert. Alle 28
Gleichheitsfälle bleiben bitgleich, einschließlich Parameter-/Mute-/Graphwechseln
und MIDI-Ereignissen. Rohdaten und p99/Maxima: [Messbericht](results.md).

Umgebung wie [Ausgangsbaseline](../reference-2026-09-18/environment.json),
Clang 20.1.0, `-std=c++17 -O2 -static`, ohne parallelen Build. Keine neue
Geräte-Langzeitmessung; diese Offline-Zahlen messen keine Treiber-Unterläufe.

SHA256:

- `BakedGraph.hpp`: `7FE3400D70EAE528BAF786BF82F282A869E0A12A0CC8D73FCB351B85F822523E`
- `GraphRuntime.hpp`: `94EF357849296C41D4EAC4E16688144B8CF8A0103517C9E4DCA14DC3BA2B47A1`
- Messprogramm: `566234E7C6CD5E109DC3A52C9442F5C1CE93E8BF2F08DBC83E6535EBCB6E4DA9`

Die Änderungen gehören zum gemeinsamen Plugin-SDK. Bestehende Quellplugins
profitieren beim Neubau nach SDK-Cacheinvalidierung; erneutes Exportieren ist unnötig.

Abschluss: vollständiges Maven-`verify` mit nativem Build sowie Audio-, GL-, Geräte-
und Referenztests bestanden: 129 Tests ohne Fehler oder Auslassungen. Auch der
erneute Referenzlauf blieb in allen 28 Fällen bitgleich. Host, Anwendung und SDK
sind neu gebaut. Buildprotokoll: `target/export-overhead-verify.log`.
