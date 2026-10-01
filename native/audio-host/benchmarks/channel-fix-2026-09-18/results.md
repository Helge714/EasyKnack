# Referenzmessung

28 Gleichheitsfälle; maximaler absoluter Fehler: 0; Bitabweichungen: 0.

Offline: Stereo-Aufruf, 48 kHz, drei Wiederholungen mit rotierter Reihenfolge. Die Tabelle zeigt den Median der drei Mittelwerte und den höchsten p99 der drei Läufe. Budgetüberschreitungen sind virtuelle Blockbudgets, keine gemessenen Geräte-Unterläufe. Samplebuffer enthalten Stimmen und eingebettete Graphen, ohne doppelte Zählung gemeinsam genutzter Speicherbereiche. Prozessorzustand, MIDI, Heap-Overhead und Programmcode fehlen in dieser Speicherzahl.

| Projekt | Frames | Modus | Mittel µs | höchster p99 µs | Maximum µs | über Budget / 1200 | Samplebuffer KiB |
|---|---:|---|---:|---:|---:|---:|---:|
| math | 128 | original | 6.21 | 17.10 | 20.20 | 0 | 28 |
| math | 128 | export | 4.04 | 15.10 | 18.10 | 0 | 41 |
| math | 128 | scheduled-export | 8.05 | 17.30 | 42.50 | 0 | 41 |
| math | 256 | original | 9.81 | 15.00 | 45.90 | 0 | 28 |
| math | 256 | export | 5.44 | 8.10 | 14.70 | 0 | 41 |
| math | 256 | scheduled-export | 10.68 | 19.00 | 42.80 | 0 | 41 |
| synth-1 | 128 | original | 28.06 | 49.40 | 94.40 | 0 | 30 |
| synth-1 | 128 | export | 28.94 | 47.90 | 78.30 | 0 | 48 |
| synth-1 | 128 | scheduled-export | 28.68 | 45.50 | 67.40 | 0 | 48 |
| synth-1 | 256 | original | 55.07 | 81.60 | 261.70 | 0 | 30 |
| synth-1 | 256 | export | 58.10 | 99.60 | 147.90 | 0 | 48 |
| synth-1 | 256 | scheduled-export | 58.06 | 102.50 | 254.80 | 0 | 48 |
| synth-8 | 128 | original | 218.79 | 402.80 | 596.10 | 0 | 191 |
| synth-8 | 128 | export | 217.94 | 338.40 | 436.90 | 0 | 265 |
| synth-8 | 128 | scheduled-export | 223.60 | 367.70 | 726.30 | 0 | 265 |
| synth-8 | 256 | original | 434.62 | 705.90 | 897.40 | 0 | 191 |
| synth-8 | 256 | export | 482.28 | 732.80 | 1102.50 | 0 | 265 |
| synth-8 | 256 | scheduled-export | 434.43 | 705.60 | 785.00 | 0 | 265 |
| synth-32 | 128 | original | 869.78 | 1343.70 | 1608.10 | 0 | 743 |
| synth-32 | 128 | export | 882.73 | 1384.60 | 35781.60 | 2 | 1009 |
| synth-32 | 128 | scheduled-export | 891.60 | 1431.00 | 4299.60 | 1 | 1009 |
| synth-32 | 256 | original | 1664.39 | 2398.30 | 3626.70 | 0 | 743 |
| synth-32 | 256 | export | 1963.14 | 3241.00 | 28053.40 | 2 | 1009 |
| synth-32 | 256 | scheduled-export | 1662.49 | 2749.40 | 3515.00 | 0 | 1009 |
| stereo | 128 | original | 7.46 | 12.10 | 23.20 | 0 | 34 |
| stereo | 128 | export | 7.78 | 10.40 | 35.50 | 0 | 51 |
| stereo | 128 | scheduled-export | 7.76 | 8.60 | 13.00 | 0 | 51 |
| stereo | 256 | original | 14.77 | 23.80 | 62.20 | 0 | 34 |
| stereo | 256 | export | 14.80 | 17.40 | 55.40 | 0 | 51 |
| stereo | 256 | scheduled-export | 14.93 | 21.70 | 35.60 | 0 | 51 |
| rates | 128 | original | 5.20 | 7.90 | 17.40 | 0 | 24 |
| rates | 128 | export | 5.86 | 8.10 | 39.70 | 0 | 42 |
| rates | 128 | scheduled-export | 5.80 | 14.10 | 23.30 | 0 | 42 |
| rates | 256 | original | 9.56 | 13.40 | 32.90 | 0 | 24 |
| rates | 256 | export | 10.32 | 15.20 | 34.30 | 0 | 42 |
| rates | 256 | scheduled-export | 10.13 | 13.60 | 140.80 | 0 | 42 |
| analysis | 128 | original | 14.45 | 369.70 | 471.90 | 0 | 13 |
| analysis | 128 | export | 13.93 | 390.40 | 437.80 | 0 | 23 |
| analysis | 128 | scheduled-export | 14.55 | 384.00 | 442.90 | 0 | 23 |
| analysis | 256 | original | 26.63 | 360.00 | 504.30 | 0 | 13 |
| analysis | 256 | export | 27.17 | 476.90 | 573.40 | 0 | 23 |
| analysis | 256 | scheduled-export | 26.99 | 360.00 | 447.40 | 0 | 23 |
