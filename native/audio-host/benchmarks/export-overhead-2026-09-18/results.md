# Referenzmessung

28 Gleichheitsfälle; maximaler absoluter Fehler: 0; Bitabweichungen: 0.

Offline: Stereo-Aufruf, 48 kHz, 4 Wiederholungen mit rotierter Reihenfolge. Die Tabelle zeigt den Median der Mittelwerte und den höchsten p99 der Läufe. Budgetüberschreitungen sind virtuelle Blockbudgets, keine gemessenen Geräte-Unterläufe. Samplebuffer enthalten Stimmen und eingebettete Graphen, ohne doppelte Zählung gemeinsam genutzter Speicherbereiche. Prozessorzustand, MIDI, Heap-Overhead und Programmcode fehlen in dieser Speicherzahl.

| Projekt | Frames | Modus | Mittel µs | höchster p99 µs | Maximum µs | über Budget / Blöcke | Samplebuffer KiB |
|---|---:|---|---:|---:|---:|---:|---:|
| math | 128 | original | 5.07 | 7.70 | 13.90 | 0 / 1600 | 28 |
| math | 128 | export | 2.89 | 4.10 | 9.30 | 0 / 1600 | 39 |
| math | 128 | scheduled-export | 5.42 | 7.00 | 54.70 | 0 / 1600 | 39 |
| math | 128 | copy-export | 2.79 | 4.00 | 11.10 | 0 / 1600 | 39 |
| math | 256 | original | 10.56 | 18.60 | 28.50 | 0 / 1600 | 28 |
| math | 256 | export | 5.11 | 7.20 | 13.20 | 0 / 1600 | 39 |
| math | 256 | scheduled-export | 10.62 | 17.20 | 319.00 | 0 / 1600 | 39 |
| math | 256 | copy-export | 5.32 | 8.30 | 19.70 | 0 / 1600 | 39 |
| synth-1 | 128 | original | 26.79 | 44.30 | 77.70 | 0 / 1600 | 30 |
| synth-1 | 128 | export | 27.45 | 59.30 | 95.60 | 0 / 1600 | 46 |
| synth-1 | 128 | scheduled-export | 27.20 | 57.20 | 104.20 | 0 / 1600 | 46 |
| synth-1 | 128 | copy-export | 27.69 | 42.80 | 139.80 | 0 / 1600 | 46 |
| synth-1 | 256 | original | 53.22 | 84.40 | 134.50 | 0 / 1600 | 30 |
| synth-1 | 256 | export | 53.71 | 81.60 | 251.30 | 0 / 1600 | 46 |
| synth-1 | 256 | scheduled-export | 54.13 | 99.20 | 646.00 | 0 / 1600 | 46 |
| synth-1 | 256 | copy-export | 53.48 | 75.30 | 154.90 | 0 / 1600 | 46 |
| synth-8 | 128 | original | 214.11 | 348.50 | 561.20 | 0 / 1600 | 191 |
| synth-8 | 128 | export | 212.81 | 328.10 | 417.90 | 0 / 1600 | 263 |
| synth-8 | 128 | scheduled-export | 213.28 | 357.30 | 577.60 | 0 / 1600 | 263 |
| synth-8 | 128 | copy-export | 217.92 | 433.30 | 1949.90 | 0 / 1600 | 263 |
| synth-8 | 256 | original | 414.92 | 642.30 | 800.70 | 0 / 1600 | 191 |
| synth-8 | 256 | export | 424.31 | 698.90 | 940.60 | 0 / 1600 | 263 |
| synth-8 | 256 | scheduled-export | 417.94 | 656.30 | 780.10 | 0 / 1600 | 263 |
| synth-8 | 256 | copy-export | 424.45 | 726.10 | 1087.50 | 0 / 1600 | 263 |
| synth-32 | 128 | original | 830.42 | 1280.70 | 1433.50 | 0 / 1600 | 743 |
| synth-32 | 128 | export | 833.77 | 1226.20 | 1952.80 | 0 / 1600 | 1007 |
| synth-32 | 128 | scheduled-export | 838.37 | 1349.60 | 2065.50 | 0 / 1600 | 1007 |
| synth-32 | 128 | copy-export | 836.02 | 1258.20 | 2094.90 | 0 / 1600 | 1007 |
| synth-32 | 256 | original | 1642.23 | 2267.90 | 2587.50 | 0 / 1600 | 743 |
| synth-32 | 256 | export | 1653.86 | 2450.80 | 2733.00 | 0 / 1600 | 1007 |
| synth-32 | 256 | scheduled-export | 1655.52 | 2361.30 | 3482.30 | 0 / 1600 | 1007 |
| synth-32 | 256 | copy-export | 1669.41 | 2441.20 | 2907.50 | 0 / 1600 | 1007 |
| stereo | 128 | original | 7.63 | 14.10 | 22.50 | 0 / 1600 | 34 |
| stereo | 128 | export | 7.82 | 12.20 | 22.00 | 0 / 1600 | 47 |
| stereo | 128 | scheduled-export | 7.94 | 12.20 | 24.20 | 0 / 1600 | 47 |
| stereo | 128 | copy-export | 7.90 | 19.30 | 35.40 | 0 / 1600 | 47 |
| stereo | 256 | original | 17.72 | 40.50 | 73.00 | 0 / 1600 | 34 |
| stereo | 256 | export | 15.66 | 36.60 | 174.40 | 0 / 1600 | 47 |
| stereo | 256 | scheduled-export | 17.06 | 37.20 | 62.80 | 0 / 1600 | 47 |
| stereo | 256 | copy-export | 18.49 | 43.10 | 133.80 | 0 / 1600 | 47 |
| rates | 128 | original | 5.68 | 14.70 | 37.30 | 0 / 1600 | 24 |
| rates | 128 | export | 6.37 | 16.20 | 50.90 | 0 / 1600 | 40 |
| rates | 128 | scheduled-export | 6.11 | 10.40 | 42.90 | 0 / 1600 | 40 |
| rates | 128 | copy-export | 6.26 | 10.20 | 20.10 | 0 / 1600 | 40 |
| rates | 256 | original | 9.82 | 24.30 | 32.40 | 0 / 1600 | 24 |
| rates | 256 | export | 9.98 | 13.80 | 26.30 | 0 / 1600 | 40 |
| rates | 256 | scheduled-export | 10.33 | 21.70 | 60.60 | 0 / 1600 | 40 |
| rates | 256 | copy-export | 11.19 | 23.40 | 398.70 | 0 / 1600 | 40 |
| analysis | 128 | original | 15.80 | 509.50 | 608.50 | 0 / 1600 | 13 |
| analysis | 128 | export | 17.81 | 520.30 | 1853.60 | 0 / 1600 | 18 |
| analysis | 128 | scheduled-export | 17.21 | 489.70 | 586.80 | 0 / 1600 | 18 |
| analysis | 128 | copy-export | 19.10 | 554.10 | 685.20 | 0 / 1600 | 18 |
| analysis | 256 | original | 33.63 | 565.80 | 797.30 | 0 / 1600 | 13 |
| analysis | 256 | export | 32.31 | 552.20 | 658.10 | 0 / 1600 | 18 |
| analysis | 256 | scheduled-export | 30.26 | 529.50 | 753.70 | 0 / 1600 | 18 |
| analysis | 256 | copy-export | 32.45 | 549.70 | 751.90 | 0 / 1600 | 18 |
