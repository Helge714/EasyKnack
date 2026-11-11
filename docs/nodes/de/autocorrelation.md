# Autokorrelation (Autocorrelation)
Berechnet die Autokorrelation aus einem vorhandenen Spektrum.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Normierung (Normalization) | Biased / Unbiased; Standard: Biased | Editor |
| Glättungsfaktor (Smoothing factor) | 0–1; Standard: 1 | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Korrelation (Correlation) | Analysis-Ressource; ±1; Control |

## Verhalten
`r = IFFT(|X|²)`, auf Lag 0 normiert; Stille ergibt null. FFT-Padding 2x liefert lineare, ohne Padding zirkuläre Korrelation. Unbiased kompensiert die abnehmende Sampleüberlappung bei größeren linearen Lags und begrenzt auf ±1; die Fensterabschwächung wird nicht rückgängig gemacht. Für ungefensterte, gleichanteilbereinigte Korrelation wähle Rectangular, 2x Padding und Remove DC im [FFT](fft.md). Je Frame gilt `held = old·(1 − factor) + new·factor`, initialisiert mit dem ersten Frame. Faktor 0 hält diesen Frame, 1 aktualisiert sofort. [Correlation Features](correlation-features.md) reduziert das Ergebnis auf skalare Kennwerte.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/autocorrelation.md)
