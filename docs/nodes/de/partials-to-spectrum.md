# Teiltöne zu Spektrum
Erzeugt aus Teiltönen ein komplexes Spektrum für IFFT.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Teiltöne (Partials) | — | Partials; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum; Control |

## Verhalten
Rekonstruiert positive und negative Frequenzanteile mit der Fensterfunktion der Analyse. Frequenzänderungen werden zwischen Frames in eine kontinuierliche Phasenkorrektur integriert. Frequenzen außerhalb 0–Nyquist und nichtpositive Amplituden werden verworfen. Unveränderte Teiltöne und der zugehörige Rest ergeben wieder das ursprüngliche Spektrum. Die Keulenberechnung ist auf acht Fenster-Bins je Seite begrenzt; verbleibende Originalausläufer liegen im Rest.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[English](../en/partials-to-spectrum.md)
