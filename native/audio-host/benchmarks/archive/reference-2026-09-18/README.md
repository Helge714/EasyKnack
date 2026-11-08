# Baseline vom 18.09.2026

- `environment.json`: Rechner, Energieprofil, Compilerflags und Hashes der gemessenen Programme.
- `equivalence.csv`: 28 Original-/Export-/Scheduler-Vergleiche einschließlich Livewechseln.
- `offline.csv`: 126 Messreihen mit Laufzeit und gezählten Samplebuffern.
- `routing.zip`: vollständige CSV-Verläufe, Metadaten und Zusammenfassung der vier Geräte-Langläufe.
- `results.md`: aus den Rohdaten erzeugte Tabellen.

Der Hardwarelauf verwendet 128 Frames/48 kHz, einen Producer-Host in VB-Cable und einen
Consumer-Host an den Surface-Lautsprechern. Der physische Ausgang ist im DSP stumm.
Im archivierten `metadata.json` beschreibt `clock` noch den Zustand vor Auswahl der
Routen; die tatsächliche Clock und die Capture-Puffer stehen in jedem Diagnosesnapshot.
Die Diagnosezeichenfolgen entsprechen `DIAGNOSTICS` Version 1.

Die Offline-Messung wurde separat vor dem Geräte-Langlauf und ohne parallelen Build
aufgenommen. Der spätere vollständige Maven-Lauf wiederholt die Tests und schreibt
neue Ergebnisse nach `target/reference-v1`; diese ersetzen diese archivierte Baseline nicht.

Nach Entpacken von `routing.zip` in ein separates Verzeichnis lässt sich der Bericht
vom Projektverzeichnis aus wieder erzeugen:

```powershell
python scripts/summarize-references.py --offline native/audio-host/benchmarks/archive/reference-2026-09-18 --routing <entpacktes-Verzeichnis> --output target/reference-report.md
```

Einordnung, offene Telemetrie-An/Aus-Prüfung und nächste Schritte:
[DSP-BASELINE.md](../../../../../docs/development/DSP-BASELINE.md).
