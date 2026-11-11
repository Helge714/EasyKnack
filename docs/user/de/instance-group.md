# Instance Group – Instanzgruppe

Eine Instance Group führt denselben Graphen für mehrere unabhängige Instanzen aus, etwa für eine Oszillatorbank oder mehrere Buffer Reader. Jede Instanz behält ihren eigenen Zustand. Instanzen sind keine Stereo-Kanäle.

**Maximum** reserviert die Kapazität (1–256); **Count** wählt die aktive Anzahl bis zu diesem Maximum. Größeres Maximum benötigt mehr Speicher. **Instance Context** liefert den nullbasierten Index, Count, die normierte Position und einen stabilen Seed. Bei einer Instanz ist Position 0.

Mit **Instance Merge** führst du Beiträge als Summe, Mittelwert oder gewichteten Mittelwert zusammen. Verarbeitung hinter dem Merge wird gemeinsam ausgeführt. Beginne beispielsweise mit `Instance Context → Frequenzberechnung → Oscillator → Instance Merge`.

Öffentliche Eingänge unterstützen Zahlen, Buffer, Vector, Wavetable und Spectrum; Ausgänge Zahlen oder Spectrum. MIDI und andere Ressourcengrenzen sind nicht unterstützt. Externe Buffer werden gemeinsam gelesen, der Reader-Zustand bleibt individuell. Interne Live-Buffer gehören jeweils ihrer Instanz.

Verschachtelte Instance-/Spectral Groups sind möglich, bleiben aber durch Graph- und Speicherbudgets begrenzt. Count ist eine Sonderfunktion der Instance Group, kein gewöhnlicher IO-Port. Ein kopierter Count-Editor an einer äußeren normalen Gruppe bleibt dort ein gewöhnlicher öffentlicher Editor.

Spectrum-Ausgänge werden gemäß **Instance mix: sum/mean** kombiniert. Dafür müssen FFT-Größe, Fenster, Hop, Samplerate und Framezeiten übereinstimmen. Audiokanäle bleiben getrennt; Spectrum verwendet nicht den Audio-Merge-Fade.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/instance-group.md)
