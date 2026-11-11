# Inverse FFT (IFFT)
Rekonstruiert Audio aus komplexen Spectrum-Frames.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Control |
| Stabilität (Stability) | 0–1; Standard: 0 | Wert |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audio | Audio; ±∞ |

## Verhalten

[Anleitung](../../user/de/spectral-synthesis.md)
Verwendet inverse FFT und normiertes Overlap-Add mit Zeit- und Fenstermetadaten der Quelle. Ein synchroner Pfad besitzt eine Fensterlänge Rekonstruktionslatenz. Asynchrone FFT wird angenommen, verspätete/fehlende Frames können aber Lücken hinterlassen. Ein Wechsel der Quellidentität setzt den Überlappungszustand zurück. Stability verwendet begrenzte RMS-Korrektur mit kurzer/langer Nachführung; 0 bleibt unverändert, 1 korrigiert vollständig. Sie erzeugt keine Lookahead-Latenz, kann aber gewollte Dynamik abschwächen und Spektren verbreitern. Siehe [Spectral Group](spectral-group.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/ifft.md)
