# Bitcrusher (Bitcrusher)
Verringert Amplitudenauflösung und Aktualisierungsrate.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor |
| Eingang (In) | ±1; Standard: 0 | Wert; ±1; Auto |
| Bits | 1–24, ganzzahlig; Standard: 8 | Wert; 1–24, gerundet; Auto |
| Unterabtastung (Downsample) | 1–1024, ganzzahlig; Standard: 1 | Wert; 1–1024, gerundet; Auto |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±1 |

## Verhalten
Quantisiert den begrenzten Eingang auf die gewählte Bittiefe und hält einen Wert über Downsample-Auswertungen. Downsample 1 aktualisiert bei jeder Auswertung. Vor dieser absichtlichen Ratenreduktion findet keine Anti-Aliasing-Tiefpassfilterung statt.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bitcrusher.md)
