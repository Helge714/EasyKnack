# Teilton-Kontext
Liefert die Werte des aktuellen Teiltons innerhalb einer Teilton-Gruppe.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Teiltöne (Partials) | — | Partials; Control |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Frequenz (Frequency) | Value; 0–Nyquist Hz; Audio |
| Amplitude | Value; 0–∞; Audio |
| Phase | Value; ±∞ turn; Audio |
| ID | Value; 1–∞; Audio |
| Alter (Age) | Value; 0–∞ s; Audio |
| Index | Value; 0–32767; Audio |
| Frame-Zeit (Frame time) | Value; 0–∞ s; Audio |
| Seed | Value; 0–16 777 215; Audio |
| Tonalität (Tonality) | Value; 0–1; Audio |

## Verhalten
Die Werte gelten für den aktuellen Teilton im aktuellen Frame. Phase wird in Umdrehungen angegeben; Index bezeichnet die aktuelle Lautstärkesortierung, ID die verfolgte Identität. Alter und Frame-Zeit sind Sekunden. Zustände der Teilton-Gruppe werden anhand der ID zugeordnet.

ID bleibt die Teiltonkennung. Seed ist ein daraus abgeleiteter, exakt als Float darstellbarer 24-Bit-Wert für die Seed-Eingänge gewöhnlicher Noise-Nodes; er ist keine eindeutige Kennung. Partial Noise verwendet die Teilton-ID direkt und benötigt diese Verbindung nicht.

Tonalität ist der tonale Energieanteil `1 − Bandbreite` von Loris: 0 bedeutet vollständig rauschhaft, 1 vollständig sinusförmig. Dies ist keine Klassifikationswahrscheinlichkeit. Bei Bandbreitenmodell None beträgt der Wert 1.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Nicht schaltbar |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[English](../en/partial-context.md)
