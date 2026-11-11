# Audio-Ausgang (Audio Output)
Sendet Audio an ein Ausgabegerät.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audio-Eingang (Audio In) | — | Audio; ±∞; Audio |
| Gerät (Device) | Verfügbare Ausgabegeräte; Standard: Standardgerät | Editor |
| Gerätelautstärke (Device volume) | 0–100 %; Standard: Aktuelle Windows-Lautstärke; % | Editor; Windows-Ausgang |
| Lautstärketasten koppeln (Link volume keys) | Aus / Ein; Standard: Aus | Editor; Windows-Ausgang |
| Signal | Pegelanzeige | Editor; Windows-Ausgang |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
MIDI-Stimmen werden automatisch summiert. Gerätelautstärke steuert den Windows-Endpunkt und übernimmt externe Änderungen; sie ist kein zusätzlicher Signal-Gain. Gekoppelte Lautstärketasten verändern die gewählten Endpunkte proportional. Mute unterdrückt das EasyKnack-Signal, ohne das Windows-Gerät stummzuschalten.

Gekoppelte Lautstärketasten zeigen kurz eine Einblendung auf dem Desktop, auch über anderen Programmen. Bei mehreren gekoppelten Ausgängen entspricht die Prozentanzeige dem höchsten Ausgangspegel; die relativen Lautstärkeverhältnisse bleiben erhalten.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/audio-output.md)
