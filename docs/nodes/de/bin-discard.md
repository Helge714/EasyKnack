# Bin verwerfen (Bin Discard)
Überspringt einen Bin vor dessen weiterer Berechnung.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Verwerfen (Discard) | Aus / An; Standard: Aus | Wert; 0 behält; ungleich 0 verwirft; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| — | — |

## Verhalten
In einer [Spectral Group](spectral-group.md) wird die Bedingung je Frame vor der übrigen Binarbeit ausgewertet. Ein verworfener Bin ist in allen Spectrum-Ausgängen null und trägt kein erzeugtes Audio bei. Die Bedingung darf nur von Frame-/Binwerten abhängen, nicht von erzeugtem zeitlichem Audio. Dies verwirft Berechnungen, ist aber selbst keine Amplitudenschwelle; definiere diese mit einem Comparator.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-discard.md)
