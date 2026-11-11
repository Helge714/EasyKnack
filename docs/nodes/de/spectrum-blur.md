# Spektrum weichzeichnen (Spectrum Blur)
Verteilt Spektralinhalt über benachbarte Frequenzen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Kernel | Gauß / Box / Maximum; Standard: Gauß | Editor |
| Synthese (Synthesis) | Rauschen / Kontinuierlich / Erhalten; Standard: Rauschen | Editor |
| Leistung normieren (Normalize power) | Aus / Ein; Standard: Aus | Editor |
| Breite (Width) | ≥ 0 oct / bins; Standard: 0 oct; oct / cent / st / bins | Wert; ≥ 0 oct / bins; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum-Ressource; Komplexe FFT-Bins; Control |

## Verhalten
Die Einheit wird im Kontextmenü des Sliders oder Ports gewählt. Oktaven, Cent und Halbtöne beschreiben denselben logarithmischen Radius: Eine Breite b in Oktaven umfasst `f·2^(−b)` bis `f·2^b`; 1 oct = 1200 cent = 12 st. Bins wählt einen linearen Binradius. Beim Wechsel zwischen Bins und Tonhöheneinheiten bleibt die gespeicherte Radiuszahl erhalten und ändert ihre Interpretation; eine allgemeine Umrechnung gibt es nicht. DC bleibt bei logarithmischer Breite unverändert. Gaussian verwendet einen Radius von drei Standardabweichungen, Box gewichtet die Nachbarschaft gleichmäßig und Maximum übernimmt die größte Magnitude. Noise erzeugt kontinuierliches Rauschen, Continuous binzentrierte Töne und Preserve erhält die Koeffizientenphase. Breite null ist ein exakter Bypass. Bei sehr kleinen Rauschbreiten ist die Phasenabweichung für einen stetigen Übergang begrenzt. Normalize power erhält die gesamte spektrale Leistung, im Noise-Modus als Erwartungswert statt als Begrenzer pro Frame. Steuerwerte werden pro eingehendem Frame ausgewertet.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Spektrum (Spectrum) → Spektrum (Spectrum) |
| Einfügen | Spektrum (Spectrum) → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectrum-blur.md)
