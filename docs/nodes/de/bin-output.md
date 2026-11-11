# Binausgang (Bin Output)
Schreibt einen komplexen Spectrum-Ausgang aus Binwerten.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Komponenten (Components) | Real / Imaginär, Betrag / Phase; Standard: Real / Imaginär | Editor |
| Realteil / Betrag (Real / Magnitude) | ±∞; Standard: 0 | Wert; ±∞; polarer Betrag ≥ 0; Auto |
| Imaginärteil / Phase (Imaginary / Phase) | ±∞; Standard: 0 | Wert; ±∞; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum-Ressource; Control |

## Verhalten
Kartesisch wird `Real + i × Imaginary` geschrieben; polar `max(0,Magnitude) × exp(i×Phase)` mit Phase in Radiant. Jeder Bin Output erzeugt ein eigenes Spectrum bei gemeinsamem Bin-Ausführungsplan. Die Gruppe stellt die konjugierte Symmetrie für reelle Signale her. Es dürfen nur Frame-/Binberechnungen hineinführen, kein Oszillatoraudio. Siehe [Spectral Group](spectral-group.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-output.md)
