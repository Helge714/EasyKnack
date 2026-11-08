# Referenzmessung

32 Gleichheitsfälle; maximaler absoluter Fehler: 0; Bitabweichungen: 0.

Offline: Stereo-Aufruf, 48 kHz, 4 Wiederholungen mit rotierter Reihenfolge. Die Tabelle zeigt den Median der Mittelwerte und den höchsten p99 der Läufe. Budgetüberschreitungen sind virtuelle Blockbudgets, keine gemessenen Geräte-Unterläufe. Samplebuffer enthalten Stimmen und eingebettete Graphen, ohne doppelte Zählung gemeinsam genutzter Speicherbereiche. Prozessorzustand, MIDI, Heap-Overhead und Programmcode fehlen in dieser Speicherzahl.

| Projekt | Frames | Modus | Mittel µs | höchster p99 µs | Maximum µs | über Budget / Blöcke | Samplebuffer KiB |
|---|---:|---|---:|---:|---:|---:|---:|
| math | 128 | original | 5.41 | 9.50 | 20.30 | 0 / 1600 | 28 |
| math | 128 | export | 2.87 | 4.10 | 7.10 | 0 / 1600 | 39 |
| math | 128 | scheduled-export | 5.71 | 9.90 | 81.70 | 0 / 1600 | 39 |
| math | 128 | copy-export | 3.26 | 5.70 | 33.50 | 0 / 1600 | 39 |
| math | 256 | original | 10.85 | 25.70 | 148.00 | 0 / 1600 | 28 |
| math | 256 | export | 5.58 | 9.20 | 35.50 | 0 / 1600 | 39 |
| math | 256 | scheduled-export | 10.37 | 16.40 | 43.30 | 0 / 1600 | 39 |
| math | 256 | copy-export | 5.59 | 9.40 | 21.60 | 0 / 1600 | 39 |
| synth-1 | 128 | original | 25.87 | 59.00 | 120.40 | 0 / 1600 | 30 |
| synth-1 | 128 | export | 26.98 | 55.40 | 81.60 | 0 / 1600 | 46 |
| synth-1 | 128 | scheduled-export | 28.34 | 60.50 | 188.40 | 0 / 1600 | 46 |
| synth-1 | 128 | copy-export | 27.49 | 55.20 | 179.80 | 0 / 1600 | 46 |
| synth-1 | 256 | original | 52.86 | 108.10 | 180.80 | 0 / 1600 | 30 |
| synth-1 | 256 | export | 52.04 | 92.10 | 155.80 | 0 / 1600 | 46 |
| synth-1 | 256 | scheduled-export | 51.97 | 86.90 | 137.30 | 0 / 1600 | 46 |
| synth-1 | 256 | copy-export | 51.99 | 94.70 | 114.40 | 0 / 1600 | 46 |
| synth-8 | 128 | original | 290.60 | 449.10 | 617.50 | 0 / 1600 | 191 |
| synth-8 | 128 | export | 267.96 | 422.40 | 595.40 | 0 / 1600 | 263 |
| synth-8 | 128 | scheduled-export | 279.47 | 506.90 | 602.80 | 0 / 1600 | 263 |
| synth-8 | 128 | copy-export | 281.12 | 451.60 | 672.40 | 0 / 1600 | 263 |
| synth-8 | 256 | original | 578.10 | 870.90 | 972.00 | 0 / 1600 | 191 |
| synth-8 | 256 | export | 549.11 | 818.60 | 856.40 | 0 / 1600 | 263 |
| synth-8 | 256 | scheduled-export | 573.92 | 964.90 | 2354.60 | 0 / 1600 | 263 |
| synth-8 | 256 | copy-export | 564.79 | 875.70 | 907.30 | 0 / 1600 | 263 |
| synth-32 | 128 | original | 1331.90 | 3592.50 | 5213.60 | 20 / 1600 | 743 |
| synth-32 | 128 | export | 1235.22 | 3093.70 | 3604.10 | 10 / 1600 | 1007 |
| synth-32 | 128 | scheduled-export | 1128.62 | 2336.20 | 3530.90 | 2 / 1600 | 1007 |
| synth-32 | 128 | copy-export | 1376.97 | 3167.00 | 4027.80 | 18 / 1600 | 1007 |
| synth-32 | 256 | original | 2124.35 | 3090.20 | 3718.50 | 0 / 1600 | 743 |
| synth-32 | 256 | export | 2156.34 | 12784.90 | 65051.70 | 12 / 1600 | 1007 |
| synth-32 | 256 | scheduled-export | 2181.05 | 3461.60 | 3532.30 | 0 / 1600 | 1007 |
| synth-32 | 256 | copy-export | 2216.45 | 3216.90 | 4465.20 | 0 / 1600 | 1007 |
| stereo | 128 | original | 11.01 | 19.50 | 26.50 | 0 / 1600 | 34 |
| stereo | 128 | export | 10.89 | 21.70 | 101.50 | 0 / 1600 | 47 |
| stereo | 128 | scheduled-export | 10.79 | 16.60 | 39.40 | 0 / 1600 | 47 |
| stereo | 128 | copy-export | 10.97 | 15.90 | 49.60 | 0 / 1600 | 47 |
| stereo | 256 | original | 20.65 | 44.60 | 124.10 | 0 / 1600 | 34 |
| stereo | 256 | export | 20.94 | 45.70 | 62.90 | 0 / 1600 | 47 |
| stereo | 256 | scheduled-export | 20.27 | 37.40 | 74.10 | 0 / 1600 | 47 |
| stereo | 256 | copy-export | 21.38 | 46.60 | 66.40 | 0 / 1600 | 47 |
| rates | 128 | original | 7.73 | 12.60 | 18.70 | 0 / 1600 | 24 |
| rates | 128 | export | 8.21 | 13.00 | 41.80 | 0 / 1600 | 40 |
| rates | 128 | scheduled-export | 8.39 | 12.40 | 46.80 | 0 / 1600 | 40 |
| rates | 128 | copy-export | 8.40 | 34.40 | 111.70 | 0 / 1600 | 40 |
| rates | 256 | original | 13.08 | 21.70 | 52.10 | 0 / 1600 | 24 |
| rates | 256 | export | 14.53 | 25.80 | 57.60 | 0 / 1600 | 40 |
| rates | 256 | scheduled-export | 13.76 | 21.10 | 35.60 | 0 / 1600 | 40 |
| rates | 256 | copy-export | 13.70 | 19.40 | 182.40 | 0 / 1600 | 40 |
| analysis | 128 | original | 19.20 | 483.50 | 544.40 | 0 / 1600 | 13 |
| analysis | 128 | export | 20.01 | 486.70 | 687.30 | 0 / 1600 | 18 |
| analysis | 128 | scheduled-export | 19.47 | 495.60 | 577.50 | 0 / 1600 | 18 |
| analysis | 128 | copy-export | 19.30 | 491.20 | 667.70 | 0 / 1600 | 18 |
| analysis | 256 | original | 37.22 | 507.20 | 612.50 | 0 / 1600 | 13 |
| analysis | 256 | export | 37.29 | 537.20 | 665.80 | 0 / 1600 | 18 |
| analysis | 256 | scheduled-export | 36.96 | 508.30 | 604.10 | 0 / 1600 | 18 |
| analysis | 256 | copy-export | 37.11 | 494.60 | 572.50 | 0 / 1600 | 18 |
| osc-gain-filter | 128 | original | 8.18 | 14.20 | 56.70 | 0 / 1600 | 15 |
| osc-gain-filter | 128 | export | 7.87 | 11.90 | 17.20 | 0 / 1600 | 22 |
| osc-gain-filter | 128 | scheduled-export | 8.10 | 11.40 | 57.60 | 0 / 1600 | 22 |
| osc-gain-filter | 128 | copy-export | 7.80 | 12.00 | 18.30 | 0 / 1600 | 22 |
| osc-gain-filter | 256 | original | 15.47 | 41.60 | 77.10 | 0 / 1600 | 15 |
| osc-gain-filter | 256 | export | 15.25 | 38.60 | 52.60 | 0 / 1600 | 22 |
| osc-gain-filter | 256 | scheduled-export | 15.45 | 46.70 | 69.40 | 0 / 1600 | 22 |
| osc-gain-filter | 256 | copy-export | 15.13 | 40.00 | 47.00 | 0 / 1600 | 22 |
