# Binleser (Bin Reader)
Liest einen indizierten Bin des ursprünglichen Eingangsspektrums.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Control |
| Binindex (Bin index) | ±∞; Standard: 0 | Wert; Randmodusabhängig; Auto |
| Außerhalb des Spektrums (Outside spectrum) | Null, Begrenzen, Wrap; Standard: Null | Editor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Realteil (Real) | Wert; ±∞; Audio (Bin/Frame) |
| Imaginärteil (Imaginary) | Wert; ±∞; Audio (Bin/Frame) |
| Betrag (Magnitude) | Wert; 0–∞; Audio (Bin/Frame) |
| Phase | Wert; ±π rad; Audio (Bin/Frame) |
| Amplitude | Wert; 0–∞; Audio (Bin/Frame) |
| Frequenz (Frequency) | Wert; 0–fs/2 Hz; Audio (Bin/Frame) |

## Verhalten
In einer [Spectral Group](spectral-group.md) interpolieren gebrochene Indizes benachbarte komplexe Bins. Gelesen wird immer der ursprüngliche Quellframe, unabhängig von anderen Binschreibvorgängen. Zero liefert außerhalb des einseitigen Spektrums null, Clamp hält den Rand, Wrap wiederholt 0…N/2. Ohne Spectrum werden Nullen ausgegeben. Amplitude teilt den Rohbetrag durch die Analysefenstersumme und verdoppelt Bins außer DC/Nyquist; sie eignet sich für Oszillatorgewichte. Phase ist in Radiant.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/bin-reader.md)
