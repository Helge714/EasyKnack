# Teiltonanalyse

## Übersicht

Zerlegt ein Spektrum in verfolgte Teiltöne und ein komplexes Restspektrum.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Schwelle (Threshold) | [Editor](../../user/de/reference-tables.md) | −160–0 dB | — | −60 dB | dB | — | Immer |
| Minimale Sinusähnlichkeit (Minimum sinusoidality) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0.8 | — | — | Immer |
| Minimale Teiltondauer (Minimum track duration) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 20 ms | ms, s, smpl | — | Immer |
| Maximale Teiltöne (Maximum partials) | [Editor](../../user/de/reference-tables.md) | 1–32768 | — | 64 | — | — | Immer |
| Nur harmonische Teiltöne (Harmonic only) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | Aus | — | — | Immer |
| Grundfrequenz (Fundamental) | [Value](../../user/de/data-types.md#value) | 0–20000 Hz | 0–∞ Hz | 0 Hz | Hz, kHz | [Control](../../user/de/rates.md#control) | Nur harmonische Teiltöne |
| Minimale Frequenz (Minimum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20 Hz | Hz | — | Inspektor |
| Maximale Frequenz (Maximum frequency) | [Editor](../../user/de/reference-tables.md) | 0–∞ Hz | — | 20000 Hz | Hz | — | Inspektor |
| Zuordnungstoleranz (Tracking tolerance) | [Editor](../../user/de/reference-tables.md) | 0–2400 cent | — | 100 cent | cent | — | Inspektor |
| Peak-Abhebung (Peak prominence) | [Editor](../../user/de/reference-tables.md) | 0–120 dB | — | 3 dB | dB | — | Inspektor |
| Bewertungsdauer (Assessment time) | [Editor](../../user/de/reference-tables.md) | 0–2000 ms | — | 30 ms | ms, s, smpl | — | Inspektor |
| Minimale Tonalität (Minimum tonality) | [Editor](../../user/de/reference-tables.md) | 0–1 | — | 0.7 | — | — | Inspektor |
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

Lokale FFT-Peaks werden zwischen den Bins interpoliert. Die Sinusähnlichkeit ist die normierte quadratische komplexe Übereinstimmung mit einer sinusförmigen Fensterkeule; Peak-Abhebung vergleicht die Spitze mit dem Median der Umgebung außerhalb der Hauptkeule. Nur geeignete Kandidaten werden verfolgt. Die Zuordnung verwendet vorhergesagte Frequenz, Amplitudenänderung und Phasenkontinuität.

Bewertungsdauer ist die Zeitkonstante einer exponentiellen Glättung der Tonalitätsbewertung, keine zusätzliche FFT-Größe oder rechteckige Mittelung. Die minimale Teiltondauer muss vor der ersten Übernahme erreicht sein. Tracking-Lücke erlaubt fehlende Beobachtungen über die nächste erwartete Frame-Position hinaus; während einer Lücke wird kein Teilton erfunden. Neue oder nicht bestätigte Anteile bleiben vollständig im Residual. Tonalität ist ein Modellscore, keine kalibrierte Rauschwahrscheinlichkeit.

Nur harmonische Teiltöne beschränkt die Auswahl auf ganzzahlige Vielfache der Grundfrequenz innerhalb der Cent-Toleranz. 0 Hz wählt eine monophone spektrale Schätzung aus den Peak-Abständen, positive oder verbundene Werte geben die Grundfrequenz vor. Die Automatik ist keine polyphone Tonhöhenerkennung und kann Oktavfehler machen. Für inharmonische Klänge den Modus deaktivieren.

Maximale Teiltöne ist die vorbereitete Tracking-Kapazität einschließlich noch nicht bestätigter und kurz fehlender Spuren. Bei voller Kapazität bleiben weitere Kandidaten im Residual. Gruppen, Synthese, verschachtelte Instanzen und Exporte übernehmen das größte benötigte Budget während der Vorbereitung. Der Audiothread allokiert dafür keinen Speicher. Hohe Kapazitäten erhöhen den Speicherbedarf pro Kanal, Stimme und Gruppenprozessor.

Übernommene Fensterkeulen werden komplex vom Original abgezogen. Unveränderte Teiltöne plus exaktes Residual rekonstruieren weiterhin das Ausgangsspektrum. [Residual Model](residual-model.md) ist eine separate, verlustbehaftete stochastische Modellierung; das Analyse-Residual selbst wird nicht entrauscht oder geglättet. FFT-Fenstergröße und Auflösung bleiben entscheidend. Größere Bewertungs- und Mindestzeiten verzögern die Erkennung, puffern aber keine vergangenen Frames nachträglich.

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
