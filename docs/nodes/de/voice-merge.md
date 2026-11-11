# Stimmenzusammenführung (Voice Merge)
Führt polyphone Stimmen zu einem Signal pro Kanal zusammen.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Modus (Mode) | Addieren, Mitteln; Standard: Addieren | Editor |
| Stimmen (Voices) | — | Audio; ±∞; Audio |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Audio | Audio; ±∞ |

## Verhalten
Add summiert alle Stimmen; Mean teilt durch die konfigurierte Stimmenzahl, einschließlich stummer oder losgelassener Stimmen. Stereopositionen bleiben erhalten. Ein einzelner Stimmenpfad wird durchgereicht. Effekte nach Merge laufen auf dem gemeinsamen Signal statt je Stimme.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Kein Bypass |
| Einfügen | Stimmen (Voices) → Audio |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/voice-merge.md)
