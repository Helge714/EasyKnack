# Spektrum weichzeichnen (Spectrum Blur)

## Übersicht

Verteilt Spektralinhalt über benachbarte Frequenzen.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | Komplexe FFT-Bins | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Verteilung (Distribution) | [Editor](../../user/de/reference-tables.md) | Bins / Oktaven / Faktor (Legacy) | — | Oktaven | — | — | Immer |
| Kernel | [Editor](../../user/de/reference-tables.md) | Gauß / Box / Maximum | — | Gauß | — | — | Immer |
| Synthese (Synthesis) | [Editor](../../user/de/reference-tables.md) | Rauschen / Kontinuierlich / Erhalten | — | Rauschen | — | — | Immer |
| Leistung normieren (Normalize power) | [Editor](../../user/de/reference-tables.md) | Aus / Ein | — | Aus | — | — | Immer |
| Oktaven (Octaves) | [Wert](../../user/de/data-types.md#value) | ≥ 0 oct | ≥ 0 oct | 0 oct | [oct](../../user/de/units.md) | [Control](../../user/de/rates.md#control) | Oktaven |
| Faktor (Factor) | [Wert](../../user/de/data-types.md#value) | ≥ 1 | ≥ 1 | 1 | — | [Control](../../user/de/rates.md#control) | Faktor (Legacy) |
| Breite (Bins) (Width (bins)) | [Wert](../../user/de/data-types.md#value) | 0–8192 | 0–8192 | 0 | bins | [Control](../../user/de/rates.md#control) | Bins |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | Komplexe FFT-Bins | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Oktaven legt einen logarithmischen Radius fest: Breite b umfasst `f·2^(−b)` bis `f·2^b`; Faktor q umfasst f/q bis f·q. DC bleibt in logarithmischen Modi unverändert. Bins verwendet einen linearen Binradius. Gauß verwendet einen Radius von drei Standardabweichungen; Box gewichtet die Umgebung gleichmäßig; Maximum übernimmt deren größte Magnitude. Rauschen synthetisiert kontinuierliches Rauschen über Frequenzbereiche, Kontinuierlich erzeugt binzentrierte Töne, Erhalten behält die Koeffizientenphase. Breite 0 ist ein exakter Bypass. Bei sehr kleinen Rauschbreiten wird die Phasenabweichung für einen stetigen Übergang begrenzt. Leistung normieren erhält die gesamte Spektralleistung, im Rauschmodus als Erwartungswert statt als Frame-Limiter. Steuerwerte werden je eingehendem Frame ausgewertet.

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
