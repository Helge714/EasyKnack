# Beispiele

Öffne die nummerierten Projekte im Menü **Examples**. Beginne mit **01 Basics**.
Wähle bei Bedarf im Audio Output dein Ausgabegerät und starte die Wiedergabe.
Speichere eigene Varianten unter Projects; die installierten Beispiele sind Vorlagen.
Alle Beispiele verwenden vorhandene Nodes und benötigen weder Audiodateien noch Plugins.
Die Anordnung liest sich von links nach rechts; Modulationen und Rückführungen liegen darunter.

## 01 Basics

- **01 First tone:** Oscillator → Gain → Audio Output. Ein leiser Sinuston mit 220 Hz.
  Ändere Frequency und anschließend Gain. Eine Verbindung transportiert das Signal.
- **02 Filtered noise:** Weißes Rauschen durch einen Tiefpass. Bewege Cutoff von 200 bis
  8000 Hz und höre, wie sich die Klangfarbe ändert. Resonance zunächst unverändert lassen.
- **03 Tremolo:** Ein langsamer Oszillator moduliert die Amplitude eines hörbaren Tons.
  Multiplikation mit 0,5 und Addition von 0,5 bilden −1…1 auf 0…1 ab.
  Ändere die Frequenz des unteren Oszillators von 1 bis 8 Hz.
- **04 Live buffer - speed:** Ein Sägezahn wird in einen einsekündigen Recording Buffer
  geschrieben und mit Speed 0,75 gelesen. Nach kurzer Füllzeit ist ein tieferer Ton hörbar.
  Probiere Speed 1, 0,5 und −1. Reset setzt den Leser relativ zum aktuellen Schreibkopf zurück.
  Es gibt keinen automatischen Crossfade; beim Kreuzen des Schreibkopfs können Klicks entstehen.
- **05 Live buffer - position:** Der Speed Reader liefert seine reale Read position an einen
  Position Reader am gleichen Buffer. Nur der Position Reader ist hörbar.
  Offset 0 folgt exakt; probiere anschließend Offset 0,5. Der Value Viewer zeigt die
  umlaufende Write position. Position ist eine absolute Bufferkoordinate, nicht Distance.
- **06 Group with controls:** Eine gewöhnliche Gruppe enthält Oszillator und Filter.
  Ändere Frequency und Cutoff außen, öffne dann die Gruppe per Doppelklick und verfolge
  die zugehörigen inneren Eingänge. Diese Gruppe läuft einmal und erzeugt einen Audiostrom.

## 02 MIDI

Klicke zuerst die Tastatur im MIDI Input an. Spiele mit der Maus oder den dort
angezeigten Computertasten. Alternativ wähle dein angeschlossenes MIDI-Gerät.
Diese Beispiele sind ohne Noten absichtlich still. Gate steuert das Halten,
Trigger startet die Hüllkurve, Velocity bestimmt den Pegel.

- **01 Mono instrument:** Ein Sägezahn mit Attack/Decay/Release und Anschlagstärke.
  Halte eine Note und lasse sie los; der Ton klingt in der Release-Zeit aus.
- **02 Poly instrument:** Acht unabhängige Stimmen mit derselben Verschaltung.
  Spiele einen Akkord und lasse die Noten einzeln los. Voice Merge summiert die Stimmen
  erst nach ihren Hüllkurven; der niedrigere Ausgangspegel lässt Platz für Akkorde.

## 03 Instance Groups

- **01 Harmonic bank:** Acht Instanzen erzeugen die Obertöne einer Grundfrequenz von
  220 Hz. Im Inneren wird aus Index + 1 die Obertonnummer; die Formula-Gruppe gewichtet
  mit 1 / Obertonnummer. Ändere Count von 1 bis 16 und öffne die Gruppe.
  Instance Merge verwendet Mean, daher verändert Count auch den Pegel.
- **02 Odd harmonics:** Dieselbe Bank mit stummen geraden Obertönen.
  Öffne die innere Formula-Gruppe: Index 0 bezeichnet den ersten Oberton; gerade
  Indizes entsprechen ungeraden Obertonnummern. Vergleiche den hohleren Klang mit Beispiel 01.
  Bake ist für beide Beispiele optional und wird nicht vorausgesetzt.

## 04 Spectral Groups

- **01 FFT round trip:** Rauschen → synchrone FFT → Spectral Group → IFFT.
  Die Gruppe reicht Real und Imaginary unverändert durch. Öffne sie und verfolge
  Bin Context → Bin Output. Fenstergröße 1024 verursacht eine kurze systematische Latenz.
- **02 Bin lowpass:** Im Inneren wird jeder Bin oberhalb von 1200 Hz auf Magnitude 0
  gesetzt; die Phase bleibt erhalten. Ändere die Grenze in der inneren Formula-Gruppe.
  Hier wird einmal pro Bin und FFT-Fenster gerechnet, nicht einmal pro Audiosample.
  Eine harte Bin-Grenze kann zeitliches Nachschwingen erzeugen.

## 05 Formula Groups

Öffne die Formula-Gruppe per Doppelklick. Nach Änderungen **Apply** verwenden;
bei ungültigem Code bleibt die letzte gültige Formel aktiv.

- **01 Logarithmic mapping:** Value Input 0…1 wird auf 110…440 Hz abgebildet.
  In der Mitte liegen 220 Hz: gleiche Reglerabstände ergeben gleiche Frequenzverhältnisse.
  Bewege den oberen Regler und lies die kurze Formel `110 * pow(4, signal)`.
- **02 Function waveshaper:** Eine eigene Funktion komprimiert den Sinus weich.
  `final` schützt ihren Parameter; `const DRIVE` legt den konstanten Übersteuerungsfaktor
  fest. Vergleiche DRIVE 1 und 4. Die Formel arbeitet hier mit Audiorate.

## 06 Feedback

Die Rückführung enthält ausdrücklich einen Delay. Ohne Verzögerung entsteht
keine ausführbare Audio-Rückkopplung. Die voreingestellte Rückführungsverstärkung
liegt unter 1; beginne mit den gespeicherten Werten.

- **01 Echo:** Eine Clock löst alle zwei Sekunden einen kurzen Rauschimpuls aus.
  Delay 0,3 s und Rückführung 0,5 erzeugen hörbare, abklingende Wiederholungen.
  Ändere Time und den Faktor der unteren Math-Node. Die Rückführung wird vor dem
  erneuten Addieren gefiltert und abgeschwächt.
- **02 Resonator:** Ein ähnlicher Impuls regt eine kurze, gefilterte Schleife an.
  Die Delayzeit von ungefähr 6,8 ms erzeugt einen tonalen Nachklang; die tatsächliche
  Tonhöhe hängt auch vom Filter ab. Eine kürzere Time erhöht die Tonhöhe.
  Der Faktor 0,92 verlängert den Nachklang gegenüber dem Echo.

Die frühere generierte Sammlung wird beim Entwicklungsstart unter `.previous-examples`
gesichert und im Menü ausgeblendet. Eigene anders benannte Dateien bleiben erhalten.
