# Analyse und Messung

| Frage | Signalweg |
| --- | --- |
| Wie sieht das Signal zeitlich aus? | Audio → Oscilloscope |
| Wie hoch sind Peak und RMS? | Audio → Level Detector |
| Welche Frequenzen sind enthalten? | Audio → FFT → Analysis Viewer |
| Welche spektralen Eigenschaften ändern sich? | FFT → Spectral Features → Value Viewer |
| Welche Periodizität liegt vor? | FFT → Autocorrelation → Analysis Viewer |
| Welche Teiltöne werden verfolgt? | Partial Analysis → Partial Group |

Level Detector misst keine LUFS. Autocorrelation ist kein rekonstruierbares Spektrum. Peak, RMS, Amplitude, Leistung und dB sind unterschiedliche Größen. Kanalmodus Mix mittelt Links/Rechts; gegenphasige Anteile können sich auslöschen. FFT-Auflösung folgt dem Fenster, nicht der Plotgröße.

## Anzeigen und exportieren

Doppelklick vergrößert eine Anzeige. **Freeze** hält die Darstellung, nicht Audio, an. Klicke für Messcursor A/B; Shift+Klick setzt B. Mausrad zoomt horizontal, Shift+Rad vertikal, mittlere Maustaste verschiebt. Das Kontextmenü setzt Ansicht oder Cursor zurück.

Export bietet CSV und Bilder aus einem festgehaltenen Snapshot. CSV enthält Messwerte statt bildschirmreduzierter Linien; wähle Dezimalpunkt/Komma passend zum Zielprogramm. Die Einheit folgt dem Anzeigemodus, ohne automatische dB-/Leistungsumrechnung. Empfangszeitstempel sind keine samplegenauen Aufnahmezeitpunkte. Notiere Samplerate, FFT-Fenster und Modus beim Vergleich.

## Endliche Messungen

**Value Average** mittelt Zahlen; **Analysis Average** Spektren oder Autokorrelation. Start triggert ein endliches Messfenster, Finished meldet das Ende, das Ergebnis bleibt stehen. Spektrale Mittelung erfolgt auf Leistung, nicht durch Mittelung beliebiger Phasen. **Measurement Recorder** sammelt Messreihen zum Export. Die Node-Kurzreferenzen erklären die jeweiligen Messmodi und Grenzen.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/analysis.md)
