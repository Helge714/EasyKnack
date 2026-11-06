# Datentypen

Der Datentyp beschreibt den Inhalt einer Verbindung. Er ist von ihrer
[Rate](rates.md), [Einheit](units.md) und Farbe zu unterscheiden.

## Signale und Nachrichten

| Datentyp | Inhalt |
| --- | --- |
| <a id="value"></a>Wert | Zahlen für Berechnungen und Steuerung, beispielsweise Frequenz oder Mischfaktor |
| <a id="audio"></a>Audio | Numerische Samples für die Klangverarbeitung |
| <a id="trigger"></a>Trigger | Einzelne Ereignisse ohne dauerhaft gehaltenen Zahlenwert |
| <a id="midi"></a>MIDI | Nachrichten wie Note On, Note Off, Control Change und Pitch Bend |
| <a id="vector"></a>Vektor | Drei zusammengehörige Komponenten, beispielsweise X, Y und Z |

Audio und Wert verwenden kompatible numerische Signale. Ein Math-Node kann
deshalb auch Audio verarbeiten. Eine andere Farbe allein bedeutet weder einen
anderen Zahlenbereich noch eine automatische Umrechnung.

Ein **Gate** ist ein gehaltener Wert, gewöhnlich 0 oder 1. Ein **Trigger** ist
ein Ereignis. Ein neuer Trigger kann auftreten, während ein Gate bereits 1 ist.
MIDI muss über passende MIDI-Nodes in numerische Werte oder Trigger umgewandelt
werden; ein MIDI-Port ist kein Frequenz- oder Gate-Eingang.

## Ressourcen

Ressourcenverbindungen verweisen auf zusammengehörige Daten, statt einen einzelnen
skalaren Wert zu liefern.

| Ressource | Inhalt | Typische Verwendung |
| --- | --- | --- |
| <a id="wavetable"></a>Wavetable | Eine periodische Wellenform mit Abtastwerten | Wellenformeingang eines Oszillators |
| <a id="buffer"></a>Buffer | Gespeicherte Audiosamples mit Länge und Schreibposition beziehungsweise Dateiende | Buffer Reader |
| <a id="spectrum"></a>Spectrum | Komplexe FFT-Bins mit Frequenz-, Fenster- und Zeitbezug | Spektralbearbeitung und IFFT |
| <a id="partials"></a>Partials | Verfolgte sinusförmige Komponenten mit Frequenz, Amplitude, Phase, Identität und Frame-Zeit | Teilton-Gruppe und spektrale Rekonstruktion |
| <a id="analysis"></a>Analysis | Analyseergebnis, beispielsweise ein Spektrum oder eine Autokorrelation | Viewer und Merkmalsberechnung |

Spectrum kann an geeignete Analysis-Eingänge angeschlossen werden. Ein beliebiges
Analysis-Ergebnis ist umgekehrt nicht automatisch ein rekonstruierbares Spectrum.
Eine Ressource wird mit einem passenden Reader oder Verarbeitungs-Node ausgewertet.
Das Weiterreichen einer Ressource bedeutet nicht, dass ihre gesamte Datenmenge
an jeder Verbindung kopiert wird.

<a id="channels"></a>

## Kanäle

Kanäle sind unabhängig vom Datentyp und von der Rate.

| Angabe in der Referenz | Bedeutung |
| --- | --- |
| Mono | Ein fester einzelner Signalverlauf |
| Stereo | Ein festes Paar aus linkem und rechtem Verlauf |
| Links / Rechts | Eine ausdrücklich zugeordnete Kanalseite |
| Auto | Die Kanalausprägung ergibt sich aus Eingängen und Node-Verhalten |
| — | Kanalangabe nicht anwendbar |

Ein Stereo-Signal benötigt nicht grundsätzlich zwei sichtbare Leitungen.
Kanaltrennung und Zusammenführung erfolgen mit den entsprechenden Channel-Nodes.
Polyphone Stimmen und Instance-Gruppen sind zusätzliche Verarbeitungsdimensionen;
sie sind nicht gleichbedeutend mit linkem und rechtem Kanal.

[Grundlagen](index.md) · [English](../en/data-types.md)
