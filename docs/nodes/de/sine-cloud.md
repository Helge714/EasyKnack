# Sinuswolke (Sine Cloud)

## Übersicht

Erzeugt einen Forschungsstimulus aus bis zu zehn Millionen gleich lauten, gleichmäßig verteilten Sinussen mit optional zufälligen Startphasen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | 1–10.000.000 | 1–10.000.000, gerundet | 10.000 | — | [Control](../../user/de/rates.md#control) | Immer |
| Minimale Frequenz (Min Frequency) | [Wert](../../user/de/data-types.md#value) | 0,00001 Hz–halbe DSP-Samplerate | 0,00001 Hz–halbe Samplerate | 20 Hz | [Hz, kHz](../../user/de/units.md#frequency) | [Control](../../user/de/rates.md#control) | Immer |
| Maximale Frequenz (Max Frequency) | [Wert](../../user/de/data-types.md#value) | 0,00001 Hz–halbe DSP-Samplerate | 0,00001 Hz–halbe Samplerate | 20.000 Hz | [Hz, kHz](../../user/de/units.md#frequency) | [Control](../../user/de/rates.md#control) | Immer |
| Seed (Seed) | [Wert](../../user/de/data-types.md#value) | 0–16.777.215 | 0–16.777.215, gerundet | 0 | — | [Control](../../user/de/rates.md#control) | Immer |
| Zufallsphasen (Randomize phases) | [Editor](../../user/de/reference-tables.md) | Aus, Ein | — | Ein | — | — | Immer |
| Maximaler Binabstand (Max Bin Spacing) | [Editor](../../user/de/reference-tables.md) | 0,001–1.000 Hz | — | 0,01 Hz | [Hz, kHz](../../user/de/units.md#frequency) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Tatsächliche Anzahl (Actual count) | [Wert](../../user/de/data-types.md#value) | 0–10.000.000 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Periodenlänge (Period) | [Wert](../../user/de/data-types.md#value) | 0 oder N / (Samplerate × Bin-ggT) | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Maximaler Binabstand bestimmt das Frequenzraster unabhängig von Count und Frequenzbereich. Gewählt wird die kleinste unterstützte Tabelle mit N ≥ Samplerate / Binabstand, mindestens 65.536 und höchstens 67.108.864 Samples. Unterstützte Größen enthalten nur die Faktoren 2, 3 und 5 und müssen keine Zweierpotenzen sein. Der Binabstand dieses Auswahlrasters ist Samplerate / N und überschreitet den gewünschten Höchstwert nicht. Bei 48 kHz ergeben 0,01 Hz genau 4.800.000 Samples (100 Sekunden, 19,2 MB Mono-Float-Audio); 0,001 Hz ergeben 48.000.000 Samples. Ein engerer Frequenzbereich vergrößert das Auswahlraster nicht. Nach der Auswahl der belegten Bins werden N und alle Bin-Indizes vor der Synthese durch ihren gemeinsamen Teiler geteilt. Dadurch entfallen exakte Samplewiederholungen, ohne Frequenzen, Startphasen oder Wellenform zu ändern. Die gespeicherte Tabelle kann deshalb wesentlich kürzer als die Auswahlrasterdauer sein. Bei Count = 2 in 0–24.000 Hz und 48 kHz entstehen 8.000 und 16.000 Hz; dafür genügen sechs Samples (0,125 ms). Manche Kombinationen sind nicht kürzbar und benötigen weiterhin die vollständige Tabelle.

Count fordert bis zu zehn Millionen gleich laute Sinusse an. Actual count wird auf die verfügbaren verschiedenen positiven Bins im gewählten Intervall begrenzt. Die Zielfrequenzen liegen innerhalb des Bereichs bei Minimum + (i + 1) × (Maximum − Minimum) / (Actual count + 1), mit i ab null. Sie werden auf das Raster gerundet; bei knapper Kapazität bleiben verschiedene Bins für die übrigen Töne reserviert. Bei voller Kapazität werden alle verfügbaren Bins einschließlich der Randbins verwendet; benachbarte Abstände können um einen Bin abweichen. Bei 48 kHz und 0,01 Hz Binabstand passen 1.998.001 Töne in 20–20.000 Hz. In 100–100,025 Hz passen drei Bins. Ein einzelner Ton verwendet den Bin am nächsten zur Intervallmitte. Zwei Töne in 0–1.320 Hz ergeben 440 und 880 Hz und eignen sich zum hörbaren Test. Vertauschte Grenzen werden sortiert. Enthält ein Intervall keinen Bin, wird der nächste innere Bin zur Mitte verwendet. Steuerwerte verwenden die Float-Genauigkeit der Audio-Engine; Rundung direkt auf einer Bin-Grenze kann beeinflussen, ob der Randbin enthalten ist. DC und der exakte Nyquist-Bin sind ausgeschlossen. Frequenzregler reichen bis zur halben DSP-Samplerate der Anwendung; die native Erzeugung verwendet ihre tatsächliche Samplerate.

Zufallsphasen werden einmalig aus Seed gezogen. Ausgeschaltet starten alle Sinusse bei Phase null und Seed-Änderungen erzeugen keine neue Tabelle. Die gespeicherten Samples entsprechen bis auf numerische Fehler der additiven Summe der gewählten Binfrequenzen. Danach wird die gesamte Tabelle auf Spitzenbetrag 1 normiert. Dies ist keine RMS-Normierung. Bei unveränderter Wiedergabe gibt es keine neuen Ziehungen, Grenzüberblendungen oder Unterdrückung von Schwebungen. Gleiche Parameter und Samplerate ergeben denselben Stimulus.

Periodenlänge meldet die kürzeste gemeinsame Wellenformperiode: N / (Samplerate × größter gemeinsamer Teiler der belegten Bin-Indizes). Ein einzelner Ton meldet 1 / tatsächliche Frequenz. Die Periode kann von der vollständigen Tabellendauer abweichen und eine nicht ganzzahlige Sampleanzahl umfassen. Die gespeicherte Samplefolge wird nach N Samples geloopt. Period und Actual count sind anfangs 0, behalten während der Berechnung die Werte der aktiven Tabelle und wechseln beim Beginn der 20-ms-Überblendung.

Änderungen erzeugen eine neue Tabelle im Hintergrund; schnelle Änderungen werden zusammengefasst. Bis sie fertig ist, läuft die bisherige Tabelle weiter. Anfangs herrscht Stille. Die Berechnungsdauer hängt von CPU und Tabellengröße ab, nicht von der Abspieldauer. Fehlgeschlagene Berechnungen oder ein Binabstand, der mehr als 67.108.864 Samples erfordert, behalten die alte Tabelle bei (anfangs Stille). Der Worker hält höchstens drei Tabellen; zur Berechnung benötigt er zusätzlich zwei komplexe Double-Arrays, etwa 153,6 MB bei 4,8 Millionen Samples, sowie die Sampletabellen. Die Wiedergabe führt keine Allokation oder Transformation aus.

Kompatibilität: Die Gaußverteilung wurde entfernt. Alte Gauß-Einstellungen werden ignoriert; Dokumente ohne maximalen Binabstand verwenden 0,01 Hz. Die Tonpositionen verwenden außerdem innere Unterteilungen statt der alten Verteilung einschließlich der Randpunkte. Dadurch können sich Frequenzen, tatsächliche Anzahl, Schleifenlänge und Klang bestehender Dateien ändern; die Dateien bleiben lesbar.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Index](index.md) · [English](../en/sine-cloud.md)
