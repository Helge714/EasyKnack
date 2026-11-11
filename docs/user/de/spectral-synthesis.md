# Spektrale Synthese und Bearbeitung

Beginne für eine transparente Verarbeitung mit `Audio → FFT → IFFT → Audio Output`. Füge dann Spectral Group oder Spectrum-Prozessoren zwischen FFT und IFFT ein. Verwende **synchrone FFT** für kontinuierliche Rekonstruktion; asynchrone Analyse kann Frames verlieren und dadurch Lücken oder Pegeländerungen erzeugen.

Der Binabstand beträgt `Samplerate / FFT-Größe`. Größere Fenster trennen nahe Frequenzen besser, decken aber mehr Zeit ab und erhöhen Speicherbedarf und Latenz. Zero Padding verdichtet die Darstellung, schafft keine neue Information. Overlap bestimmt den Frameabstand und Rechenaufwand. FFT/IFFT fügt etwa eine Fensterlänge zusätzlich zur Gerätelatenz hinzu.

Ein komplexer Koeffizient enthält Betrag und Phase. Für reine Verstärkung beide komplexen Komponenten skalieren oder den Betrag ändern und die Phase behalten. FFT-Rohwerte sind weder automatisch normierte Amplituden noch dB. Fensterfunktion, Kanalwahl, DC-Entfernung und Phase verändern das Ergebnis.

**Bin Width** verteilt Energie auf Nachbarfrequenzen; Bins bedeuten lineare, Oktaven logarithmische Breite. Leistungsnormalisierung garantiert keine konstanten Peaks oder unveränderte Lautheit. **Spectrum Prune** entfernt schwächere Bins. Für eigene Umverteilung nutze Bin Reader in einer Spectral Group und beurteile auch das rekonstruierte Audio.

IFFT rekonstruiert komplexe Frames mit ihrem Zeit- und Fensterbezug. Eine additive Oszillatorbank kann andere Frequenzen und kontinuierliche Einzelzustände verwenden und ist deshalb nicht gleichwertig. **Sine Cloud** ist eine spezielle Quelle für sehr viele gleichmäßig angeordnete Sinusse; ihr endlicher Periodenbuffer wird unverändert wiederholt. Phasenrandomisierung und gaußförmige Frequenzdichte sind optional; die einzelnen Sinusse behalten gleiche Amplitude.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/spectral-synthesis.md)
