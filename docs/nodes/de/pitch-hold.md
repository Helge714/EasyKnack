# Tonhöhe halten (Pitch Hold)
Experimentell: Zum Hinzufügen dieses Nodes und zum Anzeigen abhängiger Beispiele im Beispiele-Menü **Dokumenteinstellungen → Experimentelle Funktionen aktivieren** einschalten. Tonhöhenerkennung und Übernahme funktionieren noch nicht in allen geprüften Fällen zuverlässig. Vorhandene Projekte behalten ihre Nodes auch bei ausgeschalteter Option.

Hält die letzte ausreichend sichere Frequenz.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Frequenz (Frequency) | 0–20000 Hz; Standard: 440 Hz; Hz, kHz | Wert; ≥ 0 Hz; Auto |
| Konfidenz (Confidence) | 0–1; Standard: 1 | Wert; ±∞; Auto |
| Minimale Konfidenz (Minimum confidence) | 0–1; Standard: 0.8 | Wert; ±∞; Auto |
| Rate | Auto / Audio / Control; Standard: Auto | Editor; Inspektor |
| Anfangsfrequenz (Initial frequency) | 1–20000 Hz; Standard: 440 Hz; Hz, kHz | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Frequenz [Hz] (Frequency [Hz]) | Wert; > 0 Hz |
| Gültig (Valid) | Wert; 0–1 |

## Verhalten
Akzeptiert positive endliche Frequenz bei Konfidenz ≥ minimale Konfidenz. Sonst bleibt der zuletzt akzeptierte Wert stehen; davor gilt Anfangsfrequenz. Gültig beschreibt den aktuellen Eingang, auch während der Ausgang hält. Es gibt keine automatische Überblendung oder Glättung.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Frequenz (Frequency) → Frequenz [Hz] (Frequency [Hz]) |
| Einfügen | Frequenz (Frequency) → Frequenz [Hz] (Frequency [Hz]) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/pitch-hold.md)
