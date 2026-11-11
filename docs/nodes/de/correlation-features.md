# Korrelationsmerkmale (Correlation Features)
Reduziert Autokorrelation auf Periodizitäts-, Perioden- und Rauschkennwerte.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Autokorrelation (Autocorrelation) | — | Analysis-Ressource; Korrelationsressource; Control |
| Minimale Frequenz (Minimum frequency) | ≥ 0.001 Hz; Standard: 20 Hz; Hz, kHz | Editor; Inspektor |
| Maximale Frequenz (Maximum frequency) | ≥ 0.001 Hz; Standard: 20000 Hz; Hz, kHz | Editor; Inspektor |
| Periodizitätsschwelle (Periodicity threshold) | 0–1; Standard: 0.3; Faktor, % | Editor; Inspektor |
| Stille-Schwelle (Silence threshold) | ≥ 0; Standard: 0.000001; Faktor, % | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Periodizität (Periodicity) | Wert; 0–1; Control |
| Periode (Period) | Wert; ≥ 0 s; Control |
| Frequenz (Frequency) | Wert; ≥ 0 Hz; Control |
| HNR | Wert; ±120 dB; Control |
| Gültig (Valid) | Wert; 0–1; Control |

## Verhalten
Sucht positive lokale Korrelationsmaxima innerhalb der Frequenzgrenzen mit Subsample-Peakinterpolation. Mindestens drei Perioden müssen in das Analysefenster passen. Periodizität p ist das gewählte normierte Korrelationsmaximum, keine Wahrscheinlichkeit. Periode ist dessen Lag in Sekunden; Frequenz = 1/Periode. HNR = `10·log10(p/(1−p))`, begrenzt auf ±120 dB: 0 dB bedeutet gleich große geschätzte periodische und verbleibende Leistung, positive Werte bevorzugen periodische Leistung. Der Wert hängt von vorgeschaltetem Fenster und Normierung ab. Gültig = 1 nur bei erfüllter Peak- und Quell-RMS-Schwelle; sonst sind Periode/Frequenz null. Werte halten zwischen Frames. Verwende [Autokorrelation](autocorrelation.md) oder deren Mittelwert statt eines rohen Spektrums.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/correlation-features.md)
