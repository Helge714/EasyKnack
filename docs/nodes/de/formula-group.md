# Formelgruppe (Formula Group)

## Übersicht

Berechnet eigene Formeln mit konfigurierbarer Gruppenschnittstelle.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| signal | [Wert](../../user/de/data-types.md#value) | ±∞ | ±∞ | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Quelltext (Source) | [Editor](../../user/de/reference-tables.md) | Formeleditor | — | result = signal; | — | — | Inspektor |
| Rate | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor |
| Live-Vorschau (Live preview) | [Editor](../../user/de/reference-tables.md) | Aus / An | — | An | — | — | Inspektor |
| Gruppenschnittstelle (Group IO) | [Editor](../../user/de/reference-tables.md) | Zahlen-, Trigger- und Bufferports | — | signal → result | — | — | Inspektor |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| result | [Wert](../../user/de/data-types.md#value) | ±∞ | [Auto](../../user/de/rates.md#auto) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Group-IO legt Portbeschriftungen, Einheiten und stabile Codebezeichner fest. Die Anfangsschnittstelle ist signal → result. Die Sprache unterstützt typisierte lokale/Zustandsvariablen, Arithmetik, if/else, Casts und Mathematikfunktionen; Triggereingänge sind am Ereignissample boolesch. Audio läuft je Sample, Control je Block mit Prüfung auf den Verlust verwendeter Audio-/Triggereingänge. Zustand ist je Kanal/Stimme/Instanz unabhängig. Eingangsspezifische Lesemethoden unterscheiden aktuelle Signalblöcke von Buffer-Ressourcen. Live-Vorschau übernimmt gültige Änderungen vor Abschluss der nativen Kompilierung; ungültige Entwürfe erhalten das letzte übernommene Programm. In Spectral Groups laufen gültige Formeln je Bin/Frame; Zeit- und Binkontext sind nicht austauschbar. Siehe [Sprachreferenz (englisch)](../../FORMULA-GROUP.md).

Hyperbolische Funktionen: `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`.
`tanh` liefert weiche Sättigung; die Pegelkompensation bleibt Teil der Formel.
`expm1` und `log1p` erhalten die Genauigkeit nahe null. Die Funktionshilfe nennt
Definitionsbereiche und nichtendliche Ergebnisse; Oversampling erfolgt nicht automatisch.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Gewählter Gruppeneingang (Configured group input) | Gewählter Gruppenausgang (Configured group output) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Gewählter Gruppeneingang (Configured group input) | Gewählter Gruppenausgang (Configured group output) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/formula-group.md)
