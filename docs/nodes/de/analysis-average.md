# Analysemittelung (Analysis Average)
Führt eine getriggerte arithmetische Messung aus und hält ihr Endergebnis.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Analyse (Analysis) | — | Analysis-Ressource; Spektrum oder Korrelation; Control |
| Start | Schaltfläche | Trigger; Trigger; Trigger |
| Messen nach (Measure by) | Zeit / Messungen; Standard: Zeit | Editor |
| Dauer (Duration) | 1 smp–86400 s; Standard: 1 s; s, ms, smp | Editor; Zeit |
| Messungen (Measurements) | 1–16777216; Standard: 16 | Editor; Messungen |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Mittelwert (Mean) | Analysis-Ressource; Spektrum oder Korrelation; Control |
| Anzahl (Count) | Wert; ≥ 0; Audio |
| Abgeschlossen (Finished) | Trigger |

## Verhalten
Start löscht und beginnt die Aufnahme neu und übernimmt Dauer/Messungen. Zeit misst die Audiouhrdauer; Messungen stoppt nach der gewählten Anzahl akzeptierter Werte. Abgeschlossen sendet einmal beim Ende. Das laufende Ergebnis ist während der Aufnahme verfügbar und bleibt danach bis Start stehen.

Counts distinct delivered FFT/correlation frames. Spectrum averaging takes arithmetic mean power per bin, displaying its square root as magnitude; phase is discarded, so this is not an IFFT reconstruction source. Correlation values are averaged per lag. A source or FFT geometry change invalidates the acquisition; restart it for the new configuration. Connect [Analysis Viewer](analysis-viewer.md) or feature extractors to Mean.~Zählt unterschiedliche gelieferte FFT-/Korrelationsframes. Die Spektralmittelung bildet je Bin die arithmetische mittlere Leistung und zeigt deren Quadratwurzel als Magnitude; die Phase entfällt, daher ist dies keine IFFT-Rekonstruktionsquelle. Korrelationswerte werden je Lag gemittelt. Quellen- oder FFT-Geometrieänderungen verwerfen die Aufnahme; starte sie für die neue Konfiguration erneut. Verbinde [Analysis Viewer](analysis-viewer.md) oder Merkmalsextraktoren mit Mittelwert.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Analyse (Analysis) → Mittelwert (Mean) |
| Einfügen | Analyse (Analysis) → Mittelwert (Mean) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/analysis-average.md)
