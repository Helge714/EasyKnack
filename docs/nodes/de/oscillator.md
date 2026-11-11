# Oszillator (Oscillator)
Erzeugt eine periodische Wellenform.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wellenform (Waveform) | Sinus, Dreieck, Sägezahn, Rechteck; Standard: Sinus | Wavetable-Ressource; Control |
| Frequenz (Frequency) | 1e-5–20 000 Hz; Standard: 440 Hz; Hz, kHz | Wert; 0–20 000 Hz; Auto |
| Phase | ±∞ turn; Standard: 0 turn; turn, °, rad | Wert; ±∞ turn; Auto |
| Zurücksetzen (Reset) | Button | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±1 |

## Verhalten
Die laufende Phase steigt pro Sample um `f / fs` Umdrehungen. Phase addiert einen Versatz ohne Neustart; Reset startet vor dem betreffenden Sample neu. Bei Frequenz 0 bleibt die Phase stehen und kann einen konstanten Ausgang erzeugen. Die gewählte oder verbundene Wavetable wird frequenzabhängig bandbegrenzt gelesen. Die Phase wrappt nach einer Umdrehung. Ein manueller Reset erfolgt am nächsten Blockanfang.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/oscillator.md)
