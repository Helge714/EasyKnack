# Digital Noise (Digital Noise)
Erzeugt zyklusbasierte zufällige Frequenzmodulation.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Frequenz (Frequency) | 1e-5–20 000 Hz; Standard: 440 Hz; Hz, kHz | Wert; 0–20 000 Hz; Auto |
| Stärke (Strength) | 0–1; Standard: 0 | Wert; 0–1; Auto |
| Verteilung (Distribution) | Gleichverteilt, Gauß; Standard: Gleichverteilt | Editor |
| Startwert (Seed) | −1–16 777 215; Standard: −1 | Wert; −1–16 777 215, gerundet; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Faktor (Factor) | Wert; 2^(−s)–2^s; Audio |
| Trigger | Trigger |
| Frequenz (Frequency) | Wert; f × Factor; Audio |

## Verhalten
Factor wird über einen selbstmodulierten Zyklus gehalten; Frequency liefert `f × Factor`. Stärke `s` begrenzt die Oktavabweichung auf `±s`. Gauß verwendet die Standardabweichung `s/3`, begrenzt auf `±s`. Die Ziehung berücksichtigt die Zyklusdauer, damit zeitgewichtete Oktavabweichungen symmetrisch sind. Der arithmetische Hz-Mittelwert bleibt dadurch nicht konstant. Stärke 0 ergibt Faktor 1 und reicht Frequency unverändert durch. Trigger markiert eine neue Ziehung. Seed entspricht [Rauschen](noise.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/digital-noise.md)
