# Formelgruppe (Formula Group)
Berechnet eigene Formeln mit konfigurierbarer Gruppenschnittstelle.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| signal | ±∞; Standard: 0 | Wert; ±∞; Auto |
| Quelltext (Source) | Formeleditor; Standard: result = signal; | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Live-Vorschau (Live preview) | Aus / An; Standard: An | Editor; Inspektor |
| Gruppenschnittstelle (Group IO) | Zahlen-, Trigger- und Bufferports; Standard: signal → result | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| result | Wert; ±∞ |

## Verhalten

[Formeln schreiben](../../user/de/formula-group.md) erklärt Ports, Zustände und Auswertungsrate. Buffer-Ressourcen sind nur als Eingänge unterstützt. Zustand ist pro Kanal, Stimme und Instanz getrennt. Audio-/Triggereingänge dürfen durch die gewählte Rate nicht unbemerkt verloren gehen.

In Spectral Groups werden gültige Formeln pro Bin/Frame ausgeführt; zeitlicher Samplekontext und Binkontext sind verschieden. Ungültiger neuer Code erhält das letzte akzeptierte Programm. Funktionshilfe und Vervollständigung beschreiben unterstützte Funktionen und Definitionsbereiche. `tanh` ermöglicht weiche Sättigung; Oversampling und Pegelkompensation erfolgen nicht automatisch.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Gewählter Gruppeneingang (Configured group input) → Gewählter Gruppenausgang (Configured group output) |
| Einfügen | Gewählter Gruppeneingang (Configured group input) → Gewählter Gruppenausgang (Configured group output) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/formula-group.md)
