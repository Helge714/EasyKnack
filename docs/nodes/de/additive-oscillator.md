# Additiver Oszillator

## Übersicht

Der **Additive Oscillator** erzeugt einen Klang aus bis zu 144 Teiltönen.
Die Grundfrequenz legt die Tonhöhe des ersten Teiltons fest. **Falloff** bestimmt
die Gewichtung der höheren Teiltöne; **Stretch** verändert ihre Frequenzabstände.

Jeder Teilton verwendet dieselbe Wellenform. Mit Sinus entstehen einzelne
Sinusteiltöne. Dreieck, Sägezahn, Rechteck und eigene Wavetables können dagegen
bereits innerhalb jedes Teiltons weitere Obertöne enthalten.

## Eingänge

Die englischen Namen entsprechen den Bezeichnungen der englischen Oberfläche.
Ohne Verbindung verwendet ein Eingang seinen eingestellten Wert beziehungsweise
seinen Editor. Eine Verbindung ersetzt diesen Wert.

| Eingang | Datentyp und Rate | Standardwert | Bereich und Einheit | Bedeutung |
| --- | --- | --- | --- | --- |
| **Waveform – Wellenform** | Wavetable-Ressource, Control | Sinus | Sinus, Dreieck, Sägezahn, Rechteck oder verbundene Wavetable | Wellenform für sämtliche Teiltöne. Ohne Verbindung gilt die Auswahl in der Combobox. |
| **Partials – Teiltöne** | Wert, Control | 1 | Ganze Zahl, 1–144 | Anzahl der Teiltöne **einschließlich des Grundtons**. Ein verbundener Wert wird am Blockanfang gelesen, auf 1–144 begrenzt und abgerundet. |
| **Falloff – Abfall** | Wert, Audio oder Control | 1 | 0–4, dimensionslos | Exponent für die Abnahme der Teiltongewichte. Höhere Werte betonen den Grundton stärker. |
| **Stretch – Streckung** | Wert, Audio oder Control | 1 | 0–4, dimensionslos | Exponent für die Frequenzverhältnisse. 1 ergibt die gewöhnliche harmonische Reihe. |
| **Frequency – Frequenz** | Wert, Audio oder Control | 440 Hz | Regler: 0,00001–20.000 Hz; Einheiten: Hz, kHz | Grundfrequenz. Verbundene Werte werden für die Berechnung auf 0–20.000 Hz begrenzt. |
| **Phase – Phase** | Wert, Audio oder Control | 0 Umdrehungen | Regler: 0–1 Umdrehung beziehungsweise 0–360° oder 0–2π rad | Laufender Phasenversatz, bezogen auf den Grundton. |
| **Reset – Zurücksetzen** | Trigger, samplegenau | Kein Trigger | Einzelne Ereignisse | Setzt die Phasen aller Teiltöne vor dem betreffenden Sample zurück. Ohne Verbindung lässt sich Reset per Button zum nächsten Blockanfang auslösen. |

**Falloff** und **Stretch** werden sichtbar, sobald mehr als ein Teilton
eingestellt ist **oder** der Partials-Eingang eine Verbindung hat. Eine Verbindung
an Partials hält die beiden Parameter auch dann sichtbar, wenn das eingehende
Signal gerade den Wert 1 liefert.

**Raten:** Audio bedeutet einen Wert pro Sample, Control einen Wert pro
Audioblock. Frequenz, Phase, Falloff und Stretch können jeweils unabhängig
Audio- oder Control-Signale annehmen. Ein Control-Signal wird innerhalb des
Blocks gehalten; die Klangerzeugung selbst läuft weiterhin mit Audiorate.

## Ausgänge

| Ausgang | Datentyp und Rate | Bedeutung und Wertebereich |
| --- | --- | --- |
| **Audio Out – Audioausgang** | Audiosignal, Audio | Normierte Summe der berechneten Teiltöne. Mit den eingebauten Wellenformen liegt das Signal innerhalb von −1 bis +1. Eine eigene Wavetable kann größere Werte enthalten; der Ausgang wird nicht zusätzlich hart begrenzt. |

Der Node erzeugt nicht grundsätzlich zwei unterschiedliche Kanäle. Ohne
kanalabhängige Eingänge kann dieselbe Berechnung für links und rechts verwendet
werden. Unterschiedliche Eingangssignale je Kanal ermöglichen unterschiedliche
Ausgangssignale.

## Einstellungen

Die Klangparameter sind als Eingänge angelegt und werden in der Tabelle oben
beschrieben. Es gibt keine zusätzliche globale Rate-Auswahl und keine
umschaltbare Pegelnormierung für diesen Node.

Bei **Frequency** und **Phase** lässt sich die Einheit auswählen. Die gewählte
Einheit bestimmt auch die Interpretation eines verbundenen Zahlenwerts:
beispielsweise bedeutet 1 bei kHz eine Frequenz von 1.000 Hz. Beim Verbinden wird
die Einheit nicht automatisch an die Quelle angepasst.

Für die Phase gilt: **1 Umdrehung = 360° = 2π rad**.

## Verhalten

### Teiltonfrequenzen

Für Teiltonnummern `n = 1 … N` gilt im eingeschwungenen Zustand:

```text
Teiltonfrequenz(n) = Grundfrequenz × n^Stretch
```

Der erste Teilton bleibt unabhängig von Stretch auf der Grundfrequenz.

| Stretch | Wirkung auf die Frequenzverhältnisse |
| --- | --- |
| 0 | Alle Teiltöne liegen auf der Grundfrequenz. |
| Zwischen 0 und 1 | Die Abstände sind gegenüber der harmonischen Reihe zusammengedrückt. |
| 1 | Harmonische Reihe: 1, 2, 3, 4, … mal Grundfrequenz. |
| 2 | Quadratische Reihe: 1, 4, 9, 16, … mal Grundfrequenz. |
| Größer als 1 | Höhere Teiltöne werden zunehmend weiter nach oben gestreckt. |

Eine Streckung muss nicht automatisch inharmonisch sein: Bei Stretch = 2 bleiben
die Frequenzen ganzzahlige Vielfache der Grundfrequenz. Nichtganzzahlige Exponenten
können dagegen inharmonische Verhältnisse erzeugen.

### Teiltongewichte und Pegel

Das Zielgewicht jedes Teiltons wird so berechnet:

```text
Gewicht(n) = n^(−Falloff)
```

- **Falloff = 0:** Alle Teiltöne haben dasselbe Gewicht.
- **Falloff = 1:** Die Gewichte sind 1, 1/2, 1/3, 1/4, …
- **Falloff = 2:** Die Gewichte sind 1, 1/4, 1/9, 1/16, …

Die gewichteten Signale werden addiert und durch die Summe ihrer tatsächlich
berücksichtigten Gewichte geteilt. Mehr Teiltöne bedeuten daher nicht automatisch
einen proportional höheren Ausgangspegel. Diese Normierung hält weder den
Effektivwert noch die wahrgenommene Lautheit konstant.

Teiltöne mit einer Frequenz **größer oder gleich der halben Sample-Rate** werden
nicht ausgegeben. Sie tragen dann auch nicht zur Gewichtssumme bei. Die eingestellte
Anzahl ist somit eine Obergrenze; tatsächlich hörbar können weniger Teiltöne sein.
Die Wellenformen werden aus vorberechneten, frequenzabhängig bandbegrenzten
Wavetables gelesen.

### Änderungen und Modulation

Änderungen der Teiltongewichte und Frequenzverhältnisse werden intern weich
überführt. Beim Verringern der Teiltonzahl verschwinden die betreffenden Teiltöne
daher nicht schlagartig. Schnelle Änderungen von Falloff oder Stretch können
entsprechend nachlaufen.

Verbundene Frequenz- und Phasensignale werden mit ihrer jeweiligen Eingangsrate
ausgewertet. Für manuelle beziehungsweise MIDI-gesteuerte Regleränderungen können
zusätzlich die Interpolationseinstellungen des Dokuments gelten.

### Phase und Reset

Phase ist ein **laufender Versatz**, nicht ausschließlich eine Startposition.
Für den Teilton `n` wird der Grundton-Phasenversatz mit `n^Stretch` multipliziert.

Reset setzt zunächst alle laufenden Teiltonphasen auf null. Anschließend wird der
aktuelle Phasenversatz bei der Ausgabe berücksichtigt. Reset verändert weder die
Frequenz noch die anderen eingestellten Klangparameter.

Ein Reset oder ein sprunghafter Phasenwechsel kann einen Klick erzeugen. Bei
nichtganzzahligen Teiltonverhältnissen bedeutet auch ein Sprung der Phase um eine
ganze Grundton-Umdrehung nicht zwangsläufig dieselbe Phase für alle Teiltöne.

## Mute und Einfügen

| Aktion | Verhalten |
| --- | --- |
| **Mute** | Die Klangerzeugung wird aus dem wirksamen Signalpfad genommen. Es gibt **keine Durchreicheverdrahtung**: Kein Eingang wird mit Audio Out verbunden. Ausgehende Verbindungen werden als ignoriert behandelt; nachfolgende Eingänge können dadurch wieder ihren eigenen Editorwert verwenden. |
| **Einfügen in eine bestehende Verbindung** | Nicht unterstützt. Der Node ist ein Generator und legt kein Eingangs-/Ausgangspaar zum automatischen Auftrennen und Wiederverbinden einer Leitung fest. |

## Hinweise und weiterführende Dokumentation

- **Frequenz 0 ist kein Mute.** Sie hält den Fortschritt der laufenden Phasen an.
  Abhängig von Phase und Wellenform kann dadurch ein konstanter Wert entstehen.
- Der Rechenaufwand steigt mit der Anzahl berechneter Teiltöne. Schnelle
  Modulation von Falloff oder Stretch verursacht zusätzliche Berechnungen.
- Der Node unterstützt sampleweises Feedback innerhalb einer durch einen
  geeigneten Delay abgesicherten Schleife. Er ersetzt selbst keinen Delay.

Weiterführende Seiten sind derzeit auf Englisch verfügbar:

- [Eigene Wellenformen und Wavetable-Ressourcen](../../WAVEFORMS.md)
- [Phasen-Reset von Oszillatoren](../../OSCILLATOR-RESET.md)
- [Einheiten und Interpretation verbundener Werte](../../PORT-UNITS.md)
- [Sampleweises Feedback](../../SAMPLE-FEEDBACK.md)
