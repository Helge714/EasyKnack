# Schrittumschalter (Step Switch)
Wählt einen Eingang per Trigger oder numerischem Index.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Datentyp (Data type) | Value, Vector, Wavetable, Buffer, Spectrum, Analysis; Standard: Wert | Editor; Inspektor |
| Schritte (Steps) | 1–32; Standard: 4 | Wert |
| Auswahl (Selection) | Trigger, Index; Standard: Trigger | Editor; Inspektor |
| Weiter (Next) | Button | Trigger; Trigger; Trigger-Modus |
| Zurücksetzen (Reset) | Button | Trigger; Trigger; Trigger-Modus |
| Gewählter Index (Selected index) | 0–31, ganzzahlig; Standard: 0 | Wert; 0–Steps−1, abgerundet; Auto; Index-Modus |
| Eingang 1…N (Input 1…N) | ±∞ für Wert; kein Ressourceneditor; Standard: 0 für Wert | Gewählter Typ; Gewählter Datentyp; Auto |
| Schrittname 1…N (Step name 1…N) | Text; Standard: Eingang 1…N | Editor; Inspektor |
| Eingangsreihenfolge (Input order) | Name, Wert, Nach oben / unten; Standard: Ursprüngliche Slotreihenfolge | Editor; Inspektor |
| Rate | Auto, Audio, Control; Standard: Auto | Editor; Inspektor; Wert/Vector |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Wert / Auswahl (Value / Selected) | Gewählter Typ; Gewählter Eingang |
| Index | Wert; 0–Steps−1 |

## Verhalten
Next schaltet mit Wrap weiter; Reset wählt 0 und hat Vorrang vor gleichzeitigem Next. Im Indexmodus wird abgerundet und begrenzt; die nummerierten Auswahlbuttons sind deaktiviert. Ressourcen-/Vektormodi ersetzen numerische Schritt-/Ausgangstypen durch den gewählten Typ; Ressourcen besitzen keine Ersatzslider. Die Ressourcenauswahl gilt je Block, spätere Trigger wirken auf den nächsten; Index meldet dann die gewählte Blockressource mit Control. Vektorinhalte können Audio behalten. Spektrenwechsel sind hart und ohne Crossfade. Eingangsnamen und gewählter Schritt werden gespeichert. In Instance Groups zeigt die Hervorhebung die erste verschachtelte Instanz; manuelle Auswahl betrifft alle, verbundene Auswahl bleibt unabhängig. Alle angeschlossenen Quellen werden weiterhin berechnet.

Der Inspektor zeigt pro Eingang eine zusammenhängende Control-Gruppe mit bearbeitbarem Namen, numerischem Ersatzwert soweit vorhanden und Buttons zum Verschieben nach oben oder unten. Jeder Eingang behält seinen festen Index, Wert, Namen und seine Verbindungen; Next und Index adressieren weiterhin diese Indizes. Namen zurücksetzen im Node-Kontextmenü stellt den Standardtitel wieder her; Undo stellt den eigenen Titel wieder her.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Eingang 1 (Input 1) → Wert / Auswahl (Value / Selected) |
| Einfügen | Eingang 1 (Input 1) → Wert / Auswahl (Value / Selected) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/step-switch.md)
