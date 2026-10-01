"""Summarize raw reference CSVs without treating polling windows as individual blocks."""
import argparse
import csv
import json
import pathlib
import statistics


def summarize(offline, routing):
    rows=list(csv.DictReader((offline / "offline.csv").open()))
    equality=list(csv.DictReader((offline / "equivalence.csv").open()))
    repetitions=len({r["repeat"] for r in rows})
    result=["# Referenzmessung", "",
            f"{len(equality)} Gleichheitsfälle; maximaler absoluter Fehler: "
            f"{max(float(r['max_abs_error']) for r in equality):g}; "
            f"Bitabweichungen: {sum(int(r['bit_differences']) for r in equality)}.", "",
            f"Offline: Stereo-Aufruf, 48 kHz, {repetitions} Wiederholungen mit rotierter Reihenfolge. "
            "Die Tabelle zeigt den Median der Mittelwerte und den höchsten p99 der Läufe. "
            "Budgetüberschreitungen sind virtuelle Blockbudgets, keine gemessenen Geräte-Unterläufe. "
            "Samplebuffer enthalten Stimmen und eingebettete Graphen, ohne doppelte Zählung gemeinsam "
            "genutzter Speicherbereiche. Prozessorzustand, MIDI, Heap-Overhead und Programmcode fehlen in dieser Speicherzahl.", "",
            "| Projekt | Frames | Modus | Mittel µs | höchster p99 µs | Maximum µs | über Budget / Blöcke | Samplebuffer KiB |",
            "|---|---:|---|---:|---:|---:|---:|---:|"]
    keys=list(dict.fromkeys((r["scenario"],r["frames"],r["mode"]) for r in rows))
    for scenario,frames,mode in keys:
        selected=[r for r in rows if (r["scenario"],r["frames"],r["mode"])==(scenario,frames,mode)]
        result.append(f"| {scenario} | {frames} | {mode} | "
                      f"{statistics.median(float(r['mean_us']) for r in selected):.2f} | "
                      f"{max(float(r['p99_us']) for r in selected):.2f} | "
                      f"{max(float(r['max_us']) for r in selected):.2f} | "
                      f"{sum(int(r['over_budget']) for r in selected)} / {sum(int(r['blocks']) for r in selected)} | "
                      f"{max(int(r['sample_buffer_bytes']) for r in selected)/1024:g} |")
    if routing and (routing/"summary.json").exists():
        result += ["", "## Gerätebetrieb", "",
                   "Aktivierte Render-Telemetrie, echter VB-Cable-Capture und Surface-Ausgabe. "
                   "Der physische Ausgang bleibt im DSP stumm; die Analyse davor prüft den Testton. "
                   "p99 ist hier das Maximum der abgefragten Ein-Sekunden-p99, **kein Sitzungs-p99**. "
                   "Das Maximum und die Fehlerzähler beziehen sich dagegen auf den gesamten Messlauf. "
                   "Fehlende Treiber-Deadlines und die Ende-zu-Ende-Latenz werden hier nicht gemessen.", "",
                   "| Lauf | Host | Sekunden | Blöcke | höchster Fenster-p99 µs | Maximum µs | über Budget | Capture U/O | Playback U/O | Priming |",
                   "|---|---|---:|---:|---:|---:|---:|---|---|---:|"]
        for run in json.loads((routing/"summary.json").read_text()):
            polls=list(csv.DictReader((routing/(run["run"]+".csv")).open()))
            for host in ("consumer","producer"):
                d=run[host].split()
                p99=max(float(row[host+"_diagnostics"].split()[6]) for row in polls)
                result.append(f"| {run['run']} | {host} | {run['elapsed_s']:.1f} | {d[3]} | {p99:.1f} | {d[7]} | {d[8]} | {d[12]}/{d[13]} | {d[14]}/{d[15]} | {d[17]} |")
        result += [""]
        live=next((r for r in json.loads((routing/"summary.json").read_text()) if r["run"]=="live"),None)
        if live:
            result += [f"Live-Lauf: {live['edits']} Graphänderungen; {live['tone_frames']} Analyseabfragen erkannten 440 Hz mit Konfidenz > 0,9.", ""]
    return "\n".join(result)+"\n"


if __name__ == "__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--offline",type=pathlib.Path,default=pathlib.Path("target/reference-v1"))
    parser.add_argument("--routing",type=pathlib.Path,default=pathlib.Path("target/reference-routing"))
    parser.add_argument("--output",type=pathlib.Path,default=pathlib.Path("target/reference-v1/results.md"))
    args=parser.parse_args()
    args.output.write_text(summarize(args.offline,args.routing),encoding="utf8")
