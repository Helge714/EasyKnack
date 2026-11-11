# Teilton-Gruppe
Bearbeitet jeden erkannten Teilton einmal pro Analyseframe.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Teiltöne (Partials) | — | Partials; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Teiltöne (Partials) | Partials; Control |

## Verhalten

[Anleitung](../../user/de/partial-group.md)
Standardaufbau: `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Math (Add) → IFFT`; den Rest der Analyse mit dem zweiten Math-Eingang verbinden. Im Inneren `Partial Context → Math / Formula Group → Partial Output` verwenden. Frequenz, Amplitude und Phase bleiben unabhängige Werte. Zustände sind pro ID getrennt und werden bei einer neuen ID zurückgesetzt. Maximal 64 numerische Prozessoren; gewöhnliche verschachtelte Gruppen und numerische Formula Groups werden aufgelöst. Keine Audio-Oszillatorbank, Buffer oder Triggerprogramme. Externe numerische Werte werden am Fensterende aus dem aktuellen Veröffentlichungsblock abgetastet; asynchrone historische Modulationswerte sind nicht gepuffert.

Für unabhängige Zufallsmodulation pro Teilton [Partial Noise](partial-noise.md) verwenden. Der Node berücksichtigt stabile IDs automatisch; für eine symmetrische Abweichung den Ausgang mit `2 * random - 1` umrechnen.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Teiltöne (Partials) → Partials |
| Einfügen | Teiltöne (Partials) → Partials |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[English](../en/partial-group.md)
