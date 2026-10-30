# Additiver Oszillator (Additive Oscillator)

## Übersicht

Der **Additive Oscillator** erzeugt einen Klang aus bis zu 144 Teiltönen.

## Eingänge

| Bezeichnung | Datentyp | Wertebereich (mit Einheit) | Standardwert (mit Einheit) | Erlaubte Einheiten | Rate | Sichtbarkeit |
| --- | --- | --- | --- | --- | --- | --- |
| Wellenform (Waveform) | Wavetable | Wavetable-Ressource; Auswahl: Sinus, Dreieck, Sägezahn, Rechteck | Sinus | — | Control | Immer |
| Teiltöne (Partials) | Wert, ganzzahlig | 1–144 | 1 | — | Control | Immer |
| Abfall (Falloff) | Wert | 0–4 | 1 | — | Audio / Control | Teiltöne > 1 oder Teiltöne verbunden |
| Streckung (Stretch) | Wert | 0–4 | 1 | — | Audio / Control | Teiltöne > 1 oder Teiltöne verbunden |
| Frequenz (Frequency) | Wert | Regler: 0,00001–20.000 Hz; Signal: 0–20.000 Hz | 440 Hz | Hz, kHz | Audio / Control | Immer |
| Phase (Phase) | Wert | Regler: 0–1 turn; Signal: beliebiger Phasenversatz | 0 turn | turn, °, rad | Audio / Control | Immer |
| Zurücksetzen (Reset) | Trigger | — | — | — | Samplegenau | Immer |

## Ausgänge

| Bezeichnung | Datentyp | Wertebereich (mit Einheit) | Rate | Kanal |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | Audiosignal | −1 bis +1 bei Standardwellenformen; bei eigener Wavetable ressourcenabhängig | Audio | Mono / Stereo (eingangsabhängig) |

## Verhalten

### Teiltöne = 1

Entspricht dem normalen Oszillator.

### Teiltöne > 1

Im eingeschwungenen Zustand gilt:

```text
I = { n ∈ {1, …, N} : f · n^p < fs / 2 }

θn[k + 1] = wrap(θn[k] + f[k] · n^p / fs)

y[k] = Σ(n ∈ I) n^(−a) · W(θn[k] + φ[k] · n^p)
       ─────────────────────────────────────────
                  Σ(n ∈ I) n^(−a)
```

`N`: Teiltöne einschließlich Grundton; `f`: Frequenz in Hz; `fs`: Sample-Rate
in Hz; `p`: Stretch; `a`: Falloff; `φ`: Phase in Umdrehungen; `W`: gewählte
periodische Wellenform; `wrap(x) = x − floor(x)`. Für eine leere Menge `I` ist
`y[k] = 0`.

- **Stretch** bestimmt die Teiltonfrequenzen über `f · n^p`; bei `p = 1` ergibt
  sich die harmonische Reihe.
- **Falloff** bestimmt die Teiltongewichte über `n^(−a)`. Die Summe wird durch
  die berücksichtigte Gewichtssumme normiert.
- Frequenzverhältnisse und Gewichte werden bei Änderungen intern geglättet.

Die übrigen Funktionen entsprechen dem normalen Oszillator.

## Mute

| Mutable | Eingang | Ausgang |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| Eingang | Ausgang |
| --- | --- |
| — | — |

## Delayable

| Sampleweises Feedback |
| --- |
| Unterstützt |
