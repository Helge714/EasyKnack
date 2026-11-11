# Teiltonrauschen
Liefert reproduzierbare Zufallswerte pro erkanntem Teilton und Analyseframe.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Seed | 0–2 147 483 647, ganzzahlig; Standard: 0 | Wert; 0–2 147 483 647; Auto |
| Pro Teilton variieren (Vary by partial) | Aus / An; Standard: An | Editor |
| Pro Frame variieren (Vary by frame) | Aus / An; Standard: An | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Zufallswert (Random) | Wert; 0–1, ohne 1; Audio (Teilton/Frame) |

## Verhalten
Nur innerhalb einer Partial Group. Der Zufallswert ist gleichverteilt in [0, 1) und wird aus Seed, stabiler Teilton-ID und FFT-Frame-Position gebildet. Pro Teilton variieren trennt die Teiltöne; deaktiviert erhalten alle Teiltöne desselben Frames denselben Wert. Pro Frame variieren erzeugt neue Werte für jedes FFT-Fenster; deaktiviert bleibt der Wert über die Lebensdauer eines Teiltons konstant. Sind beide Optionen deaktiviert, hängt der Wert nur vom Seed ab. Lautstärkesortierung, Verarbeitungsreihenfolge und Kanal verändern die Zuordnung nicht. Für symmetrische Oktavmodulation `f_neu = f * pow(2, strength * (2 * random - 1))` verwenden. Der Seed-Ausgang von Partial Context wird hierfür nicht benötigt.

Ein verbundener Seed wird je ausgewertetem Teilton abgetastet, abgerundet und auf 0–2 147 483 647 begrenzt. Nicht endliche Werte verwenden den manuellen Seed. Ohne Verbindung bleibt der ganzzahlige Editorwert exakt erhalten. Ein Kontextindex oder Gruppeninput kann den Seed je Instanz verändern.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Partial Group](partial-group.md) · [Partial Context](partial-context.md)
