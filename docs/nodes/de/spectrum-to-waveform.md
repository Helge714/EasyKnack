# Spektrum zu Wellenform (Spectrum to Waveform)
Erfasst eine periodische Wavetable aus einem Spektrum.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Grundfrequenz (Fundamental) | 1–20000 Hz; Standard: 440 Hz; Hz, kHz | Wert; 1–20000 Hz; Auto |
| Erfassen (Capture) | Schaltfläche | Trigger; Trigger; Trigger |
| Normieren (Normalize) | Aus / Ein; Standard: Ein | Editor |
| Erfasste Ressource kopieren (Copy captured resource) | Schaltfläche | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wellenform (Waveform) | Wavetable-Ressource; 2048 Samples; Control |

## Verhalten
Erfassen fordert eine Hintergrundrekonstruktion aus einem gültigen FFT-Frame an; Grundfrequenz dient als harmonische Referenz. Der Ausgang hält die Ressource bis zur nächsten Erfassung, ohne sie je Block neu zu berechnen. Normieren skaliert die erfasste Wellenform. Erfasste Ressource kopieren legt Sampledaten in die Anwendungszwischenablage; Einfügen erzeugt eine [Waveform](waveform.md) im Ressourcenmodus. Erfasste Samples werden im Dokument gespeichert. [Wavetable Viewer](wavetable-viewer.md) zeigt sie an.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectrum-to-waveform.md)
