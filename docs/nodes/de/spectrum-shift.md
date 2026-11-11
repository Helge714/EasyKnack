# Spektrumverschiebung (Spectrum Shift)
Verschiebt ein Spektrum um einen Binversatz.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Verschiebung (Bins) (Shift (bins)) | ±8192; Standard: 0; bins | Wert; ±∞; Auto |
| Kontinuierliche Phase (Continuous phase) | Aus / Ein; Standard: Ein | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum-Ressource; Komplexe FFT-Bins; Control |

## Verhalten
Positive Verschiebung verschiebt den Inhalt um `shift·sampleRate/FFTSize` Hz nach oben. Gebrochene Verschiebungen interpolieren komplexe Koeffizienten; Inhalt außerhalb des gültigen Bereichs entfällt. Kontinuierliche Phase kompensiert die Frame-Zeitphase bei konstantem Versatz. Dies ist Frequenztranslation statt proportionaler musikalischer Transposition; schnelle Modulation kann Artefakte erzeugen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Spektrum (Spectrum) → Spektrum (Spectrum) |
| Einfügen | Spektrum (Spectrum) → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectrum-shift.md)
