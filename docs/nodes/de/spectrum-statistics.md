# Spektralstatistik (Spectrum Statistics)
Reduziert angezeigte Spektralwerte auf eine Zahl.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Komplexe FFT-Bins; Control |
| Daten (Data) | Magnitude / Phase / Power; Standard: Magnitude | Editor |
| Statistik (Statistic) | Peak / Mean / RMS / Integral; Standard: Peak | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wert (Value) | Wert; Statistikabhängig; Control |

## Verhalten
Magnitude ist die einseitige Amplitude, Power deren Quadrat und Phase der Winkel in Radiant. Für die gewählten Binwerte v[k]: Peak = max(abs(v)), Mean = Σv/K, RMS = sqrt(Σv²/K), Integral = Σv·Δf. Bin-RMS ist nicht das Audio-RMS im Zeitbereich. Das Ergebnis bleibt bis zum nächsten Frame stehen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectrum-statistics.md)
