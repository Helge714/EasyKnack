# Formula Group – Formeln

Erstelle eine Formula Group und definiere ihre Ports in Group-IO. Verwende die dortigen Code-Bezeichner im Editor. Die anfänglichen numerischen Ports heißen `signal` und `result`:

```c
result = clamp(signal * 2, -1, 1);
```

Weise jedem numerischen Ausgang auf jedem Ausführungspfad einen Wert zu. Trigger-Eingänge sind boolesch; ein wahrer Trigger-Ausgang erzeugt ein Ereignis und startet bei jeder Auswertung wieder falsch.

Lokale Variablen gelten nur für eine Auswertung. Dauerhafte Werte stehen als `state` vor den übrigen Anweisungen. Mit einem zusätzlichen Eingang `frequency` in Hz und Audio-Rate:

```c
state double phase = 0;
result = sin(tau * phase);
phase += frequency / sampleRate;
phase -= floor(phase);
```

Auto, Audio oder Control bestimmen die Auswertungsrate. Die Sprache unterstützt numerische Typen, Casts, if/else und die angebotenen Mathematikfunktionen, sowie eigene typisierte Funktionen und begrenzte for-Schleifen. Beliebige C++-Aufrufe, Pointer, Rekursion und while-Schleifen sind nicht verfügbar. Syntax und Grenzen stehen in der [Node-Referenz](../../nodes/de/formula-group.md#eigene-funktionen-und-begrenzte-schleifen). Vervollständigung und Hover-Hilfe zeigen verfügbare Funktionen und Kontextwerte für Zeit und Zufall. Digital Noise ist eine Alternative für zeitlich wechselnde Zufallssteuerung.

Buffer-Eingänge erlauben indizierte Reads und Metadaten; ein Audioeingang besitzt keine beliebig abrufbare Vergangenheit. **Live** übernimmt gültigen Code nach einer kurzen Pause; sonst **Apply** verwenden. Ungültiger Code zeigt Diagnosen. Gespeichert werden Code und Anfangszustände, nicht der laufende DSP-Zustand.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/formula-group.md)
