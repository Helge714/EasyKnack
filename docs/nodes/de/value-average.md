# Wertemittelung (Value Average)
Führt eine getriggerte arithmetische Messung aus und hält ihr Endergebnis.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wert (Value) | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Start | Schaltfläche | Trigger; Trigger; Trigger |
| Messen nach (Measure by) | Zeit / Messungen; Standard: Zeit | Editor |
| Dauer (Duration) | 1 smp–86400 s; Standard: 1 s; s, ms, smp | Editor; Zeit |
| Messungen (Measurements) | 1–16777216; Standard: 16 | Editor; Messungen |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Mittelwert (Mean) | Wert; ±∞; Audio |
| Anzahl (Count) | Wert; ≥ 0; Audio |
| Abgeschlossen (Finished) | Trigger |
| Standardabweichung (Standard Deviation) | Wert; ≥ 0; Audio |
| Minimum | Wert; ±∞; Audio |
| Maximum | Wert; ±∞; Audio |

## Verhalten
Start löscht und beginnt die Aufnahme neu und übernimmt Dauer/Messungen. Zeit misst die Audiouhrdauer; Messungen stoppt nach der gewählten Anzahl akzeptierter Werte. Abgeschlossen sendet einmal beim Ende. Das laufende Ergebnis ist während der Aufnahme verfügbar und bleibt danach bis Start stehen.

Audio contributes every finite sample; Control contributes once per block, plus a value at Start. Mean = Σx/N, population standard deviation = sqrt(Σ(x−mean)²/N). Minimum/Maximum describe the same accepted values; deviation is zero for fewer than two. All retain the input’s numerical unit.~Audio liefert jedes endliche Sample; Control einmal je Block sowie einen Wert bei Start. Mittelwert = Σx/N, Populationsstandardabweichung = sqrt(Σ(x−mean)²/N). Minimum/Maximum beschreiben dieselben akzeptierten Werte; bei weniger als zwei ist die Abweichung null. Alle behalten die numerische Eingangseinheit.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Wert (Value) → Mittelwert (Mean) |
| Einfügen | Wert (Value) → Mittelwert (Mean) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/value-average.md)
