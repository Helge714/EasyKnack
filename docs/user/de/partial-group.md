# Partial Group – Teiltöne bearbeiten

**Partial Analysis** erkennt und verfolgt sinusförmige Komponenten. Anders als FFT-Bins besitzen Partials zeitlich verfolgte IDs und unabhängig veränderbare Frequenz, Amplitude und Phase.

Ein typischer Weg ist `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Math (Add) → IFFT`. Verbinde das Residualspektrum der Analyse mit dem zweiten Add-Eingang, wenn der nichtsinusförmige Anteil erhalten bleiben soll.

Im Inneren liefert **Partial Context** die Teiltonwerte; bearbeite sie mit Math oder Formula Group und schreibe sie mit **Partial Output** zurück. Zustände sind pro ID getrennt und werden bei einer neuen ID zurückgesetzt. **Partial Noise** liefert unabhängige Zufallssteuerung mit stabilen IDs; `2 * random - 1` ergibt eine symmetrische Abweichung.

Die Gruppe arbeitet einmal pro Analyseframe und Teilton, nicht als Audio-Oszillatorbank. Sie unterstützt bis zu 64 numerische Prozessoren einschließlich aufgelöster normaler Gruppen und numerischer Formula Groups. Buffer und Triggerprogramme gehören nicht in diesen Kontext. Externe Zahlen werden am Fensterende aus dem aktuellen Veröffentlichungsblock gelesen; historische asynchrone Modulationswerte sind nicht gepuffert.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/partial-group.md)
