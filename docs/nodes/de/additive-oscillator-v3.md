# Additiver Oszillator (Additive Oscillator)

## Übersicht

Der **Additive Oscillator** erzeugt einen Klang aus bis zu 144 Teiltönen.

## Eingänge

| Bezeichnung | Datentyp | Reglerbereich | Signalbereich | Standardwert | Einheit | Rate | Sichtbarkeit |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Wellenform (Waveform) | [Wavetable-Ressource](../../user/de/data-types.md#wavetable) | Sinus, Dreieck, Sägezahn, Rechteck | — | Sinus | — | [Control](../../user/de/rates.md#control) | Immer |
| Teiltöne (Partials) | [Wert](../../user/de/data-types.md#value) | 1–144, ganzzahlig | 1–144, abgerundet | 1 | — | [Control](../../user/de/rates.md#control) | Immer |
| Abfall (Falloff) | [Wert](../../user/de/data-types.md#value) | 0–4 | 0–4 | 1 | — | [Auto](../../user/de/rates.md#auto) | Teiltöne > 1 oder Teiltöne verbunden |
| Streckung (Stretch) | [Wert](../../user/de/data-types.md#value) | 0–4 | 0–4 | 1 | — | [Auto](../../user/de/rates.md#auto) | Teiltöne > 1 oder Teiltöne verbunden |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 0,00001–20.000 Hz | 0–20.000 Hz | 440 Hz | [Hz](../../user/de/units.md#frequency), [kHz](../../user/de/units.md#frequency) | [Auto](../../user/de/rates.md#auto) | Immer |
| Phase (Phase) | [Wert](../../user/de/data-types.md#value) | 0–1 turn | ±∞ turn | 0 turn | [turn](../../user/de/units.md#angle), [°](../../user/de/units.md#angle), [rad](../../user/de/units.md#angle) | [Auto](../../user/de/rates.md#auto) | Immer |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md#trigger) | — | — | — | — | [Trigger](../../user/de/rates.md#trigger) | Immer |

## Ausgänge

| Bezeichnung | Datentyp | Wertebereich | Rate | Kanal |
| --- | --- | --- | --- | --- |
| Audioausgang (Audio Out) | [Audio](../../user/de/data-types.md#audio) | ±1 | [Audio](../../user/de/rates.md#audio) | [Auto](../../user/de/data-types.md#channels) |

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

| [Mutable](../../user/de/muting.md) | Eingang | Ausgang |
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
