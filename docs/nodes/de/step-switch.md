# Schrittumschalter (Step Switch)

## Übersicht

Wählt einen Eingang per Trigger oder numerischem Index.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Datentyp (Data type) | [Editor](../../user/de/reference-tables.md) | Value, Vector, Wavetable, Buffer, Spectrum, Analysis | — | Wert | — | — | Inspektor |
| Schritte (Steps) | [Wert](../../user/de/data-types.md) | 1–32 | — | 4 | — | — | Immer |
| Auswahl (Selection) | [Editor](../../user/de/reference-tables.md) | Trigger, Index | — | Trigger | — | — | Inspektor |
| Weiter (Next) | [Trigger](../../user/de/data-types.md) | Button | — | — | — | [Trigger](../../user/de/rates.md) | Trigger-Modus |
| Zurücksetzen (Reset) | [Trigger](../../user/de/data-types.md) | Button | — | — | — | [Trigger](../../user/de/rates.md) | Trigger-Modus |
| Gewählter Index (Selected index) | [Wert](../../user/de/data-types.md) | 0–31, ganzzahlig | 0–Steps−1, abgerundet | 0 | — | [Auto](../../user/de/rates.md) | Index-Modus |
| Eingang 1…N (Input 1…N) | [Gewählter Typ](../../user/de/data-types.md) | ±∞ für Wert; kein Ressourceneditor | Gewählter Datentyp | 0 für Wert | — | [Auto](../../user/de/rates.md) | Immer |
| Schrittname 1…N (Step name 1…N) | [Editor](../../user/de/reference-tables.md) | Text | — | Eingang 1…N | — | — | Inspektor |
| Rate (Rate) | [Editor](../../user/de/reference-tables.md) | Auto, Audio, Control | — | Auto | — | — | Inspektor; Wert/Vector |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Wert / Auswahl (Value / Selected) | [Gewählter Typ](../../user/de/data-types.md) | Gewählter Eingang | [Auto](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Index | [Wert](../../user/de/data-types.md) | 0–Steps−1 | [Auto](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

Next schaltet mit Wrap weiter; Reset wählt 0 und hat Vorrang vor gleichzeitigem Next. Im Indexmodus wird abgerundet und begrenzt; die nummerierten Auswahlbuttons sind deaktiviert. Ressourcen-/Vektormodi ersetzen numerische Schritt-/Ausgangstypen durch den gewählten Typ; Ressourcen besitzen keine Ersatzslider. Die Ressourcenauswahl gilt je Block, spätere Trigger wirken auf den nächsten; Index meldet dann die gewählte Blockressource mit Control. Vektorinhalte können Audio behalten. Spektrenwechsel sind hart und ohne Crossfade. Eingangsnamen und gewählter Schritt werden gespeichert. In Instance Groups zeigt die Hervorhebung die erste verschachtelte Instanz; manuelle Auswahl betrifft alle, verbundene Auswahl bleibt unabhängig. Alle angeschlossenen Quellen werden weiterhin berechnet.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | Eingang 1 (Input 1) | Wert / Auswahl (Value / Selected) |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| Eingang 1 (Input 1) | Wert / Auswahl (Value / Selected) |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/step-switch.md)
