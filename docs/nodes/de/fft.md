# FFT
Transformiert gefenstertes Audio in komplexe Spektralframes.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audio-Eingang (Audio In) | — | Audio; ±∞; Auto |
| Synchron (Synchronous) | Aus / Ein; Standard: Ein | Editor |
| Minimaler Frameabstand (Minimum frame interval) | 0–60000 ms; Standard: 0 ms; ms | Editor; Inspektor; asynchron |
| Fenstergröße (Window size) | 64 / 128 / 256 / 512 / 1024 / 2048 / 4096 / 8192 / 16384 / 32768 / 65536 smp; Standard: 2048 smp; smp | Editor |
| Fenster (Window) | Hann / Hamming / Blackman / Rectangular; Standard: Hann | Editor |
| Zero-Padding (Zero padding) | Keines / Lineare Korrelation (2x); Standard: Keines | Editor |
| Gleichanteil entfernen (Remove DC) | Aus / Ein; Standard: Aus | Editor |
| Überlappung (Overlap) | 0–1; Standard: 0.75; Faktor, % | Editor |
| Kanal (Channel) | Alle / Links / Rechts / Mix; Standard: Alle | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum-Ressource; Komplexe FFT-Bins; Control |

## Verhalten

[Anleitung](../../user/de/spectral-synthesis.md)
Jeder Frame enthält komplexe Koeffizienten `X[k] = Σ x[n]·w[n]·exp(−i·2πkn/N)` mit ursprünglicher Sampleposition. Die Binfrequenz ist `k·sampleRate/N`; Zero-Padding vergrößert N ohne zusätzliche Messinformation. Gleichanteil entfernen zieht den Fenstermittelwert vor der Fensterung ab. Hop = gerundet `windowSize·(1 − overlap)`, synchron mindestens 16 Samples bei Fenstergröße 64, 32 bei 128 und 64 bei größeren Fenstern; asynchron mindestens 1 Sample. Alle erhält Stereo und Stimmen; Links/Rechts/Mix summieren Stimmen, mit Mix = `(L+R)/2`. Synchrones Rechnen ist für [IFFT](ifft.md) vorgesehen. Asynchrone Analyse kann Frames überspringen oder verspätet liefern; Minimaler Frameabstand begrenzt zusätzlich die Anfragerate. Für lineare [Autokorrelation](autocorrelation.md) wähle Rectangular und 2x Padding.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/fft.md)
