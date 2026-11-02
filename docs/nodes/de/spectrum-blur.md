# Spektrum weichzeichnen (Spectrum Blur)

## Übersicht

Verteilt Spektralinhalt über benachbarte Frequenzen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Kernel | [Editor](../../user/de/reference-tables.md) | Gauß / Box / Maximum | — | Gauß | — | — | Immer |
| Synthese (Synthesis) | [Editor](../../user/de/reference-tables.md) | Rauschen / Kontinuierlich / Erhalten | — | Rauschen | — | — | Immer |
| Leistung normieren (Normalize power) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Breite (Width) | [Wert](../../user/de/data-types.md#value) | ≥ 0 oct / bins | ≥ 0 oct / bins | 0 oct | [oct / cent / st / bins](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | Komplexe FFT-Bins | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Die Einheit wird im Kontextmenü des Sliders oder Ports gewählt. Oktaven, Cent und Halbtöne beschreiben denselben logarithmischen Radius: Eine Breite b in Oktaven umfasst `f·2^(−b)` bis `f·2^b`; 1 oct = 1200 cent = 12 st. Bins wählt einen linearen Binradius. Beim Wechsel zwischen Bins und Tonhöheneinheiten bleibt die gespeicherte Radiuszahl erhalten und ändert ihre Interpretation; eine allgemeine Umrechnung gibt es nicht. DC bleibt bei logarithmischer Breite unverändert. Gaussian verwendet einen Radius von drei Standardabweichungen, Box gewichtet die Nachbarschaft gleichmäßig und Maximum übernimmt die größte Magnitude. Noise erzeugt kontinuierliches Rauschen, Continuous binzentrierte Töne und Preserve erhält die Koeffizientenphase. Breite null ist ein exakter Bypass. Bei sehr kleinen Rauschbreiten ist die Phasenabweichung für einen stetigen Übergang begrenzt. Normalize power erhält die gesamte spektrale Leistung, im Noise-Modus als Erwartungswert statt als Begrenzer pro Frame. Steuerwerte werden pro eingehendem Frame ausgewertet.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Spektrum (Spectrum) | Spektrum (Spectrum) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Spektrum (Spectrum) | Spektrum (Spectrum) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/spectrum-blur.md)
