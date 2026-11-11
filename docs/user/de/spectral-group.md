# Spectral Group – Bins bearbeiten

Eine Spectral Group bearbeitet die Bins eines eingehenden Spectrum. Der Spectrum-Eingang bestimmt Binanzahl und Framezeiten. **Bin Context** liefert Real-/Imaginärteil, Betrag, Phase, Frequenz und Index; **Bin Reader** liest andere Bins eines ausdrücklich verbundenen Spektrums.

Verbinde für eine einfache Verstärkung `Magnitude * Faktor` mit Bin Output und reiche Phase unverändert durch. **Bin Output** schreibt komplexe Frame-Werte, wahlweise als Real/Imaginary oder Magnitude/Phase. Mehrere Bin Outputs erzeugen unabhängige Spektren. Zwei komplementäre Masken `m` und `1-m` auf die ursprünglichen komplexen Werte ergeben zusammen wieder das Eingangsspektrum.

**Bin Discard** verwirft einen Bin für alle Ausgänge. **Skip empty input bins** überspringt exakt leere Eingangskoeffizienten bereits vor der Berechnung: ausschalten, wenn du neue Bins erzeugst oder andere Bins liest. **Parallel bins** kann geeignete unabhängige Berechnungen parallel ausführen; kleine Programme oder Audio pro Bin bleiben seriell.

Externe numerische Eingänge werden zum Fensterende pro Frame abgetastet und gelten für dessen Bins. Frame-Trigger gehören zum entsprechenden Frame. Bin Random, Bin Sample & Hold und Bin Smoothing unterstützen Zufall, Halten und Glätten pro Bin.

Frame-Verarbeitung läuft pro Bin und Frame. Oszillatoren innerhalb der Gruppe erzeugen dagegen kontinuierliches Audio je Bin; dafür **Instance Merge** verwenden. Bin Output nimmt dieses Audio nicht an. Eine Oszillatorbank aus Beträgen ist keine phasengleiche IFFT.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/spectral-group.md)
