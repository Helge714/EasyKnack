# Verstärkung (Gain)
Skaliert ein Audiosignal mit einem Amplitudenfaktor.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Verstärkung (Gain) | 0–1.995262 ×; Standard: 0.251189 ×; ×, %, dB | Wert; 0–1e6 ×; Auto |
| Audioeingang (Audio In) | — | Audio; ±∞; Audio |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±∞ |

## Verhalten
`y = x × gain`, mit `gain = 10^(dB/20)`. Manuelle −12 dB entsprechen etwa 0,251189×. Werte bis −160 dB schalten das Signal stumm. Verbundene Verstärkung wird auf 120 dB begrenzt; manuelle Änderungen werden geglättet. Gain begrenzt das Audiosignal nicht.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Audioeingang (Audio In) → Audioausgang (Audio Out) |
| Einfügen | Audioeingang (Audio In) → Audioausgang (Audio Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/gain.md)
