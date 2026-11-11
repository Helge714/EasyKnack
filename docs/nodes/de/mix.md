# Mischung (Mix)
Überblendet zwischen zwei numerischen Signalen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Linear, Equal Power; Standard: Linear | Editor |
| Faktor (Factor) | 0–1 ×; Standard: 0.5 ×; ×, % | Wert; 0–1 ×; Auto |
| A | ±∞; Standard: 0 | Wert; ±∞; Auto |
| B | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞ |

## Verhalten
Eine neue Verbindung an einem belegten Eingang A oder B verschiebt die bisherige Verbindung zum anderen Eingang, wenn dieser frei und kompatibel ist. Andernfalls wird die bisherige Verbindung ersetzt.

Linear: `y = (1−t)A + tB`. Equal Power: `y = cos(πt/2)A + sin(πt/2)B`, mit t auf 0–1 begrenzt. Equal Power erhält die erwartete Leistung unkorrelierter Signale; gleiche Eingänge können in der Mitte etwa 3 dB gewinnen. Linear erhält den Pegel gleicher Eingänge.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | A → Ausgang (Out) |
| Einfügen | A → Ausgang (Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/mix.md)
