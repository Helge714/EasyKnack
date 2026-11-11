# Pegeldetektor (Level Detector)
Misst Audio-Spitzenwert und gleitenden RMS-Pegel.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Audio-Eingang (Audio In) | — | Audio; ±∞; Audio |
| Kanal (Channel) | Links / Rechts / Mix; Standard: Links | Editor |
| RMS-Fenster (RMS window) | 0.1–2000 ms; Standard: 50 ms; s, ms, smp | Editor |
| Glättung (Smoothing) | Aus / Ein; Standard: Aus | Editor |
| Anstieg (Attack) | 0–2000 ms; Standard: 10 ms; s, ms, smp | Editor; Immer; aktiv bei Glättung |
| Abfall (Release) | 0–2000 ms; Standard: 100 ms; s, ms, smp | Editor; Immer; aktiv bei Glättung |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Spitzenwert (Peak) | Wert; ≥ 0; Control |
| RMS | Wert; ≥ 0; Control |

## Verhalten
Spitzenwert = max(abs(x)) des aktuellen Blocks; RMS = sqrt(mean(x²)) über das zurückliegende Fenster. Zu Beginn verwendet RMS nur eingetroffene Samples. Polyphone Stimmen werden zuerst summiert; Mix = (L+R)/2. Optionale getrennte Anstiegs-/Abfallglättung folgt beiden Messungen; eine Zeitkonstante erreicht etwa 63% eines Sprungs. Zeit null reagiert sofort. Ausgangseinheiten sind Faktor, % oder dB; das RMS eines voll ausgesteuerten Sinus ist ungefähr 0.707. Es gibt keine Frequenzgewichtung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/level-detector.md)
