# Tonhöhenerkennung (Pitch Detection)
Schätzt eine Grundfrequenz aus Autokorrelation.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Autokorrelation (Autocorrelation) | — | Analysis-Ressource; Korrelationsressource; Control |
| Minimum | 1–20000 Hz; Standard: 40 Hz; Hz, kHz | Editor |
| Maximum | 1–20000 Hz; Standard: 2000 Hz; Hz, kHz | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Frequenz [Hz] (Frequency [Hz]) | Wert; 0–20000 Hz; Control |
| Konfidenz (Confidence) | Wert; 0–1; Control |

## Verhalten
Überspringt die anfängliche Lag-0-Kuppe und wählt das früheste lokale Maximum mit mindestens 90% des stärksten Maximums im Suchbereich, sofern dieses 0.1 erreicht. Parabolische Interpolation verfeinert den Lag. Frequenz = 1/Lag; Konfidenz ist die normierte Korrelationshöhe. Ohne geeigneten Peak wird null ausgegeben. Verwende Rectangular-FFT, 2x Padding und mehrere Perioden der tiefsten erwarteten Frequenz. Minimum muss unter Maximum liegen. [Correlation Features](correlation-features.md) liefert zusätzlich Gültigkeit und HNR.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/pitch-detection.md)
