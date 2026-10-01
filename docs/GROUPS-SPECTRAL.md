# Gruppen und Spektralverarbeitung

## Gruppeneinstellungen

Der Seitenbereich verwendet eine gemeinsame vertikale Tableiste:

- **Properties**: Eigenschaften der ausgewählten Nodes, immer verfügbar.
- **Editors**: wiederverwendbare Combobox-Vorlagen für das gesamte Dokument.
- **Group-General**: Gruppenname, Titel-Farbtyp, Add-Menü-Kategorie, Ports für Einfügen und Mute-Bypass.
- **Group-IO**: alle Inputs und Outputs, Reihenfolge, Typ, Editor, Wertebereich und erlaubte Einheiten.

Die letzten drei Tabs erscheinen nur innerhalb einer Gruppe. Alle Gruppenregler
sind Inputs; ein Input mit Editor darf seinen Anschluss ausblenden. Die IO-Liste
bestimmt auch die Reihenfolge der Editoren. Bestehende Gruppen-Comboboxen werden
beim Laden zu solchen Inputs.

Die Positionen der Input-/Output-Grenznodes werden mit dem Dokument gespeichert,
auch beim Speichern innerhalb einer geöffneten Gruppe.

### Eigene Combobox

1. Unter **Editors → Add combobox** eine Vorlage anlegen und benennen.
2. Einträge hinzufügen, benennen, sortieren und Zahlen- oder Textwerte zuweisen. **Set default** legt den Standard fest.
3. In **Group-IO** einen Input auswählen, **Editor → Combobox** und unter **Model** die Vorlage wählen.
4. Den Gruppeninput innerhalb der Gruppe mit den gewünschten Inputs verbinden. Diese Verbindungen bestimmen auch, welche Parameter die Auswahl steuert; eine separate Parameterbindung gibt es nicht.
5. **Assign indices 0, 1, ...** bildet die Einträge auf laufende Zahlenwerte ab. Bei angeschlossenen Comboboxen wählen ganzzahlige Indizes deren Einträge aus; stabile Enum-IDs sind ebenfalls möglich.

Vorlagen werden mit Namen, Einträgen, Werten und Standardauswahl in der `.knack`-Datei
gespeichert. Verwendende Inputs behalten zusätzlich ihre eigene Definition.
Die Modellauswahl übernimmt eine Kopie der Vorlage für den Input. Das Entfernen einer Vorlage aus der
Bibliothek verändert vorhandene Gruppen nicht.

Ein Editor darf über mehrere Verbindungen mehrere innere Inputs steuern.
Zahlenwerte verwenden die gespeicherte Einheit des Zielparameters.
**Editor → Copy Editor** übernimmt über **Connected editor** den Editor eines ausgewählten inneren Anschlusses,
etwa die Wellenform-Auswahl eines Oszillators oder einen Slider.
Diese Übernahme erzeugt eine lokale, mit dem Input gespeicherte Kopie und keine
neue Vorlage in der Bibliothek. Das Ausblenden eines äußeren Nodepoints lässt den
entsprechenden Eingang und seine Verbindungen innerhalb der Gruppe bestehen.
Bereits vorhandene Gruppen-Comboboxen erscheinen in der IO-Liste; verschachtelte
Gruppenauswahlen lassen sich weiterreichen.

Einträge besitzen stabile IDs. Umbenennen oder Sortieren ändert ihre Bindungen nicht.
Erst der explizite Befehl zum Zuweisen von Indizes nummeriert Werte neu.
Wird eine ausgewählte Option gelöscht, verwendet die Instanz den neuen Standard.
Entfernte Editoren behalten intern eine wirkungslose Eigenschaft für bestehende
Instanzen und Undo; ihre Oberfläche und Bindungen entfallen.
Alle Änderungen sind rückgängig machbar und bleiben beim Speichern/Export erhalten.
Auswahlen, die Anzahl oder Art der Ports verändern, werden nicht als direkt
weiterreichbare innere Combobox angeboten.

### Auswahl nach Rate

- **Trigger**: Button oder Copy Editor. Der Button löst die aktuell verbundenen inneren Triggerinputs aus.
- **Audio, Control, Auto**: Slider, Combobox oder Copy Editor.
- **Slider** zeigt Minimum, Maximum, Standardwert und erlaubte Einheiten. Leere Grenzen sind unbegrenzt.
- **Combobox** zeigt die Modellauswahl; **Copy Editor** zeigt die angeschlossenen Editoren.

Die Einstellungen anderer Editortypen bleiben ausgeblendet. Copy Editor erstellt keine
Bibliotheksvorlage. Bestehende ältere Dokumente behalten ihre gespeicherten Auswahlen.

## Audio aus Spektren rekonstruieren

**Datei → Spektral-Beispiel öffnen …** enthält eine fertige Kette:

`Oscillator → FFT Stream → Spectrum Shift → Spectral Group → IFFT → Audio Out`

Die Nodes stehen in der Kategorie **Spectral**. Eine zusätzliche bearbeitbare
Spektralgruppe entsteht über **Nodes → Add spectral group**. Sie lässt sich wie
eine normale Gruppe öffnen, speichern, duplizieren, lokal backen oder als Plugin exportieren.

**FFT Stream** und die bisherige Analyse-FFT haben unterschiedliche Aufgaben:
Die Analyse darf für die Anzeige Frames auslassen. Der neue Spectrum-Port trägt
alle komplexen FFT-Frames mit Zeitbezug auf dem Audiothread bis zur IFFT.
Spectrum-Verbindungen sind gelbe, gestrichelte Ressourcenverbindungen.
Die vorhandenen Analysis-Viewer arbeiten weiterhin mit dem Analysis-Datentyp.

FFT Stream bietet Fenstergrößen 256–8192 Samples, Hann/Hamming/Blackman/Rectangular
und 50 % oder 75 % Überlappung. **IFFT** rekonstruiert mit normalisiertem Overlap-Add.
Die Verzögerung einer FFT/IFFT-Kette beträgt genau eine Fensterlänge zusätzlich
zur Geräte-/Hostlatenz: 2048 Samples bei 48 kHz sind etwa 42,7 ms; 256 Samples etwa 5,3 ms.
Ohne Spektraländerung wird das Signal bis auf numerische Rundung rekonstruiert.
Stereo und MIDI-Stimmen bleiben unabhängig. Eine Größen-/Fensteränderung startet
die betroffene Spektralkette neu; bloße Änderungen am übrigen Graphen erhalten
kompatiblen FFT-/IFFT-Zustand.

## Einzelne Bins bearbeiten

Eine neue **Spectral Group** verbindet zunächst Real/Imaginary von **Bin Context**
direkt mit **Bin Output**, verändert also nichts.

**Bin Context** liefert für den gerade bearbeiteten positiven Frequenzbin:

| Ausgang | Bedeutung |
|---|---|
| Real / Imaginary | komplexer FFT-Koeffizient |
| Magnitude | Betrag des rohen FFT-Koeffizienten, nicht auf 0–1 normiert |
| Phase | Phase in Radiant |
| Frequency | Bin-Frequenz in Hz |
| Bin index | ganzzahliger Index 0 bis N/2 |
| Position | normierter Index 0 bis 1 |
| Frame time | Fensteranfang in Sekunden; während der Anlaufphase negativ |

Diese Zahlen werden **pro Bin** ausgewertet, nicht pro zeitlichem Audiosample.
Innerhalb der Gruppe können Math, Map Range, Curve, Mix, Clamp, Unit Converter und
Value verwendet werden, auch in normalen Untergruppen. Zeitabhängige Nodes wie
Delay, Oszillatoren oder MIDI gehören außerhalb dieser Bin-Berechnung.
Die Rate-Auswahl erzwingt hier keine zeitliche Blockabtastung: die Gruppe definiert
die Auswertung pro Bin. Innere Skalareinstellungen lassen sich über **Editors**
nach außen reichen; das Spektralinterface selbst besteht aus einem Spectrum-Eingang
und einem Spectrum-Ausgang.

Beispiele:

- **Spektrale Verstärkung:** Bin Context → Magnitude → Math Multiply → Bin Output Magnitude;
  Phase unverändert weiterreichen. Bin Output auf **Magnitude / phase** stellen.
- **Filterkurve:** Position → Curve, Kurvenwert mit Magnitude multiplizieren.
- **Nachbarbins:** Bin index → Math Add/Subtract → **Bin Read**. Dieser liest immer
  das unveränderte Eingangsspektrum, auch wenn andere Bins bereits berechnet wurden.
  Fractional indices interpolieren Real/Imaginary; außerhalb gilt Zero, Clamp oder Wrap.

Bin Output akzeptiert wahlweise Real/Imaginary oder Magnitude/Phase. Der Host
ergänzt die negativen Frequenzen konjugiert, hält DC/Nyquist reell und erzeugt so
ein reelles Audiosignal. Ungültige/nichtendliche Ergebniswerte werden zu Null.

## Bin-Verschiebung

**Spectrum Shift** verschiebt das gesamte Spektrum um die eingestellte Binzahl.
Positive Werte verschieben nach oben, negative nach unten. Ein Bin entspricht
`Samplerate / Fenstergröße` Hz. Fractional shifts interpolieren komplexe Bins;
Energie außerhalb des darstellbaren Bereichs entfällt.

**Continuous phase** korrigiert für konstante Verschiebungen die Phasenlage
zwischen überlappenden Frames. Das ist eine additive Frequenzverschiebung,
kein musikalischer Pitch-/Formantshifter. Bruchteile von Bins und schnelle
Modulation können weiterhin Färbung oder Artefakte verursachen.

Spektralgruppen verwenden einen vorbereiteten skalaren Ausführungsplan ohne
Speicherallokationen im Audiocallback. Bake/Plugin-Export unterstützt diesen Plan
einschließlich verschachtelter Gruppen und unabhängig schaltbarer Editoren;
eine zusätzliche vollständige SIMD-Fusion der Bin-Rechnung ist damit nicht zugesagt.
