# Messwertrekorder (Measurement Recorder)
Fügt getriggerte Zahlenschnappschüsse einer Dokumenttabelle hinzu.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Messungen (Measurements) | Tabelle / CSV / Leeren; Standard: Leer | Editor |
| Aufzeichnen (Record) | Schaltfläche | Trigger; Trigger; Trigger |
| Eingänge (Inputs) | 1–8; Standard: 1 | Editor |
| Werte: Namen, Einheiten, Reihenfolge (Values: names, units, order) | Einstellungen je Eingang; Standard: Wert 1…8; keine Einheit | Editor; Inspektor |
| Wert 1…8 (Value 1…8) | Standard: 0 | Wert; ±∞; Auto; Entsprechend Eingänge |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
Jeder Aufzeichnen-Trigger ergänzt eine Zeile mit allen Eingängen an dessen Sampleposition. Für die Aufnahme muss der Audiohost laufen. Die Tabelle mit bis zu 4096 Zeilen wird im .knack-Dokument gespeichert. Öffne sie, um die Testbezeichnung nach dem Zeilenindex zu bearbeiten. Inspektorzeilen steuern Namen, Einheitenbeschriftungen und gemeinsame Port-/Spaltenreihenfolge; Beschriftungen rechnen Werte nicht um. CSV bietet Standard- und deutsches Excel-Format. Leeren entfernt gespeicherte Zeilen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/measurement-recorder.md)
