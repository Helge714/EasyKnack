# Sinuswolke (Sine Cloud)
Erzeugt einen Forschungsstimulus aus bis zu zehn Millionen gleich lauten, gleichmäßig oder gaußförmig verteilten Sinussen mit optional zufälligen Startphasen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Anzahl (Count) | 1–10.000.000; Standard: 10.000 | Wert; 1–10.000.000, gerundet; Control |
| Minimale Frequenz (Min Frequency) | 0,00001 Hz–halbe DSP-Samplerate; Standard: 20 Hz; Hz, kHz | Wert; 0,00001 Hz–halbe Samplerate; Control |
| Maximale Frequenz (Max Frequency) | 0,00001 Hz–halbe DSP-Samplerate; Standard: 20.000 Hz; Hz, kHz | Wert; 0,00001 Hz–halbe Samplerate; Control |
| Seed (Seed) | 0–16.777.215; Standard: 0 | Wert; 0–16.777.215, gerundet; Control |
| Gaußverteilung (Gaussian) | Aus, Ein; Standard: Aus | Editor |
| Zufallsphasen (Randomize phases) | Aus, Ein; Standard: Ein | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±1 |
| Tatsächliche Anzahl (Actual count) | Wert; 0–10.000.000; Control |
| Periodenlänge (Period) | Wert; 0 oder N / Samplerate; Control |

## Verhalten

Gleiche Parameter und Samplerate erzeugen denselben periodischen Buffer. Jede enthaltene Sinusschwingung hat gleiche Amplitude; die Gesamttabelle ist auf Peak 1 normiert, nicht auf RMS. Zufallsphasen werden einmal aus Seed gezogen. Ohne diese Option starten alle Sinusse bei Phase 0 und Seed ist wirkungslos. Bei unveränderten Parametern läuft die Schleife exakt, ohne neue Zufallswerte oder Kaschierung von Periodik und Schwebungen.

Gaussian verändert die Frequenzdichte, nicht die Einzelamplituden: Normalverteilung in linearen Hz um die Intervallmitte, Standardabweichung `(Max-Min)/6`, begrenzt auf ±3σ. Seed verändert nur Phasen. Frequenzen liegen auf verschiedenen inneren IFFT-Bins; DC und die exakte Nyquist-Frequenz sind ausgeschlossen. Bei zu wenig Rasterplätzen sinkt Actual count. Vertauschte Grenzen werden sortiert; gleiche Grenzen ergeben einen Ton am nächsten darstellbaren Bin.

Period meldet die vollständige aktive Bufferlänge in Sekunden, vor der ersten Tabelle 0. Bei Neuberechnung bleibt die alte Tabelle aktiv; mit Beginn des 20-ms-Übergangs gilt die neue Periode. Sie muss nicht die kürzeste mathematische Klangperiode sein. Schnelle Parameteränderungen werden zusammengefasst; Fehler erhalten die alte Tabelle.

Große Tabellen brauchen erhebliche Aufbauzeit und Speicher: bis zu 67.108.864 Samples (256 MiB) plus 1 GiB IFFT-Arbeitsspeicher; höchstens drei Tabellen pro Node. Enge Frequenzintervalle können trotz großer Tabelle die gewünschte Anzahl nicht erreichen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Index](index.md) · [English](../en/sine-cloud.md)
