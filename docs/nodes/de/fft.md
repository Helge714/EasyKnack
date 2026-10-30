# FFT

## Übersicht

Transformiert gefenstertes Audio in komplexe Spektralframes.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Synchron (Synchronous) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Ein | — | — | Immer |
| Minimaler Frameabstand (Minimum frame interval) | [Editor](../../user/de/reference-tables.md) | 0–60000 ms | — | 0 ms | [ms](../../user/de/units.md) | — | Inspektor; asynchron |
| Fenstergröße (Window size) | [Editor](../../user/de/reference-tables.md) | 256 / 512 / 1024 / 2048 / 4096 / 8192 / 16384 / 32768 / 65536 smp | — | 2048 smp | [smp](../../user/de/units.md) | — | Immer |
| Fenster (Window) | [Editor](../../user/de/reference-tables.md) | Hann / Hamming / Blackman / Rectangular | — | Hann | — | — | Immer |
| Zero-Padding (Zero padding) | [Editor](../../user/de/reference-tables.md) | Keines / Lineare Korrelation (2x) | — | Keines | — | — | Immer |
| Gleichanteil entfernen (Remove DC) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Überlappung (Overlap) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0.75 | [Faktor](../../user/de/units.md), [%](../../user/de/units.md) | — | Immer |
| Kanal (Channel) | [Editor](../../user/de/reference-tables.md) | Alle / Links / Rechts / Mix | — | Alle | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | Komplexe FFT-Bins | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Jeder Frame enthält komplexe Koeffizienten `X[k] = Σ x[n]·w[n]·exp(−i·2πkn/N)` mit ursprünglicher Sampleposition. Die Binfrequenz ist `k·sampleRate/N`; Zero-Padding vergrößert N ohne zusätzliche Messinformation. Gleichanteil entfernen zieht den Fenstermittelwert vor der Fensterung ab. Hop = gerundet `windowSize·(1 − overlap)`, mindestens 64 Samples synchron bzw. 1 asynchron. Alle erhält Stereo und Stimmen; Links/Rechts/Mix summieren Stimmen, mit Mix = `(L+R)/2`. Synchrones Rechnen ist für [IFFT](ifft.md) vorgesehen. Asynchrone Analyse kann Frames überspringen oder verspätet liefern; Minimaler Frameabstand begrenzt zusätzlich die Anfragerate. Für lineare [Autokorrelation](autocorrelation.md) wähle Rectangular und 2x Padding.

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

[Node-Verzeichnis](index.md) · [English](../en/fft.md)
