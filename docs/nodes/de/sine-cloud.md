# Sinuswolke (Sine Cloud)

## Übersicht

Erzeugt einen Forschungsstimulus aus bis zu einer Million gleich lauten, linear verteilten Sinussen mit zufälligen Startphasen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | 1–1.000.000 | 1–1.000.000, gerundet | 10.000 | — | [Control](../../user/de/rates.md#control) | Immer |
| Minimale Frequenz (Min Frequency) | [Wert](../../user/de/data-types.md#value) | 0,00001–20.000 Hz | 0,00001 Hz–halbe Samplerate | 20 Hz | [Hz, kHz](../../user/de/units.md#frequency) | [Control](../../user/de/rates.md#control) | Immer |
| Maximale Frequenz (Max Frequency) | [Wert](../../user/de/data-types.md#value) | 0,00001–20.000 Hz | 0,00001 Hz–halbe Samplerate | 20.000 Hz | [Hz, kHz](../../user/de/units.md#frequency) | [Control](../../user/de/rates.md#control) | Immer |
| Seed (Seed) | [Wert](../../user/de/data-types.md#value) | 0–16.777.215 | 0–16.777.215, gerundet | 0 | — | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Tatsächliche Anzahl (Actual count) | [Wert](../../user/de/data-types.md#value) | 0–1.000.000 | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die einmal ausgelosten Phasen bleiben in der periodischen Tabelle erhalten. Gleiche Parameter und Samplerate ergeben dieselbe Tabelle. Alle enthaltenen Sinusse haben gleiche Amplitude; die gesamte Tabelle wird auf einen Spitzenbetrag von 1 normiert. Dies ist keine RMS-Normalisierung: Der mittlere Pegel hängt vom Seed und der Tonanzahl ab. Kanal-Kopien mit gleichen Parametern spielen denselben Stimulus.

Die Frequenzen werden möglichst gleichmäßig auf verschiedene positive IFFT-Bins verteilt. Die Tabelle enthält 65.536–4.194.304 Samples; die Größe wächst mit dem benötigten Frequenzraster. Der Abstand beträgt Samplerate / Tabellengröße. Gewünschte Frequenzen werden daher angenähert; benachbarte Abstände können um einen Bin variieren. DC und Nyquist werden nicht belegt. Vertauschte Frequenzgrenzen werden sortiert. Gleiche Grenzen ergeben einen einzelnen Ton am nächsten darstellbaren Bin. Falls in einem sehr engen Bereich kein Bin liegt, wird ebenfalls der nächste Bin zur Bereichsmitte verwendet.

Bei zu wenigen Rasterplätzen sinkt die tatsächliche Anzahl. Actual count meldet die Anzahl der neu aktiven Tabelle, anfangs 0. Bei 48 kHz ermöglicht 20–20.000 Hz eine Million unterschiedliche Sinusse. Eine enge Bandbreite kann diese Anzahl unterschreiten. Die längste Tabelle wiederholt sich bei 48 kHz nach etwa 87,4 Sekunden.

Eingänge werden am Anfang jedes Verarbeitungsaufrufs ausgewertet. Änderungen erzeugen im Hintergrund eine neue Tabelle; schnelle Änderungen werden zusammengefasst. Bis zur Fertigstellung läuft die alte Tabelle weiter, beim ersten Start herrscht Stille. Der Wechsel startet die neue Tabelle an ihrem Anfang und blendet 20 ms lang linear über. Währenddessen gelten gleiche Einzelamplituden nur innerhalb der jeweiligen Tabelle. Falls eine Erzeugung scheitert, bleibt die bisherige Tabelle aktiv. Die Erstellung kostet Rechenzeit und temporären Speicher: Die größte Tabelle benötigt 16 MiB, der komplexe IFFT-Arbeitsspeicher zusätzlich 64 MiB. Pro Node werden höchstens drei Tabellen gehalten.

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
