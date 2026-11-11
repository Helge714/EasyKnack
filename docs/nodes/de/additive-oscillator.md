# Additiver Oszillator (Additive Oscillator)
Der **Additive Oscillator** erzeugt einen Klang aus bis zu 144 Teiltönen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Wellenform (Waveform) | Sinus, Dreieck, Sägezahn, Rechteck; Standard: Sinus | Wavetable-Ressource; Control |
| Teiltöne (Partials) | 1–144, ganzzahlig; Standard: 1 | Wert; 1–144, abgerundet; Control |
| Abfall (Falloff) | 0–4; Standard: 1 | Wert; 0–4; Auto; Teiltöne > 1 oder Teiltöne verbunden |
| Streckung (Stretch) | 0–4; Standard: 1 | Wert; 0–4; Auto; Teiltöne > 1 oder Teiltöne verbunden |
| Frequenz (Frequency) | 0,00001–20.000 Hz; Standard: 440 Hz; Hz, kHz | Wert; 0–20.000 Hz; Auto |
| Phase | 0–1 turn; Standard: 0 turn; turn, °, rad | Wert; ±∞ turn; Auto |
| Zurücksetzen (Reset) | — | Trigger; Trigger |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±1 |

## Verhalten
### Teiltöne = 1

Entspricht dem normalen [Oszillator](oscillator.md).

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

Die übrigen Funktionen entsprechen dem normalen [Oszillator](oscillator.md).

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/additive-oscillator.md)
