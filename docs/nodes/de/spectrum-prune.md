# Spektrum ausdünnen (Spectrum Prune)
Behält den lautesten Anteil der Spektralbins.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Behalten (Keep) | 0–1; Standard: 1; Faktor, % | Wert; 0–1; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum-Ressource; Komplexe FFT-Bins; Control |

## Verhalten
Die Anzahl wird aus Behalten mal Anzahl nichtnegativer Bins gerundet, einschließlich DC und Nyquist. Die Rangfolge verwendet einseitige Amplituden; bei Gleichstand werden kleinere Indizes bevorzugt. Erhaltene Koeffizienten und Phasen bleiben unverändert, ohne Pegelnormierung. Behalten 0 löscht alle Bins, 1 umgeht die Verarbeitung. Eine [Spectral Group](spectral-group.md) kann Nullbins bei aktivem Skip empty bins überspringen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Spektrum (Spectrum) → Spektrum (Spectrum) |
| Einfügen | Spektrum (Spectrum) → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectrum-prune.md)
