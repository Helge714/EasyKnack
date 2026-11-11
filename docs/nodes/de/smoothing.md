# Glättung (Smoothing)
Glättet steigende und fallende Werte exponentiell.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Standard: Audio | Editor; Inspektor |
| Eingang (In) | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Anstieg (Rise) | 0–60 s; Standard: 0.03 s; s, ms, smp | Wert; 0–60 s; Auto |
| Abfall (Fall) | 0–60 s; Standard: 0.03 s; s, ms, smp | Wert; 0–60 s; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞ |

## Verhalten
Für die Zeitkonstante τ gilt `y += (1 − exp(−dt/τ)) × (x − y)`, bei Anstieg mit Rise, bei Abfall mit Fall. Eine τ erreicht etwa 63% eines Sprungs, drei τ etwa 95%. Null folgt sofort. Der erste gültige Wert initialisiert direkt. Audio glättet Control-Sprünge sampleweise; Control aktualisiert blockweise. Dies ist keine lineare Rampe fester Dauer.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/smoothing.md)
