# Binglättung (Bin Smoothing)
Mischt neue Binwerte in einen Speicher je Bin.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wert (Value) | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Aktualisierung (Update) | 0–1; Standard: 1 | Wert; 0–1; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wert (Value) | Wert; ±∞; Audio (Bin/Frame) |

## Verhalten
Jeder FFT-Frame berechnet `held = (1−u) × held + u × value`. Der erste Frame initialisiert direkt. Update 0 hält und 1 ersetzt. Kleinere Hops aktualisieren häufiger; derselbe Faktor besitzt daher bei anderer Hopgröße ein anderes Zeitverhalten. Audioeingänge werden am Fensterende abgetastet. Nur in [Spectral Groups](spectral-group.md) gültig.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Wert (Value) → Wert (Value) |
| Einfügen | Wert (Value) → Wert (Value) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-smoothing.md)
