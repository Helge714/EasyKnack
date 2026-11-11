# Vektor zusammensetzen (Vector Combine)
Setzt X, Y und Z zu einem Vektor zusammen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| X | ±∞; Standard: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Wert; ±∞; Auto |
| Y | ±∞; Standard: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Wert; ±∞; Auto |
| Z | ±∞; Standard: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Wert; ±∞; Auto |
| Größe (Quantity) | Länge, Winkel, Skalierung, Einheitenlos; Standard: Länge | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Vektor (Vector) | Vektor; ±∞ je Komponente |

## Verhalten
Der Ausgangsvektor ist `(X,Y,Z)`. Jeder Zahleneingang kann unabhängig Audio oder Control führen. Quantity wählt Editoren für Länge, Winkel, Skalierung oder einheitenlose Komponenten. Für einen gleichmäßig veränderten Ort muss Audio im nachfolgenden Vektorpfad erhalten bleiben.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/vector-combine.md)
