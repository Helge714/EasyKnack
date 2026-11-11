# Residualmodell
Modelliert die spektrale Hüllkurve eines Residuals und synthetisiert daraus ein stochastisches Spektrum.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Rest (Residual) | — | Spectrum; Control |
| Hüllkurvenbreite (Envelope width) | 0–20000 Hz; Standard: 200 Hz; Hz, kHz | Value; 0–∞ Hz; Control |
| Hüllkurvenglättung (Envelope smoothing) | 0–10000 ms; Standard: 0 ms; ms, s, smpl | Editor; Inspektor |
| Seed | 0–2147483647; Standard: 0 | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum; Control |

## Verhalten
Mittelt die Leistung benachbarter Bins innerhalb der Hüllkurvenbreite (volle Breite in Hz) und erzeugt für jeden Frame neue, reproduzierbare Zufallsphasen. 0 Hz erhält die spektralen Einzelleistungen vor der Fensterung. Hüllkurvenglättung ist eine exponentielle Leistungsglättung mit der angegebenen Zeitkonstante. Das generierte Signal erhält die Fensterung der Quelle; die komplexe Konjugiertsymmetrie und ein entfernter Gleichanteil bleiben erhalten. Die Frame-Leistung wird an die geglättete Hüllkurve angepasst, nicht an wahrgenommene Lautheit. Seed und Frame-Position bestimmen die Zufallsfolge.

Die Modellierung ist absichtlich verlustbehaftet und keine Klassifikation. Für genaue Rekonstruktion das ursprüngliche Residual verwenden. FFT-Größe, Hop und Frame-Zeit bleiben unverändert; deshalb lässt sich das Ergebnis mit dem zugehörigen Teiltonspektrum addieren. Für eine unabhängige, kürzere Residualauflösung zuerst über IFFT in Audio zurückwandeln und mit einer eigenen FFT analysieren. Solche Zweige erst nach ihren IFFTs mischen und zusätzliche Latenzen ausgleichen. Die Fenstergröße wird nicht automatisch geändert.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Rest (Residual) → Spektrum (Spectrum) |
| Einfügen | Rest (Residual) → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Spectrum Math](spectrum-math.md)
