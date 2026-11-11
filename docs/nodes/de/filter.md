# Filter (Filter)
Filtert Audio mit resonantem Tiefpass, Hochpass oder Bandpass.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Tiefpass, Hochpass, Bandpass; Standard: Tiefpass | Editor |
| Ordnung (Order) | 2, 4, 6, 8; Standard: 2 | Editor |
| Pegelausgleich (Level compensation) | Aus / An; Standard: Aus | Editor |
| Audioeingang (Audio In) | — | Audio; ±∞; Audio |
| Grenzfrequenz (Cutoff) | 5–20 000 Hz; Standard: 1000 Hz; Hz, kHz | Wert; 5 Hz–0.45 × fs; Audio |
| Resonanz (Resonance) | 0.1–30 Q; Standard: 0.707 Q; Q | Wert; 0.1–30 Q; Audio |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audioausgang (Audio Out) | Audio; ±∞ |

## Verhalten
Kaskadiert Filter zweiter Ordnung zur gewählten Gesamtordnung. Bei Q = 0.707 folgen Tief-/Hochpassstufen einer Butterworth-Anordnung; Q verändert ihre Resonanzen gemeinsam. Grenzfrequenz und Resonanz werden geglättet. Pegelausgleich vergleicht Ein-/Ausgangsleistung über 100 ms und glättet die Verstärkung über 5 ms, kanalweise getrennt und begrenzt auf 0,25–4×. Er erzeugt keine Lookahead-Latenz, ist kein Limiter und stellt entfernte Spektralanteile nicht wieder her. In einer Feedbackschleife verändert der Ausgleich deren Verstärkung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Audioeingang (Audio In) → Audioausgang (Audio Out) |
| Einfügen | Audioeingang (Audio In) → Audioausgang (Audio Out) |
| Sample-Feedback | Unterstützt |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/filter.md)
