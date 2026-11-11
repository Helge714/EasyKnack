# Teiltonanalyse
Zerlegt ein Spektrum in verfolgte Teiltöne und ein komplexes Restspektrum.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum; Control |
| Auflösung (Resolution) | 0–20000 Hz; Standard: 0 Hz; Hz, kHz | Editor |
| Schwelle (Threshold) | −160–0 dB; Standard: −60 dB; dB | Editor |
| Minimale Teiltondauer (Minimum track duration) | 0–2000 ms; Standard: 20 ms; ms, s, smpl | Editor |
| Maximale Teiltöne (Maximum partials) | 1–32768; Standard: 64 | Editor |
| Minimale Frequenz (Minimum frequency) | 0–∞ Hz; Standard: 20 Hz; Hz, kHz | Editor; Inspektor |
| Maximale Frequenz (Maximum frequency) | 0–∞ Hz; Standard: 20000 Hz; Hz, kHz | Editor; Inspektor |
| Frequenzabweichung (Frequency drift) | 0–20000 Hz; Standard: 0 Hz; Hz, kHz | Editor; Inspektor |
| Bandbreitenmodell (Bandwidth model) | None / Residue / Convergence; Standard: Convergence | Editor; Inspektor |
| Konvergenztoleranz (Convergence tolerance) | 0.001–1; Standard: 0.1 | Editor; Inspektor; Convergence |
| Bandbreitenregion (Bandwidth region) | 1–20000 Hz; Standard: 2000 Hz; Hz, kHz | Editor; Inspektor; Residue |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Teiltöne (Partials) | Partials; Control |
| Rest (Residual) | Spectrum; Control |

## Verhalten
Verwendet das Kaiser-Fenster, die spektrale Neuzuordnung, Peakauswahl, auflösungsabhängige Ausdünnung und Teiltonverfolgung von Loris. Die Eingangs-FFT bestimmt die verfügbare Fensterdauer und Framefolge. Auflösung ist der minimale Peakabstand in Hz; 0 wählt `2 × Samplerate / Fenstersamples`. Frequenzabweichung begrenzt die Änderung zwischen aufeinanderfolgenden Trackpunkten; 0 wählt die halbe Auflösung. Die Komponenten benötigen keine gemeinsame Grundfrequenz.

Das Bandbreitenmodell bestimmt die Schätzung des rauschhaften Teiltonanteils:

- **None:** Bandbreite null; die gesamte erkannte Amplitude gilt als tonal.
- **Residue:** ordnet verworfene spektrale Energie innerhalb der Bandbreitenregion zu.
- **Convergence:** verwendet den gemischten Phasenableitungsindikator von Loris; die Konvergenztoleranz bestimmt dessen Sättigungspunkt.

Die ausgegebene Sinusamplitude ist `LorisAmplitude × sqrt(1 − Bandbreite)`. Der [Teilton-Kontext](partial-context.md) liefert `1 − Bandbreite` als Tonalität. Nicht durch diese Sinusschwingungen abgebildete Anteile bleiben im exakten Rest. Das ist ein Sinus-/Rauschmodell, keine Erkennung von Instrumenten, Schlagwerk oder Klangbedeutung.

Die minimale Teiltondauer wirkt kausal: Ein Track wird erst nach dieser Zeit ausgegeben; frühere Frames bleiben unverändert. Maximale Teiltöne begrenzt gleichzeitige Tracks; stärkere akzeptierte Peaks haben Vorrang. Ohne zuordenbaren Peak im nächsten Frame endet ein Track. Änderungen der Auflösung, Dauer oder weiterer Analyseparameter starten das Tracking neu. IDs bleiben innerhalb des laufenden Analyzers eindeutig.

Die Analyse muss jeden Frame innerhalb der Echtzeitfrist fertigstellen; lange FFT-Fenster und große Kapazitäten können die Wiedergabe überlasten.

Der unveränderte Ausgang von [Teiltöne zu Spektrum](partials-to-spectrum.md) plus Rest rekonstruiert das ursprüngliche komplexe Spektrum einschließlich der Schätzfehler. Das [Restmodell](residual-model.md) ist ein separates verlustbehaftetes Rauschmodell. Kurze Fenster können nahe Komponenten nicht auflösen; keine Einstellung garantiert eine perfekte Trennung zwischen Klang und Rauschen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[English](../en/partial-analysis.md)
