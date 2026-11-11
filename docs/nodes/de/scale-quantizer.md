# Skalenquantisierer (Scale Quantizer)
Rundet Frequenzen auf eine gleichstufige Tonleiter.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Skala, Fest; Standard: Skala | Editor |
| Grundton (Root) | C, C#, D, D#, E, F, F#, G, G#, A, A#, B; Standard: C | Editor |
| Skala (Scale) | Chromatisch, Dur, Moll, Durpentatonik, Mollpentatonik; Standard: Chromatisch | Editor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Frequenz (Frequency) | 0–20 000 Hz; Standard: 440 Hz; Hz, kHz | Wert; Positive Hz; Auto |
| Festes Ziel (Fixed target) | 1–20 000 Hz; Standard: 440 Hz; Hz, kHz | Wert; Positive Hz; Auto |
| A4-Stimmung (A4 tuning) | 400–480 Hz; Standard: 440 Hz; Hz, kHz | Wert; Inspektor |
| Hysterese (Hysteresis) | 0–12 st; Standard: 0.1 st; st, cent | Wert; 0–12 st; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ziel (Target) | Wert; 0–∞ Hz |

## Verhalten
Scale bildet `69 + 12 log2(f / A4)` auf die nächste erlaubte Note ab und rechnet zurück in Hz. Bei gleichem Abstand gewinnt die tiefere Note. Hysterese ergänzt einen Umschaltabstand hinter der Mitte; 0 schaltet sofort. Nichtpositive/ungültige Eingänge liefern 0. Chromatisch ignoriert Root. Fixed verwendet das feste Ziel statt der Skala. Ein einschließlich beider Grenzen durchlaufener Oktavbereich kann 13 Noten ergeben: zwölf Halbtonschritte besitzen dreizehn Endpunkte.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Frequenz (Frequency) → Ziel (Target) |
| Einfügen | Frequenz (Frequency) → Ziel (Target) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/scale-quantizer.md)
