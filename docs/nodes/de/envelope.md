# Hüllkurve (Envelope)
Erzeugt eine kurvenförmige Hüllkurve aus Gate und Trigger.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Hüllkurve (Envelope) | Kurveneditor; Standard: ADSR | Editor |
| Gate | 0–1; Standard: 0 | Wert; ±∞; Audio |
| Trigger | Button | Trigger; Trigger |
| Attack + Decay | 0–60 000 ms; Standard: 300 ms; ms, s, smp | Wert; 0–60 000 ms; Auto |
| Ausklingen (Release) | 0–60 000 ms; Standard: 300 ms; ms, s, smp | Wert; 0–60 000 ms; Auto |
| Halteposition (Sustain position) | 0.01–0.99; Standard: 0.5 | Wert; 0.01–0.99; Auto |
| Halten (Hold) | Aus / An; Standard: An | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Hüllkurve (Envelope) | Wert; 0–1; Audio |

## Verhalten
Ein Gate-Anstieg über 0 oder ein Trigger startet Attack/Decay. Die Halteposition teilt die Kurve in Attack/Decay und Release. Bei Hold hält ein positives Gate den Sustainwert; das Loslassen startet Release vom aktuellen Pegel. Ohne Hold folgt Release automatisch auf Attack/Decay. Retrigger beginnen beim aktuellen Hüllkurvenpegel. Das Ergebnis ist auf 0–1 begrenzt.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/envelope.md)
