# Binkontext (Bin Context)
Stellt den aktuellen Bin einer Spectral Group bereit.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Realteil (Real) | Wert; ±∞; Audio (Bin/Frame) |
| Imaginärteil (Imaginary) | Wert; ±∞; Audio (Bin/Frame) |
| Betrag (Magnitude) | Wert; 0–∞; Audio (Bin/Frame) |
| Phase | Wert; ±π rad; Audio (Bin/Frame) |
| Frequenz (Frequency) | Wert; 0–fs/2 Hz; Audio (Bin/Frame) |
| Binindex (Bin index) | Wert; 0–N/2; Audio (Bin/Frame) |
| Position | Wert; 0–1; Audio (Bin/Frame) |
| Framezeit (Frame time) | Wert; ±∞ s; Audio (Bin/Frame) |

## Verhalten
Nur innerhalb einer [Spectral Group](spectral-group.md) gültig. Bei FFT-Größe N hat Bin k die Frequenz `k × fs/N` und Position `k/(N/2)`. Real/Imaginary und Magnitude sind rohe komplexe FFT-Koeffizienten, keine normierten Amplituden. Framezeit bezeichnet den Fensterbeginn und kann anfangs durch Nullauffüllung negativ sein. Die angezeigte Audiorate bezeichnet hier einen je Frame ausgewerteten Binvektor, kein zeitliches Audio.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-context.md)
