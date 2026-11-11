# Hilbert
Erzeugt ein verzögertes Direktsignal und dessen Hilbert-Quadraturkomponente.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audioeingang (Audio In) | — | Audio; ±∞; Audio |
| Filterlänge (Filter length) | 61, 129, 257, 513, 1025, 2049; Standard: 257 | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Direkt (Direct) | Audio; ±∞ |
| Quadratur (Quadrature) | Audio; ±∞ |

## Verhalten
Ein Hamming-gefensterter FIR-Filter mit wählbarer Länge N nähert für positive Frequenzen eine Phasenverschiebung um −90° an. Beide Ausgänge haben eine Gruppenlaufzeit von `(N - 1) / 2` Samples (2,67 ms für N = 257 bei 48 kHz). Der Verlaufsspeicher ist beim Start mit Nullen gefüllt; Kanäle und Stimmen besitzen unabhängige Zustände.

Nahe Gleichanteil und Nyquist-Frequenz nimmt die Genauigkeit ab; Quadratur unterdrückt diese beiden Komponenten. Für N = 257 bei 48 kHz sind für genaue Quadratur etwa 400 Hz Übergangsbereich an beiden Enden freizuhalten; diese Grenzen skalieren mit der Samplerate. Längere Filter verkleinern die Übergangsbereiche, erhöhen aber Rechenaufwand und Latenz. Ein Längenwechsel bereitet außerhalb des Audiothreads einen neuen leeren Filter vor. Die Länge hat keinen Signalanschluss. Ausgänge werden nicht in der Amplitude begrenzt.

Eine Frequenzverschiebung aufwärts ergibt sich durch `Direct * cos(phase) - Quadrature * sin(phase)` mit gemeinsamer Trägerphase. Addition wählt die Verschiebung abwärts. Mute leitet den Eingang unverzögert an Direkt weiter und setzt Quadratur auf 0.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Audioeingang (Audio In) → Direkt (Direct) |
| Einfügen | Audioeingang (Audio In) → Direkt (Direct) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Nodeverzeichnis](index.md) · [English](../en/hilbert.md)
