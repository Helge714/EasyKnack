# Referenzmessung

28 Gleichheitsfälle; maximaler absoluter Fehler: 0; Bitabweichungen: 0.

Offline: Stereo-Aufruf, 48 kHz, drei Wiederholungen mit rotierter Reihenfolge. Die Tabelle zeigt den Median der drei Mittelwerte und den höchsten p99 der drei Läufe. Budgetüberschreitungen sind virtuelle Blockbudgets, keine gemessenen Geräte-Unterläufe. Samplebuffer enthalten Stimmen und eingebettete Graphen, ohne doppelte Zählung gemeinsam genutzter Speicherbereiche. Prozessorzustand, MIDI, Heap-Overhead und Programmcode fehlen in dieser Speicherzahl.

| Projekt | Frames | Modus | Mittel µs | höchster p99 µs | Maximum µs | über Budget / 1200 | Samplebuffer KiB |
|---|---:|---|---:|---:|---:|---:|---:|
| math | 128 | original | 4.84 | 8.00 | 17.20 | 0 | 28 |
| math | 128 | export | 3.37 | 4.10 | 8.20 | 0 | 43 |
| math | 128 | scheduled-export | 5.87 | 6.60 | 27.10 | 0 | 43 |
| math | 256 | original | 9.37 | 11.50 | 36.20 | 0 | 28 |
| math | 256 | export | 6.54 | 9.10 | 21.90 | 0 | 43 |
| math | 256 | scheduled-export | 11.16 | 13.30 | 15.10 | 0 | 43 |
| synth-1 | 128 | original | 27.00 | 58.60 | 76.70 | 0 | 30 |
| synth-1 | 128 | export | 55.07 | 86.60 | 205.90 | 0 | 61 |
| synth-1 | 128 | scheduled-export | 54.65 | 82.40 | 219.10 | 0 | 61 |
| synth-1 | 256 | original | 54.48 | 120.80 | 171.30 | 0 | 30 |
| synth-1 | 256 | export | 108.54 | 181.20 | 217.50 | 0 | 61 |
| synth-1 | 256 | scheduled-export | 109.50 | 184.70 | 447.30 | 0 | 61 |
| synth-8 | 128 | original | 205.75 | 311.10 | 846.70 | 0 | 191 |
| synth-8 | 128 | export | 415.05 | 659.00 | 1180.70 | 0 | 334 |
| synth-8 | 128 | scheduled-export | 415.05 | 673.70 | 1195.00 | 0 | 334 |
| synth-8 | 256 | original | 418.45 | 681.80 | 1269.20 | 0 | 191 |
| synth-8 | 256 | export | 833.73 | 1222.30 | 1926.60 | 0 | 334 |
| synth-8 | 256 | scheduled-export | 832.02 | 1226.80 | 1423.40 | 0 | 334 |
| synth-32 | 128 | original | 827.92 | 1341.40 | 1383.40 | 0 | 743 |
| synth-32 | 128 | export | 1698.38 | 3118.30 | 3185.10 | 107 | 1270 |
| synth-32 | 128 | scheduled-export | 1658.61 | 2275.10 | 2808.10 | 3 | 1270 |
| synth-32 | 256 | original | 1658.36 | 2461.50 | 3165.00 | 0 | 743 |
| synth-32 | 256 | export | 3336.78 | 4759.50 | 5430.90 | 3 | 1270 |
| synth-32 | 256 | scheduled-export | 3320.56 | 4825.30 | 5412.70 | 2 | 1270 |
| stereo | 128 | original | 7.42 | 16.80 | 36.30 | 0 | 34 |
| stereo | 128 | export | 8.11 | 12.00 | 29.80 | 0 | 51 |
| stereo | 128 | scheduled-export | 7.76 | 11.00 | 13.80 | 0 | 51 |
| stereo | 256 | original | 14.61 | 20.70 | 31.30 | 0 | 34 |
| stereo | 256 | export | 14.67 | 18.10 | 31.20 | 0 | 51 |
| stereo | 256 | scheduled-export | 15.00 | 22.20 | 32.60 | 0 | 51 |
| rates | 128 | original | 5.21 | 14.90 | 26.20 | 0 | 24 |
| rates | 128 | export | 11.15 | 18.40 | 29.60 | 0 | 55 |
| rates | 128 | scheduled-export | 11.43 | 23.20 | 46.00 | 0 | 55 |
| rates | 256 | original | 9.43 | 11.20 | 60.10 | 0 | 24 |
| rates | 256 | export | 20.36 | 60.40 | 92.10 | 0 | 55 |
| rates | 256 | scheduled-export | 20.35 | 33.60 | 54.00 | 0 | 55 |
| analysis | 128 | original | 13.72 | 348.30 | 372.40 | 0 | 13 |
| analysis | 128 | export | 26.44 | 705.80 | 1052.50 | 0 | 28 |
| analysis | 128 | scheduled-export | 26.45 | 712.20 | 930.30 | 0 | 28 |
| analysis | 256 | original | 26.86 | 370.20 | 473.10 | 0 | 13 |
| analysis | 256 | export | 50.91 | 734.70 | 864.80 | 0 | 28 |
| analysis | 256 | scheduled-export | 50.78 | 732.90 | 1015.00 | 0 | 28 |

## Gerätebetrieb

Aktivierte Render-Telemetrie, echter VB-Cable-Capture und Surface-Ausgabe. Der physische Ausgang bleibt im DSP stumm; die Analyse davor prüft den Testton. p99 ist hier das Maximum der abgefragten Ein-Sekunden-p99, **kein Sitzungs-p99**. Das Maximum und die Fehlerzähler beziehen sich dagegen auf den gesamten Messlauf. Fehlende Treiber-Deadlines und die Ende-zu-Ende-Latenz werden hier nicht gemessen.

| Lauf | Host | Sekunden | Blöcke | höchster Fenster-p99 µs | Maximum µs | über Budget | Capture U/O | Playback U/O | Priming |
|---|---|---:|---:|---:|---:|---:|---|---|---:|
| steady-1 | consumer | 120.2 | 44981 | 1592.9 | 2542.2 | 0 | 0/0 | 0/0 | 0 |
| steady-1 | producer | 120.2 | 44981 | 40.0 | 198.6 | 0 | 0/0 | 0/0 | 0 |
| steady-2 | consumer | 120.0 | 44916 | 1695.0 | 2249.6 | 0 | 0/0 | 0/0 | 0 |
| steady-2 | producer | 120.0 | 44918 | 35.7 | 413.6 | 0 | 0/0 | 0/0 | 0 |
| steady-3 | consumer | 120.2 | 44993 | 1531.0 | 2425.8 | 0 | 0/0 | 0/0 | 0 |
| steady-3 | producer | 120.2 | 44994 | 42.9 | 229.5 | 0 | 0/0 | 0/0 | 0 |
| live | consumer | 300.0 | 112471 | 1424.5 | 2748.9 | 1 | 0/0 | 0/0 | 0 |
| live | producer | 300.0 | 112473 | 36.8 | 283.5 | 0 | 0/0 | 0/0 | 0 |

Live-Lauf: 2444 Graphänderungen; 2444 Analyseabfragen erkannten 440 Hz mit Konfidenz > 0,9.

