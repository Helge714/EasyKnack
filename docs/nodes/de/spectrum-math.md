# Spektrummathematik
Verknüpft zwei zeitlich passende Spektren durch komplexe Grundrechenarten.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide; Standard: Add | Editor |
| A | — | Spectrum; Control |
| B | — | Spectrum; Control |
| Verstärkung A (Gain A) | 0–∞; Standard: 1; Factor, % | Editor; Inspektor |
| Verstärkung B (Gain B) | 0–∞; Standard: 1; Factor, % | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spektrum (Spectrum) | Spectrum; Control |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Berechnet je komplexem FFT-Koeffizienten `op(A * Gain A, B * Gain B)`. Add und Subtract erhalten die Phasenbeziehungen. Multiply ist komplexe Multiplikation und entspricht keiner sampleweisen Multiplikation der Audiosignale. Division durch null sowie nicht endliche Ergebnisse liefern 0. Es gibt keine automatische Lautstärkennormierung. Ein unverbundener Eingang ist 0. Beide Spektren müssen FFT-Größe, Fenster, Hop, Samplerate und Frame-Positionen teilen; nicht passende Batches erzeugen ein leeres Spektrum. Für Teiltonrekonstruktion Add und beide Verstärkungen 1 verwenden. Alte gewichtete Spectrum-Mix-Nodes werden beim Laden mit unveränderten Verbindungen und Verstärkungen in diesen Node umgewandelt.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | A → Spektrum (Spectrum) |
| Einfügen | A → Spektrum (Spectrum) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[English](../en/spectrum-math.md)
