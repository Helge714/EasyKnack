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

Für einen Buffer-Eingang in Group-IO den Datentyp **Buffer-Ressource** wählen. Er wurde zuvor als **HISTORY** angezeigt. Buffer-Ressourcen werden nur als Eingänge unterstützt.

Group-IO legt Portbeschriftungen, Einheiten und stabile Codebezeichner fest. Die Anfangsschnittstelle ist signal → result. Die Sprache unterstützt typisierte lokale/Zustandsvariablen, Arithmetik, if/else, Casts und Mathematikfunktionen; Triggereingänge sind am Ereignissample boolesch. Audio läuft je Sample, Control je Block mit Prüfung auf den Verlust verwendeter Audio-/Triggereingänge. Zustand ist je Kanal/Stimme/Instanz unabhängig. Eingangsspezifische Lesemethoden unterscheiden aktuelle Signalblöcke von Buffer-Ressourcen. Live-Vorschau übernimmt gültige Änderungen vor Abschluss der nativen Kompilierung; ungültige Entwürfe erhalten das letzte übernommene Programm. In Spectral Groups laufen gültige Formeln je Bin/Frame; Zeit- und Binkontext sind nicht austauschbar. Siehe [Formel-Anleitung](../../user/de/formula-group.md).

Hyperbolische Funktionen: `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`.
`tanh` liefert weiche Sättigung; die Pegelkompensation bleibt Teil der Formel.
`expm1` und `log1p` erhalten die Genauigkeit nahe null. Die Funktionshilfe nennt
Definitionsbereiche und nichtendliche Ergebnisse; Oversampling erfolgt nicht automatisch.


Beim Öffnen während der laufenden Hintergrundvorbereitung erscheint eine Ladeanzeige; der Formeleditor wird anschließend automatisch angezeigt.

### Sprache

- Typen: `bool`, `int`, `uint` (`unsigned int`), `long`, `ulong` (`unsigned long`), `float`, `double`; Casts wie `(double) value`.
- Anweisungen: lokale Deklarationen, `state` Deklarationen vor anderen Anweisungen, Zuweisungen, `+=`, `-=`, `*=`, `/=`, `if` / `else`, Blöcke `{ }`, Kommentare `//`.
- Operatoren: `+`, `-`, `*`, `/`, `%`, unäres `+` / `-` / `!`, `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`; Klammern bestimmen die Reihenfolge. Keine Schleifen, Pointer oder beliebigen C++-Aufrufe.
- Konstanten: `true`, `false`, `pi`, `tau`, `Audio`, `Control`, `Clamp`, `Wrap`, `Zero`, `Linear`, `Nearest`.
- Kontext: `sampleRate` (Hz), `blockSize` (Samples), `sampleIndex` (innerhalb des Blocks), `samplePosition` (absolute Sampleanzahl). Vergangene Sekunden: `samplePosition / sampleRate`.
- Numerische Eingänge: `.value`, `.rate`; Triggereingänge: `.active`.
- Buffer-Eingänge: `.size`, `.begin`, `.end`, `.writePosition`, `.writepos`, `.sampleRate`, `.read(position[, mode[, interpolation]])`. Positionen sind absolute Samplepositionen; `writepos` ist ein Alias von `writePosition`. Der normierte 0–1-Ausgang des Buffer-Nodes ist davon unabhängig. Dateibuffer halten die Schreibposition bei `.end`. Lesevorgaben: `Clamp`, `Linear`; alternativ `Wrap` oder `Zero` und `Nearest`. Alte Version-1-Lesezugriffe behalten physische Indizes.

| Funktionen | Argumente |
| --- | --- |
| `abs`, `sign`, `sqrt`, `exp`, `expm1`, `log`, `log2`, `log10`, `log1p`, `floor`, `ceil`, `round` | `(value)` |
| `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh` | `(value)`; Winkel in Radiant |
| `sineWave` | `(phase)`; Phase in Perioden |
| `atan2` | `(y, x)` |
| `pow` | `(base, exponent)` |
| `min`, `max` | `(a, b)` |
| `clamp` | `(value, minimum, maximum)` |
| `mix` | `(a, b, factor)` |
| `rand` | `(minimum, maximum[, seed])` |

Vervollständigung, Hover- und Signaturhilfe erklären Definitionsbereiche und Argumentdetails.

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
