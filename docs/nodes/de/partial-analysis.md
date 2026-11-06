# Teiltonanalyse

## Übersicht

Zerlegt ein Spektrum in verfolgte Teiltöne und ein komplexes Restspektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Schwelle (Threshold) | [Editor](../../user/de/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Immer |
| Minimaler Peak-SNR (Minimum peak SNR) | [Editor](../../user/de/reference-tables.md) | 5–60 dB | — | 30 dB | dB | — | Peaks klassifizieren |
| Minimale Teiltondauer (Minimum track duration) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Immer |
| Maximale Teiltöne (Maximum partials) | [Editor](../../user/de/reference-tables.md) | 1–32768 | — | 64 | — | — | Immer |
| Nur harmonische Teiltöne (Harmonic only) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Peaks klassifizieren (Classify peaks) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Inspektor |
| Grundfrequenz (Fundamental) | [Value](../../user/de/data-types.md#value) | 0–20000 Hz | 0–∞ Hz | 0 Hz | Hz, kHz | [Control](../../user/de/rates.md#control) | Nur harmonische Teiltöne |
| Minimale Frequenz (Minimum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz | — | Inspektor |
| Maximale Frequenz (Maximum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz | — | Inspektor |
| Zuordnungstoleranz (Tracking tolerance) | [Editor](../../user/de/reference-tables.md) | 0–2400 cent | — | 100 cent | cent | — | Inspektor |
| Tracking-Lücke (Tracking gap) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Inspektor |
| Harmonische Toleranz (Harmonic tolerance) | [Editor](../../user/de/reference-tables.md) | 0–200 cent | — | 30 cent | cent | — | Inspektor; Nur harmonische Teiltöne |
| Minimale Grundfrequenz (Minimum fundamental) | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 40 Hz | Hz, kHz | — | Inspektor; Nur harmonische Teiltöne |
| Maximale Grundfrequenz (Maximum fundamental) | [Editor](../../user/de/reference-tables.md) | 1–20000 Hz | — | 2000 Hz | Hz, kHz | — | Inspektor; Nur harmonische Teiltöne |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Teiltöne (Partials) | [Partials](../../user/de/data-types.md#partials) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |
| Rest (Residual) | [Spectrum](../../user/de/data-types.md#spectrum) | — | [Control](../../user/de/rates.md#control) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Lokale FFT-Peaks werden zwischen den Bins interpoliert. Bei aktivierter Option Peaks klassifizieren werden Kandidaten anhand normierter Bandbreite, normierter Dauer und Frequenzkohärenz mit fensterabhängigen Grenzen verglichen. Minimaler Peak-SNR wählt den angenommenen Peak-Rausch-Abstand des Modells: Höhere Werte verengen den Entscheidungsbereich und verwerfen mehr Kandidaten. Es handelt sich nicht um einen gemessenen Ausgangs-SNR. Ohne Klassifikation entscheiden nur Amplitudenschwelle und Tracking über die Auswahl.

Die minimale Teiltondauer muss vor der ersten Übernahme erreicht sein. Tracking-Lücke erlaubt fehlende Beobachtungen über die nächste erwartete Frame-Position hinaus; während einer Lücke wird kein Teilton erfunden. Neue oder nicht bestätigte Anteile bleiben im Residual. Die Zuordnung verwendet vorhergesagte Frequenz, Amplitudenänderung und Phasenkontinuität am Fenstermittelpunkt. Akzeptierte Kandidaten werden nach absteigender Amplitude angepasst, um den Einfluss stärkerer Nachbarn zu verringern.

Der Klassifikator unterstützt Hann-, Hamming- und Blackman-Fenster. Rechteckfenster werden intern für die Klassifikation mit Hann erneut analysiert; Originalspektrum und Residualkonvention bleiben erhalten. Kurze Fenster lösen nahe Komponenten nicht auf, lange Fenster können Modulation als getrennte Seitenbänder darstellen. Keine Einstellung garantiert eine vollständige Trennung von Klang und Rauschen.

Nur harmonische Teiltöne beschränkt die Auswahl auf ganzzahlige Vielfache der Grundfrequenz innerhalb der Cent-Toleranz. 0 Hz wählt eine monophone spektrale Schätzung aus den Peak-Abständen, positive oder verbundene Werte geben die Grundfrequenz vor. Die Automatik ist keine polyphone Tonhöhenerkennung und kann Oktavfehler machen. Für inharmonische Klänge den Modus deaktivieren.

Maximale Teiltöne ist die vorbereitete Tracking-Kapazität einschließlich noch nicht bestätigter und kurz fehlender Spuren. Bei voller Kapazität bleiben weitere Kandidaten im Residual. Gruppen, Synthese, verschachtelte Instanzen und Exporte übernehmen das größte benötigte Budget während der Vorbereitung. Der Audiothread allokiert dafür keinen Speicher. Hohe Kapazitäten erhöhen den Speicherbedarf pro Kanal, Stimme und Gruppenprozessor.

Übernommene Fensterkeulen werden komplex vom Original abgezogen. Unveränderte Teiltöne plus exaktes Residual rekonstruieren weiterhin das Ausgangsspektrum. [Residual Model](residual-model.md) ist eine separate, verlustbehaftete stochastische Modellierung; das Analyse-Residual selbst wird nicht entrauscht oder geglättet. FFT-Fenstergröße und Auflösung bleiben entscheidend. Größere Mindestzeiten verzögern die Erkennung, puffern aber keine vergangenen Frames nachträglich.

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
