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
- Operatoren: `+`, `-`, `*`, `/`, `%`, unäres `+` / `-` / `!`, `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`; Klammern bestimmen die Reihenfolge. Keine Pointer, Rekursion, while-Schleifen oder beliebigen C++-Aufrufe.
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

Der Editor unterscheidet Übernommen, Noch nicht übernommen und Ungültiger Entwurf, getrennt vom Stand der nativen Optimierung. Ein ungültiger Entwurf erhält das letzte übernommene Programm und zeigt ein rechtsbündiges rotes Badge mit kontrastierendem ! in einem reservierten Titelbereich am zugehörigen Formula-Node; Hover zeigt den Fehler. Funktionsnamen, Variablen, Parameter und schreibgeschützte Eingänge verwenden die jeweiligen semantischen Theme-Farben. Lange Titel werden vor der Warnmarkierung gekürzt; das ! bleibt sichtbar. Backspace öffnet keine Vervollständigung. Format verwendet `name(...)` bei Aufrufen, `name(...) {` bei Definitionen und `if (...)` / `for (...)` bei Kontrollanweisungen, mit einem Leerzeichen nach Argumentkommas. Kommentare bleiben unverändert. Vorläufige und dauerhafte Parameterhinweise werden pro Argument zusammengeführt.

Strg+Linksklick (oder Zur Definition) springt zur Deklaration einer eigenen Funktion, lokalen Variablen oder eines Parameters im selben Formula-Dokument, auch bei vorwärts referenzierten Funktionen. Eingebaute Funktionen öffnen diese Referenz direkt beim Funktionseintrag, in der unabhängig eingestellten Dokusprache. Leere Editorbereiche zeigen keinen Komponentennamen als Tooltip.

### Eigene Funktionen und begrenzte Schleifen

Numerische/boolesche Funktionen besitzen typisierte Parameter und einen Rückgabetyp und dürfen vor oder nach ihrem Aufruf stehen. Jeder Ausführungspfad muss einen Wert zurückgeben. Funktionen haben lokale Variablen und Parameter, dürfen Eingänge/Kontext lesen und andere Funktionen aufrufen. Sie dürfen keine Ausgangsports beschreiben, Aufrufer-Lokale/Zustände übernehmen, persistenten Zustand deklarieren oder rekursiv aufrufen. Höchstens 16 Parameter sind erlaubt. Ausgänge werden mit dem Rückgabewert gesetzt.

```cpp
float shape(float x, float amount) {
    return tanh(x * amount);
}

float sum = 0;
for (int i = 0; i < 4; i++) {
    sum += shape(signal, i + 1);
}
result = sum / 4.0;
```

`for` benötigt einen neuen `int`-Zähler, feste ganzzahlige Literale für Anfang/Grenze/Schritt, `<`, `<=`, `>` oder `>=` und `++`, `--`, `+=` oder `-=`. Der Schritt muss zur Grenze führen; im Rumpf ist der Zähler schreibgeschützt. Jede Schleife erlaubt höchstens 1.024 Durchläufe. Schleifen und Aufrufe werden beim Kompilieren eingebettet; verschachtelte Erweiterungen unterliegen weiter den Gesamtgrenzen für Anweisungen, Register und Quelltext. `break` und `continue` sind nicht verfügbar. Eine Schleife mit null Durchläufen setzt ihre Ausgänge nicht. Geschweifte Klammern um if/else- und Schleifenrümpfe sind optional.

Zuweisungen und Funktionsargumente dürfen `double`-Ergebnisse implizit nach `float` umwandeln; Float-Variablen benötigen deshalb nach Rechnungen mit Dezimalliteralen keine erneuten Casts. Float-zu-Ganzzahl und verengende Ganzzahlkonvertierungen erfordern für Lokale, Zustand und Funktionsargumente weiterhin Casts. Bedingungen benötigen bool; dafür einen Vergleich oder expliziten bool-Cast verwenden. Bestehende Sprachversionen bleiben lesbar; Funktions-/Schleifensyntax wählt die typisierte Ausführung ohne Änderung des Dokumentformats.

Vervollständigung erscheint beim Tippen von Bezeichnern oder mit Strg+Leertaste. Hover, Signaturhilfe und eingeblendete Parameternamen unterstützen auch eigene Funktionen. Enter und Format berücksichtigen if/else ohne geschweifte Klammern.

### Schreibgeschützte Variablen

`final float gain = signal * 2;` berechnet bei jeder Auswertung einen neuen Wert (Audio: pro Sample; Control: pro Block) und verhindert weitere Zuweisungen innerhalb dieser Auswertung. Auch Funktionsparameter dürfen final sein.

`const float MAX_SPEED = 2;` definiert einen eingangsunabhängigen konstanten Ausdruck: erlaubt sind Literale, vorherige const-Variablen und reine eingebaute Mathematikfunktionen. Eingänge, Kontext, state, final-Lokale, eigene Funktionen, `rand` und `sineWave` sind im Initialisierer nicht erlaubt. Const-Parameter werden nicht unterstützt; dafür final verwenden. `const` ist bereits schreibgeschützt: nicht mit `final` kombinieren. `state` bleibt dauerhaft gespeichert und veränderbar; beide Zusätze sind mit state unzulässig.

Final- und Const-Variablennamen erscheinen fett in derselben semantischen Farbe, schreibgeschützte Eingänge und Kontextvariablen ebenfalls fett, state-Namen kursiv. Das ist reine Darstellung. Die Zusätze verhindern Fehler, garantieren aber keine schnellere Verarbeitung.

### Eingebaute Funktionen

In typisierten Formeln verwenden Mathematikfunktionen den gemeinsamen Argumenttyp; ganzzahlige Argumente rechnen in double, außer bei `abs`, `sign`, `min`, `max` und `clamp`, die den gemeinsamen Ganzzahltyp erhalten. `sineWave`, `rand` und Buffer-Lesen liefern float. Alte Float-Formeln behalten ihre Float-Arithmetik.

### abs

`abs(value)` liefert den Betrag. Die kleinste vorzeichenbehaftete Ganzzahl bleibt unverändert, weil ihr positiver Gegenwert nicht darstellbar ist.

### sign

`sign(value)` liefert je nach Vorzeichen -1, 0 oder 1.

### sqrt

`sqrt(value)` liefert die Quadratwurzel; negative Eingaben ergeben NaN.

### sin

`sin(angle)` liefert den Sinus im Bogenmaß. Beispiel: `sin(pi / 2)` = 1.

### cos

`cos(angle)` liefert den Kosinus im Bogenmaß. Beispiel: `cos(0)` = 1.

### tan

`tan(angle)` liefert den Tangens im Bogenmaß; Polstellen bei pi/2 plus ganzzahligen Vielfachen von pi vermeiden.

### asin

`asin(value)` liefert den Arkussinus im Bogenmaß; Eingabebereich -1 bis 1.

### acos

`acos(value)` liefert den Arkuskosinus im Bogenmaß; Eingabebereich -1 bis 1.

### atan

`atan(value)` liefert den Arkustangens im Bogenmaß.

### atan2

`atan2(y, x)` liefert die Richtung von (x, y) im Bogenmaß unter Berücksichtigung des Quadranten.

### sinh

`sinh(value)` liefert den hyperbolischen Sinus; große Beträge können überlaufen.

### cosh

`cosh(value)` liefert den hyperbolischen Kosinus, bei reellen Eingaben mindestens 1; große Beträge können überlaufen.

### tanh

`tanh(value)` sättigt weich gegen -1 und 1; Pegelausgleich und Oversampling erfolgen nicht automatisch.

### asinh

`asinh(value)` liefert den inversen hyperbolischen Sinus für beliebige reelle Eingaben.

### acosh

`acosh(value)` liefert den inversen hyperbolischen Kosinus; Eingaben unter 1 ergeben NaN.

### atanh

`atanh(value)` liefert den inversen hyperbolischen Tangens; endlich für -1 < value < 1, unendlich bei ±1, außerhalb NaN.

### exp

`exp(value)` liefert e hoch value; große positive Eingaben können überlaufen.

### expm1

`expm1(value)` berechnet exp(value) - 1 nahe null mit höherer Genauigkeit.

### log

`log(value)` liefert den natürlichen Logarithmus. Positive Eingaben ergeben endliche Werte, null negative Unendlichkeit, negative Eingaben NaN.

### log2

`log2(value)` liefert den Logarithmus zur Basis 2; für endliche Ergebnisse muss die Eingabe positiv sein.

### log10

`log10(value)` liefert den Logarithmus zur Basis 10; für endliche Ergebnisse muss die Eingabe positiv sein.

### log1p

`log1p(value)` berechnet log(1 + value) nahe null genau; -1 ergibt negative Unendlichkeit, kleinere Werte NaN.

### floor

`floor(value)` rundet gegen negative Unendlichkeit ab; `floor(-0.2)` = -1.

### ceil

`ceil(value)` rundet gegen positive Unendlichkeit auf; `ceil(-0.2)` = 0.

### round

`round(value)` rundet zum nächsten ganzzahligen Wert, bei halben Werten von null weg. Das Ergebnis bleibt eine Gleitkommazahl.

### pow

`pow(base, exponent)` berechnet base hoch exponent; nicht reell darstellbare Ergebnisse ergeben NaN.

### min

`min(a, b)` liefert den kleineren Wert.

### max

`max(a, b)` liefert den größeren Wert.

### clamp

`clamp(value, minimum, maximum)` begrenzt auf den angegebenen Bereich; minimum darf maximum nicht überschreiten.

### mix

`mix(a, b, factor)` berechnet a + (b - a) * factor. factor wird nicht begrenzt; 0 liefert a, 1 liefert b.

### sineWave

`sineWave(phase)` liefert float-Sinus aus einer gemeinsamen 2048-Punkte-Tabelle mit linearer Interpolation. phase zählt Perioden, wird automatisch umgebrochen und ergibt bei nicht endlicher Eingabe null; `sineWave(0.25)` = 1.

### rand

`rand(minimum, maximum[, seed])` liefert pro Auswertung einen reproduzierbaren, gleichverteilten float-Zufallswert. Der optionale Seed wählt die Folge, sonst gilt der Node-Seed. Vertauschte Grenzen werden getauscht.

### buffer-read

`buffer.read(position[, mode[, interpolation]])` liefert eine float-Probe an einer absoluten Sampleposition. Modi: Clamp (Standard), Wrap, Zero; Interpolation: Linear (Standard), Nearest. Leere Buffer liefern null. Alte boolesche Argumente der Sprachversion 1 behalten die physische Indexsemantik.

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
