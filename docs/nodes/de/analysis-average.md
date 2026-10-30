# Analysemittelung (Analysis Average)

## Übersicht

Führt eine getriggerte arithmetische Messung aus und hält ihr Endergebnis.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Analyse (Analysis) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | — | Spektrum oder Korrelation | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Start | [Trigger](../../user/de/data-types.md#trigger) | Schaltfläche | Trigger | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |
| Messen nach (Measure by) | [Editor](../../user/de/reference-tables.md) | Zeit / Messungen | — | Zeit | — | — | Immer |
| Dauer (Duration) | [Editor](../../user/de/reference-tables.md) | 1 smp–86400 s | — | 1 s | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Zeit |
| Messungen (Measurements) | [Editor](../../user/de/reference-tables.md) | 1–16777216 | — | 16 | — | — | Messungen |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Mittelwert (Mean) | [Analysis-Ressource](../../user/de/data-types.md#analysis) | Spektrum oder Korrelation | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Anzahl (Count) | [Wert](../../user/de/data-types.md#value) | ≥ 0 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |
| Abgeschlossen (Finished) | [Trigger](../../user/de/data-types.md#trigger) | — | [Trigger](../../user/de/rates.md#trigger) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Start löscht und beginnt die Aufnahme neu und übernimmt Dauer/Messungen. Zeit misst die Audiouhrdauer; Messungen stoppt nach der gewählten Anzahl akzeptierter Werte. Abgeschlossen sendet einmal beim Ende. Das laufende Ergebnis ist während der Aufnahme verfügbar und bleibt danach bis Start stehen.

Counts distinct delivered FFT/correlation frames. Spectrum averaging takes arithmetic mean power per bin, displaying its square root as magnitude; phase is discarded, so this is not an IFFT reconstruction source. Correlation values are averaged per lag. A source or FFT geometry change invalidates the acquisition; restart it for the new configuration. Connect [Analysis Viewer](analysis-viewer.md) or feature extractors to Mean.~Zählt unterschiedliche gelieferte FFT-/Korrelationsframes. Die Spektralmittelung bildet je Bin die arithmetische mittlere Leistung und zeigt deren Quadratwurzel als Magnitude; die Phase entfällt, daher ist dies keine IFFT-Rekonstruktionsquelle. Korrelationswerte werden je Lag gemittelt. Quellen- oder FFT-Geometrieänderungen verwerfen die Aufnahme; starte sie für die neue Konfiguration erneut. Verbinde [Analysis Viewer](analysis-viewer.md) oder Merkmalsextraktoren mit Mittelwert.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Analyse (Analysis) | Mittelwert (Mean) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Analyse (Analysis) | Mittelwert (Mean) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/analysis-average.md)
