# Ratenkonverter (Rate Converter)
Wandelt numerische Signale zwischen Audio und Control um.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Eingang (In) | ±∞; Standard: 0 | Wert; ±∞; Audio |
| Ausgangsrate (Output Rate) | Control, Audio; Standard: Control | Editor |
| Reduktion (Reduction) | Erstes, Letztes, Mittelwert, RMS, Minimum, Maximum, Absolutspitze; Standard: Erstes | Editor; Control-Ausgang |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Ausgang (Out) | Wert; ±∞; Audio / Control |

## Verhalten
Audio erhält die Samples. Control reduziert jeden tatsächlichen Block mit der gewählten Methode und hält das Ergebnis. Mean ist vorzeichenbehaftet; RMS und Absolutspitze sind nichtnegativ. Beim Hochsetzen wird Control gehalten; es entstehen keine Zwischenwerte. Trigger werden mit eigenen Nodes konvertiert.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang (In) → Ausgang (Out) |
| Einfügen | Eingang (In) → Ausgang (Out) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/rate-converter.md)
