# Teiltonanalyse

## Übersicht

Zerlegt ein Spektrum in verfolgte Teiltöne und ein komplexes Restspektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Auflösung (Resolution) | [Editor](../../user/de/reference-tables.md) | 0–20000 Hz | — | 0 Hz | Hz, kHz | — | Immer |
| Schwelle (Threshold) | [Editor](../../user/de/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Immer |
| Minimale Teiltondauer (Minimum track duration) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Immer |
| Maximale Teiltöne (Maximum partials) | [Editor](../../user/de/reference-tables.md) | 1–32768 | — | 64 | — | — | Immer |
| Minimale Frequenz (Minimum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz, kHz | — | Inspektor |
| Maximale Frequenz (Maximum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz, kHz | — | Inspektor |
| Frequenzabweichung (Frequency drift) | [Editor](../../user/de/reference-tables.md) | 0–20000 Hz | — | 0 Hz | Hz, kHz | — | Inspektor |
| Bandbreitenmodell (Bandwidth model) | [Editor](../../user/de/reference-tables.md) | None / Residue / Convergence | — | Convergence | — | — | Inspektor |
| Konvergenztoleranz (Convergence tolerance) | [Editor](../../user/de/reference-tables.md) | 0.001–1 | — | 0.1 | — | — | Inspektor; Convergence |
| Bandbreitenregion (Bandwidth region) | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | Hz, kHz | — | Inspektor; Residue |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Rest (Residual) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |


## Verhalten

Verwendet das Kaiser-Fenster, die spektrale Neuzuordnung, Peakauswahl, auflösungsabhängige Ausdünnung und Teiltonverfolgung von Loris. Die Eingangs-FFT bestimmt die verfügbare Fensterdauer und Framefolge. Auflösung ist der minimale Peakabstand in Hz; 0 wählt `2 × Samplerate / Fenstersamples`. Frequenzabweichung begrenzt die Änderung zwischen aufeinanderfolgenden Trackpunkten; 0 wählt die halbe Auflösung. Die Komponenten benötigen keine gemeinsame Grundfrequenz.

Das Bandbreitenmodell bestimmt die Schätzung des rauschhaften Teiltonanteils:

- **None:** Bandbreite null; die gesamte erkannte Amplitude gilt als tonal.
- **Residue:** ordnet verworfene spektrale Energie innerhalb der Bandbreitenregion zu.
- **Convergence:** verwendet den gemischten Phasenableitungsindikator von Loris; die Konvergenztoleranz bestimmt dessen Sättigungspunkt.

Die ausgegebene Sinusamplitude ist `LorisAmplitude × sqrt(1 − Bandbreite)`. Der [Teilton-Kontext](partial-context.md) liefert `1 − Bandbreite` als Tonalität. Nicht durch diese Sinusschwingungen abgebildete Anteile bleiben im exakten Rest. Das ist ein Sinus-/Rauschmodell, keine Erkennung von Instrumenten, Schlagwerk oder Klangbedeutung.

Die minimale Teiltondauer wirkt kausal: Ein Track wird erst nach dieser Zeit ausgegeben; frühere Frames bleiben unverändert. Maximale Teiltöne begrenzt gleichzeitige Tracks; stärkere akzeptierte Peaks haben Vorrang. Ohne zuordenbaren Peak im nächsten Frame endet ein Track. Änderungen der Auflösung, Dauer oder weiterer Analyseparameter starten das Tracking neu. IDs bleiben innerhalb des laufenden Analyzers eindeutig.

Die originalen, Speicher anfordernden Loris-Schritte laufen auf einem vorbereiteten Hilfsthread. Das Rendering wartet auf jeden Frame; die Rechenzeit zählt somit weiterhin zur Echtzeitfrist. Es gibt keinen Offline-Durchlauf über die gesamte Datei, keine rückwirkende Phasenkorrektur und keine zusätzliche Framewarteschlange. Lange FFT-Fenster und große Kapazitäten benötigen mehr Rechenzeit und Speicher.

Der unveränderte Ausgang von [Teiltöne zu Spektrum](partials-to-spectrum.md) plus Rest rekonstruiert das ursprüngliche komplexe Spektrum einschließlich der Schätzfehler. Das [Restmodell](residual-model.md) ist ein separates verlustbehaftetes Rauschmodell. Kurze Fenster können nahe Komponenten nicht auflösen; keine Einstellung garantiert eine perfekte Trennung zwischen Klang und Rauschen.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Nein | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Teiltöne zu Spektrum](partials-to-spectrum.md) · [Spektrummathematik](spectrum-math.md) · [Teilton-Kontext](partial-context.md) · [Teilton-Ausgang](partial-output.md) · [Teilton-Gruppe](partial-group.md)

[English](../en/partial-analysis.md)
