# Instanzgruppe (Instance Group)
Führt einen internen Graphen für mehrere unabhängige Instanzen aus.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Anzahl (Count) | 0–256, ganzzahlig; Standard: 8 | Wert; 0–Maximum, abgerundet; Control |
| Frequenz (Frequency) | ±∞; Standard: 220 | Wert; ±∞; Auto |
| Maximum (Maximum) | 1–256; Standard: 16 | Wert; Inspektor |
| Allgemein / IO / Editoren (General / IO / Editors) | Gruppendefiniert | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Audio; ±∞ |

## Verhalten

[Anleitung](../../user/de/instance-group.md)
Count kann über einen kopierten Slider-Editor an einer umschließenden normalen Gruppe bereitgestellt werden. Der äußere Port bleibt ein gewöhnlicher, bearbeitbarer Gruppeneingang; die feste Count-Sonderrolle gehört zur Instance Group selbst.

Der Anfangsgraph erzeugt harmonische Oszillatoren und einen [Instance Merge](instance-merge.md); Frequency ist sein anfänglicher öffentlicher Parameter, kein dauerhaft vorgeschriebener Port. Jede Instanz besitzt eigenen DSP-Zustand. [Instance Context](instance-context.md) liefert Index, Count, Position und Seed. Maximum reserviert Zustand/Speicher; Count wählt die aktive Anzahl und ist kein gewöhnlicher Group-IO-Port. Mehrere Merge-Zweige und gemeinsame Verarbeitung danach sind möglich. Verschachtelte Instanz-/Spektralgruppen werden unterstützt. Öffentliche Eingänge erlauben Zahlen, Buffer, Vector, Wavetable und Spectrum; Ausgänge derzeit Zahlen oder Spectrum. MIDI und andere Ressourcengrenzen werden nicht unterstützt. Beide Buffer Reader werden unterstützt. Externe Buffer werden gemeinsam gelesen; jede Instanz behält ihren eigenen Wiedergabezustand. Interne Live-Buffer besitzen pro Instanz eigenen Speicher. Gleiche Generatoren können Arbeit teilen, solange Kontext und Verlauf gleich bleiben.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Gewählter Gruppeneingang (Configured group input) → Gewählter Gruppenausgang (Configured group output) |
| Einfügen | Gewählter Gruppeneingang (Configured group input) → Gewählter Gruppenausgang (Configured group output) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/instance-group.md)
