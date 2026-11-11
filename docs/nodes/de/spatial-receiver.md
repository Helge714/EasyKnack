# Räumlicher Empfänger (Spatial Receiver)
Gibt eine positionierte Quelle an einem Mono- oder Stereoempfänger aus.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Kanäle (Channels) | Mono, Stereo; Standard: Stereo | Editor |
| Audioeingang (Audio In) | — | Audio; ±∞; Audio |
| Quellposition (Source Position) | ±∞ je Komponente; Standard: (0, 0, 1) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Empfängerposition (Receiver Position) | ±∞ je Komponente; Standard: (0, 0, 0) m; m, km, cm, mm | Vektor; ±∞ je Komponente; Auto |
| Drehwinkel (Yaw) | ±∞ °; Standard: 0°; °, rad, turn | Wert; ±∞ °; Auto; Stereo |
| Empfängerabstand (Receiver Spacing) | 0–∞ m; Standard: 0.18 m; m, km, cm, mm | Wert; 0–∞ m; Auto; Stereo |
| Entfernungsdämpfung (Distance Attenuation) | Aus / An; Standard: An | Editor |
| Referenzabstand (Reference Distance) | 0.001–∞ m; Standard: 1 m; m, km, cm, mm | Wert; Inspektor |
| Schalllaufzeit (Sound Travel Time) | Aus / An; Standard: Aus | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±∞ |
| Abstand (Distance) | Wert; 0–∞ m |

## Verhalten
Stereoeingänge werden zu einer Mono-Punktquelle gemittelt. Stereoempfänger liegen je einen halben Spacing entlang der gedrehten Rechtsachse versetzt; Distance wird pro Seite gemessen. Mono verwendet die Mitte und ignoriert Yaw/Spacing. Die Dämpfung beträgt `reference/max(reference,distance)`; Stereo verwendet leistungsbezogene Richtungsverstärkung. Schalllaufzeit verzögert um `distance/343` Sekunden, bis höchstens 2 Sekunden (686 m); weiter entfernte Quellen sind bei Aktivierung stumm. Geänderter Abstand verändert dann die Tonhöhe. Verwende gleichmäßige Bewegung mit Audiorate für kontinuierliche Dopplerverläufe. Dies rendert Punktquellen im Freifeld, keine Raumakustik, Mikrofoncharakteristik oder HRTF-Simulation.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Audioeingang (Audio In) → Audioausgang (Audio Out) |
| Einfügen | Audioeingang (Audio In) → Audioausgang (Audio Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spatial-receiver.md)
