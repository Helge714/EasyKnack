# Spektralgruppe (Spectral Group)
Verarbeitet jeden Bin eines eingehenden Spektrums.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Spektrum (Spectrum) | — | Spectrum-Ressource; Control |
| Frame-Eingänge (Frame inputs) | Schnittstellenabhängig; Standard: Schnittstellenabhängig | Wert; Audio / Control / Trigger; Auto |
| Leere Bins überspringen (Skip empty bins) | Aus / An; Standard: Aus | Editor; Inspektor |
| Parallele Bins (Parallel bins) | Aus / An; Standard: An | Editor; Inspektor |
| Allgemein / IO / Editoren (General / IO / Editors) | Gruppendefiniert | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Spectrum-Ressource; Control |

## Verhalten

[Anleitung](../../user/de/spectral-group.md)
Ein Spectrum bestimmt Binanzahl und Framezeitpunkte. [Bin Context](bin-context.md) stellt komplexe Komponenten, Frequenz und Index bereit; [Bin Reader](bin-reader.md) liest andere Bins. [Bin Output](bin-output.md) schreibt unabhängige komplexe Ausgänge; die Anfangsverdrahtung reicht die komplexen Werte unverändert durch. Externe Frame-Werte werden am FFT-Fensterende abgetastet; asynchrone Pfade erhalten ihre zeitliche Zuordnung. Frame-Trigger wirken auf den zugehörigen Frame. Reine Frame-Verarbeitung läuft einmal je Bin/Frame, nicht je Audiosample. Oszillatoren/Rauschen erzeugen dauerndes Audio je Bin, reduziert über Instance Merge; Bin Output nimmt dieses Audio nicht an. Bin Discard läuft vor der Binberechnung und betrifft alle Spectrum-Ausgänge. Parallele Worker erhalten unabhängige Binzustände. [IFFT](ifft.md) rekonstruiert komplexe Spektren; eine Oszillatorbank ist keine phasengleiche IFFT.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Spektrum (Spectrum) → Ausgang (Out) |
| Einfügen | Spektrum (Spectrum) → Ausgang (Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/spectral-group.md)
