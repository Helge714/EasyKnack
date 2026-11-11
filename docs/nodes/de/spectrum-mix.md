# Spektrummischung
Überblendet zwei zeitlich passende komplexe Spektren.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Linear, Equal Power; Standard: Linear | Editor |
| Faktor (Factor) | 0–1; Standard: 0.5; Factor, % | Value; 0–1; Control |
| A | — | Spectrum; Control |
| B | — | Spectrum; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum; Control |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Linear berechnet `A * (1 − f) + B * f`. Equal Power verwendet `cos(πf/2)` und `sin(πf/2)` als Gewichte. Faktor 0 liefert A, Faktor 1 liefert B; der Faktor wird auf 0–1 begrenzt und pro FFT-Frame mit dem Control-Wert des aktuellen Blocks angewendet. Equal Power erhält die erwartete Leistung unkorrelierter Signale; gleiche Signale werden in der Mitte um etwa 3 dB verstärkt. Gegenphasige Koeffizienten können sich auslöschen. FFT-Größe, Fenster, Hop, Samplerate und Frame-Positionen müssen übereinstimmen; nicht passende Batches erzeugen ein leeres Spektrum. Ein unverbundener Eingang ist 0. Für Teiltöne plus Rest stattdessen Spectrum Math mit Add verwenden.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | A → Spektrum (Spectrum) |
| Einfügen | A → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Spectrum Math](spectrum-math.md)
