# Spektrale Merkmale (Spectral Features)
Misst die Frequenzverteilung und ihre Änderung zwischen Frames.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Analysis-Ressource; Spektrum- oder Analyseressource; Control |
| Frequenzskala (Frequency scale) | Linear / Logarithmisch; Standard: Linear | Editor; Inspektor |
| Gewichtung (Weighting) | Power / Magnitude; Standard: Power | Editor |
| Roll-off-Anteil (Roll-off fraction) | 0–1; Standard: 0.85; Faktor, % | Editor |
| Flux normieren (Normalize flux) | Aus / Ein; Standard: Ein | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Zentrum (Centroid) | Wert; 0–Nyquist Hz; Control |
| Streuung (Spread) | Wert; ≥ 0 Hz / oct; Control |
| Schiefe (Skewness) | Wert; ±∞; Control |
| Kurtosis (Kurtosis) | Wert; ≥ 0; Control |
| Flux | Wert; ≥ 0; Control |
| Roll-off (Rolloff) | Wert; 0–Nyquist Hz; Control |
| Neigung (Slope) | Wert; ±∞ / Hz; Control |
| Flachheit (Flatness) | Wert; 0–1; Control |

## Verhalten

[Anleitung](../../user/de/analysis.md)
Sei A[k] = abs(X[k])/Σw[n] und m[k] = 1 bei DC/Nyquist, sonst 2. Die Gewichtung ist W[k] = m[k]·A[k]² für Power oder m[k]·A[k] für Magnitude. Setze p[k] = W[k]/ΣW. Lineare Momente verwenden z[k] = f[k]; logarithmische Momente verwenden z[k] = log2(f[k]), ohne DC und mit neu normierten verbleibenden Gewichten.

- Zentrum: μ = Σp·z; logarithmischer Ausgang ist 2^μ Hz.
- Streuung: σ = sqrt(Σp·(z−μ)²), in Hz oder Oktaven.
- Schiefe: Σp·(z−μ)³/σ³.
- Kurtosis: Σp·(z−μ)⁴/σ⁴; Gauß = 3.
- Flux: sqrt(Σ(current−previous)²), normiert mit p, sonst mit W. Erster Frame = 0.
- Roll-off: erste Frequenz, deren kumuliertes Gewicht den gewählten Anteil erreicht.
- Neigung: Steigung der linearen Ausgleichsgeraden von p über f, je Hz.
- Flachheit: geometrisches Mittel der nach unten begrenzten W geteilt durch ihr arithmetisches Mittel; Untergrenze = max(1e−300, mean(W)·1e−12).

Die logarithmische Skala verändert nur die vier Momente. Stille und undefinierte Momente bei Streuung null ergeben null. Ausgänge halten zwischen Frames. Die mittlere Flachheit einer einzelnen Rausch-FFT liegt unter 1; vorherige Leistungsmittelung verringert die Schätzstreuung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectral-features.md)
