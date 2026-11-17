# Formula-Sprachreferenz

Die Formula-Sprache ist eine begrenzte, C++-ähnliche Sprache für numerische Signalverarbeitung, keine vollständige C++- oder Java-Laufzeit. Dieses Dokument erklärt die Sprache. Ports, Rate und Editorbedienung stehen beim [Formula-Group-Node](../../nodes/de/formula-group.md) und im [Arbeitsablauf](formula-group.md).

[Variablen](#variablen) · [state](#state) · [final](#final) · [const](#const) · [Datentypen](#datentypen) · [Ausdrücke](#ausdrucke) · [Kontext](#kontext) · [Buffer](#buffer) · [Funktionen und Schleifen](#eigene-funktionen-und-begrenzte-schleifen) · [Eingebaute Funktionen](#eingebaute-funktionen)

## Auswertung

Anweisungen laufen in Quelltextreihenfolge. Audio wertet pro Sample aus, Control pro Block. Auto wählt die zulässige Rate anhand der verwendeten Eingänge. In Spectral Groups bestimmt die umgebende Gruppe die Bin-/Frame-Auswertung; Samplezeit ist kein Binindex.

Eingänge werden über ihre eingestellten Codebezeichner gelesen, Ausgänge durch Zuweisung gesetzt. Numerische Eingänge sind float, Trigger bool und Buffer Ressourcen. Eingänge sind schreibgeschützt. Jeder numerische Ausgang muss auf jedem Ausführungspfad gesetzt werden. Trigger-Ausgänge beginnen pro Auswertung mit false; true erzeugt ein Ereignis.

```cpp
final float gain = signal * 2;
result = clamp(gain, -1, 1);
```

## Variablen

Jede Deklaration benötigt Typ, Namen und Anfangswert, gefolgt von einem Semikolon. Namen bestehen aus ASCII-Buchstaben (A–Z, a–z), Ziffern und Unterstrichen, dürfen nicht mit einer Ziffer beginnen und unterscheiden Groß-/Kleinschreibung. Schlüsselwörter, eingebaute Funktionen und Kontextnamen sind reserviert. Ein Name muss vor seiner Verwendung deklariert sein. Lokale Variablen in einem Block sind nur dort verfügbar; eine bereits sichtbare Variable darf dort nicht erneut deklariert werden. Funktionsparameter haben ihren eigenen Gültigkeitsbereich.

| Deklaration | Lebensdauer | Weitere Zuweisungen? | Anfangswert |
| --- | --- | --- | --- |
| `float x = signal;` | Aktuelle Auswertung und umgebender Gültigkeitsbereich | Ja | Laufzeitausdruck |
| `final float x = signal;` | Aktuelle Auswertung und umgebender Gültigkeitsbereich | Nein | Laufzeitausdruck |
| `const float X = 2;` | Konstanter Wert im Gültigkeitsbereich der Deklaration | Nein | Eingangsunabhängiger konstanter Ausdruck |
| `state float x = 0;` | Bleibt zwischen Auswertungen dieses Prozessors erhalten | Ja | Endliches Literal im Wertebereich des Typs |

### state

`state` merkt sich einen Wert zwischen Auswertungen. Der Initialisierer legt den Anfangswert fest; er wird nicht bei jedem Sample erneut zugewiesen. Änderungen bilden den Startwert der nächsten Auswertung. Der Zustand ist pro Kanal, Stimme und Instanz unabhängig und wird nicht global geteilt.

Alle state-Variablen müssen am Anfang stehen, vor ausführbaren Anweisungen, lokalen/final/const-Deklarationen oder Verzweigungen. Innerhalb von Funktionen und Blöcken ist state nicht erlaubt. Höchstens 64 Zustandsvariablen werden unterstützt. Als Anfangswert ist nur ein Literal zulässig, etwa `0`, `0.5` oder `false`; Eingänge, `pi`, andere Variablen und Berechnungen sind keine gültigen state-Initialisierer.

```cpp
state double phase = 0;
phase += 440.0 / sampleRate;
phase -= floor(phase);
result = sin(tau * phase);
```

Hier läuft phase vom vorherigen Wert aus weiter. Mit `double phase = 0;` statt der ersten Zeile würde phase bei jeder Auswertung zurückgesetzt und könnte sich nicht aufsummieren.

Bei Live-Änderungen bleibt Zustand mit gleichem Namen, Typ und Anfangswert erhalten. Ändert sich eines davon, beginnt diese Variable mit ihrem neuen Anfangszustand. Dokumente speichern Quelltext und Anfangswerte, nicht den laufenden DSP-Zustand; beim Öffnen beginnt die Verarbeitung mit den Anfangswerten. Ein Wert, der einfach bei jedem Sample einem Eingang folgen soll, benötigt kein state.

### final

`final` erlaubt die Initialisierung und verbietet danach weitere Zuweisungen, auch `+=`, `-=`, `*=` und `/=`. Es speichert keinen Wert zwischen Auswertungen. Audio berechnet den Initialisierer pro Sample neu, Control pro Block. In einer Funktion oder Schleife wird die Variable jedes Mal initialisiert, wenn die Ausführung die Deklaration erreicht.

```cpp
final float gain = signal * 2;
result = gain;
// gain = 1;   // ungültig: gain ist final
```

Auch Funktionsparameter dürfen final sein: `float square(final float x) { return x * x; }`. Das Argument darf zwischen Aufrufen wechseln; die Funktion darf x nicht neu zuweisen. Ohne final dürfen Parameter lokal verändert werden, ohne den Wert beim Aufrufer zu ändern.

### const

`const` definiert einen Wert unabhängig von aktuellen Eingängen und Laufzeitkontext. Erlaubt sind Literale, `pi`, `tau`, vorherige const-Variablen, Rechenoperationen, Casts und reine eingebaute Mathematikfunktionen. Großgeschriebene Namen sind eine Konvention, keine Pflicht.

```cpp
const float MIN_SPEED = 0.5;
const float MAX_SPEED = MIN_SPEED * 4;
final float speed = MIN_SPEED + signal * (MAX_SPEED - MIN_SPEED);
result = speed;
```

Die beiden Grenzen behalten ihre Werte; speed kann sich pro Auswertung ändern. Ein const-Initialisierer darf keine Eingänge, Kontextwerte (auch nicht sampleRate), state, veränderbare Lokale, final-Lokale, eigene Funktionen, `rand` oder `sineWave` verwenden. `const float X = signal;` ist beispielsweise ungültig, selbst wenn der Eingang gerade zufällig unverändert bleibt. Dafür final verwenden. Wie andere lokale Variablen werden const-Variablen nicht von eigenen Funktionen übernommen; als Argument übergeben oder innerhalb der Funktion deklarieren.

Const-Parameter werden nicht unterstützt; dafür final verwenden. `const` ist bereits schreibgeschützt, daher wird `const final` abgewiesen. Weder const noch final lassen sich mit state kombinieren. Die Zusätze beschreiben erlaubte Abhängigkeiten und Zuweisungen; sie garantieren weder schnellere Verarbeitung noch, dass intern jeder Initialisierer nur einmal ausgewertet wird.

### Darstellung im Editor

State-Namen erscheinen kursiv. Final-/Const-Namen, Eingangsnamen und schreibgeschützte Kontextvariablen sind fett. Final und const verwenden dieselbe semantische Variablenfarbe. Diese Darstellung verändert weder den Quelltext noch die Bedeutung einer Variablen.

## Datentypen

| Typ | Bedeutung |
| --- | --- |
| `bool` | true/false; geht in typisierte Arithmetik als 1/0 ein |
| `int` | Ganzzahl mit Vorzeichen, 32 Bit |
| `uint`, `unsigned int` | Ganzzahl ohne Vorzeichen, 32 Bit |
| `long` | Ganzzahl mit Vorzeichen, 64 Bit |
| `ulong`, `unsigned long` | Ganzzahl ohne Vorzeichen, 64 Bit |
| `float` | Gleitkommazahl, 32 Bit, etwa 7 signifikante Dezimalstellen |
| `double` | Gleitkommazahl, 64 Bit, etwa 16 signifikante Dezimalstellen |

Dezimalliterale ohne Suffix sind in typisierten Formeln double; `0.5f` ist ausdrücklich float. Ganzzahlsuffixe sind unter anderem `u`, `l` und `ul`. Casts stehen als `(double) value` oder `(int) value`. Zuweisungen und Argumente dürfen double implizit nach float umwandeln. Float-zu-Ganzzahl und verengende Ganzzahlzuweisungen benötigen einen expliziten Cast; ebenso das Mischen vorzeichenbehafteter und vorzeichenloser Ganzzahlen. Bool-Bedingungen benötigen einen Vergleich oder `(bool)`-Cast.

```cpp
int whole = 5 / 2;                 // 2: Ganzzahldivision
float fraction = 5.0 / 2;         // 2.5
int truncated = (int) fraction;   // 2
result = whole + fraction + truncated;
```

Ältere reine Float-Formeln behalten ihr Kompatibilitätsverhalten. Ganzzahl-/double-Deklarationen, Casts, Funktionen, Schleifen, final und const wählen die typisierte Ausführung. Buffer-Ressourcen sind Eingänge, kein Typ für lokale numerische Variablen oder Funktionsparameter.

## Ausdrücke

Von hoher zu niedriger Priorität: Klammern/Casts; unäre `+`, `-`, `!`; `*`, `/`, `%`; `+`, `-`; Vergleiche `<`, `<=`, `>`, `>=`; Gleichheit `==`, `!=`; `&&`; `||`. Gleichrangige binäre Operatoren werden von links nach rechts verknüpft. Logische Operatoren werten nur so weit aus, wie für das Ergebnis nötig. Klammern verdeutlichen die Absicht. `=` weist zu, `==` vergleicht.

Anweisungen enden mit `;`. Zuweisungen unterstützen `=`, `+=`, `-=`, `*=`, `/=`. Kommentare beginnen mit `//` und reichen bis zum Zeilenende. Geschweifte Klammern fassen Anweisungen zusammen. Bedingungen müssen bool sein; bei einer einzelnen Anweisung sind Klammern optional:

```cpp
if (signal > 0) {
    result = signal;
} else {
    result = 0;
}
```

Arrays, Pointer, Zeichenketten, ternäre Ausdrücke, beliebige C++-Aufrufe, Rekursion und while-Schleifen sind nicht verfügbar. Die unten beschriebenen `++`-/`--`-Formen gelten nur für Schleifenschritte, nicht für allgemeine Ausdrücke.

## Kontext

Kontextwerte sind schreibgeschützt und dürfen keine const-Variablen initialisieren.

| Name | Typisierter Wert | Bedeutung |
| --- | --- | --- |
| `sampleRate` | double | Verarbeitungssamplerate in Hz |
| `blockSize` | ulong | Vollständige Audioblocklänge in Samples, auch bei Feedbackverarbeitung |
| `sampleIndex` | ulong | Sampleoffset im aktuellen Audioblock, beginnend mit 0 |
| `samplePosition` | ulong | Von diesem Node verarbeitete Samples; bleibt bei Live-Änderungen erhalten; schreitet bei Control um einen ganzen Block voran |

Vergangene Sekunden: `samplePosition / sampleRate`. `sin`/`cos` verwenden Bogenmaß, `sineWave` Perioden. `pi` und `tau` sind mathematische Konstanten (double in typisierten Formeln). `Audio` = 1 und `Control` = 0 kennzeichnen Raten; Clamp/Wrap/Zero und Linear/Nearest sind Optionen für Buffer-Lesen, keine eigenständigen numerischen Variablen.

Numerische Eingänge bieten `.value` und `.rate` (Audio = 1, Control = 0), Trigger `.active`. Der bloße Eingangsname liefert bereits den aktuellen Wert. Ein Audioeingang besitzt keinen beliebigen Zugriff auf vergangene Samples; dafür eine Buffer-Ressource verwenden.

## Buffer

Für einen konfigurierten Buffer-Eingang namens buffer:

| Member | Bedeutung |
| --- | --- |
| `.size` | Kapazität in Samples |
| `.begin` | Älteste verfügbare absolute Sampleposition |
| `.end` | Position nach dem neuesten verfügbaren Sample |
| `.writePosition`, `.writepos` | Absolute nächste Schreibposition; bei Dateibuffern fest auf end |
| `.sampleRate` | Buffersamplerate in Hz |
| `.read(...)` | Sample lesen; siehe [buffer-read](#buffer-read) |

Metadaten und gelesene Samples sind float. Positionen sind absolute Samplepositionen, anders als der normierte 0–1-Positionsausgang des Buffer-Nodes. Ressourcen sind schreibgeschützt und nur als Eingänge unterstützt.

```cpp
result = buffer.read(buffer.end - 1, Clamp, Linear);
```

Das liest das neueste verfügbare Sample. Leere Buffer liefern null.

## Eigene Funktionen und begrenzte Schleifen

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

## Eingebaute Funktionen

In typisierten Formeln verwenden Mathematikfunktionen den gemeinsamen Argumenttyp; ganzzahlige Argumente rechnen in double, außer abs, sign, min, max und clamp, die den gemeinsamen Ganzzahltyp erhalten. sineWave, rand und Buffer-Lesen liefern float. Es erfolgt kein automatisches Oversampling.

| Bereich | Funktionen |
| --- | --- |
| Betrag und Begrenzung | [abs](#abs), [sign](#sign), [min](#min), [max](#max), [clamp](#clamp) |
| Potenzen und Logarithmen | [sqrt](#sqrt), [pow](#pow), [exp](#exp), [expm1](#expm1), [log](#log), [log2](#log2), [log10](#log10), [log1p](#log1p) |
| Trigonometrie | [sin](#sin), [cos](#cos), [tan](#tan), [asin](#asin), [acos](#acos), [atan](#atan), [atan2](#atan2) |
| Hyperbolische Funktionen | [sinh](#sinh), [cosh](#cosh), [tanh](#tanh), [asinh](#asinh), [acosh](#acosh), [atanh](#atanh) |
| Rundung und Interpolation | [floor](#floor), [ceil](#ceil), [round](#round), [mix](#mix) |
| Signal und Zufall | [sineWave](#sinewave), [rand](#rand) |
| Buffer-Zugriff | [buffer.read](#buffer-read) |

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

[Formula Group](../../nodes/de/formula-group.md) · [Handbuch](index.md) · [English](../en/formula-language.md)
