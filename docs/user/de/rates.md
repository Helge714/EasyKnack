# Raten

Die Rate bestimmt, **wann** Werte ausgewertet werden. Der
[Datentyp](data-types.md) bestimmt, **was** eine Verbindung transportiert.

## Audio, Control und Auto

| Rate | Bedeutung | Darstellung am numerischen Port |
| --- | --- | --- |
| <a id="audio"></a>Audio | Ein Wert pro Sample | Kreis |
| <a id="control"></a>Control | Ein Wert pro Audioblock; innerhalb des Blocks gehalten | Raute |
| <a id="auto"></a>Auto | Automatische Wahl zwischen Audio und Control nach den Regeln des Nodes | Form der aufgelösten Rate |

Die aktuelle DSP-Konfiguration verwendet 48.000 Samples pro Sekunde und Blöcke
mit bis zu 256 Samples. Ein vollständiger Block dauert etwa 5,33 ms.

Auto ist keine zusätzliche Berechnungsrate. Bei adaptiven numerischen Nodes führt
ein Audioeingang gewöhnlich zu einem Audioausgang; reine Controleingänge erlauben
Control. Die Eingänge behalten dabei ihre jeweiligen Raten: Ein Controlwert muss
nicht für jedes Sample neu berechnet werden, nur weil ein anderer Eingang Audio
liefert. Feste Raten und Sonderregeln stehen in der jeweiligen Node-Referenz.

Ein Wert mit Audiorate muss kein hörbares Audiosignal sein. Auch Frequenz,
Position, Gate oder andere Steuerwerte können sich sampleweise ändern.

## Ratenwechsel

Ein Controlwert an einem Audioeingang wird für den Block gehalten. Dadurch
entstehen keine interpolierten Zwischenwerte.

Ein Controleingang liest ein anliegendes Audiosignal grundsätzlich am Blockanfang,
sofern der Node keine andere Mess- oder Reduktionsmethode festlegt. Kurze Änderungen
innerhalb des Blocks können dadurch verloren gehen. Der **Rate Converter** bietet
dafür unter anderem erstes/letztes Sample, Mittelwert, RMS, Minimum, Maximum und
Absolutspitze.

Ein Mittelwert eines symmetrischen Wechselspannungssignals kann nahe null liegen.
Für dessen Pegel sind RMS oder Absolutspitze geeigneter.

## Trigger und MIDI

<a id="trigger"></a>**Trigger** bezeichnet einzelne Ereignisse, die eine Aktion auslösen. Der Port
wird als Doppelraute dargestellt. „Samplegenau“ beschreibt die zeitliche
Auflösung eines Ereignisses, keine zusätzliche Rate. Ein Trigger ist kein
gehaltener Zahlenwert. Zur Umwandlung dienen entsprechende Trigger-Konverter.

MIDI enthält zeitlich zugeordnete Nachrichten und ist ein eigener Datentyp.
Ein **MIDI Note** kann daraus beispielsweise Frequenz und Gate mit Audiorate
sowie separate Trigger erzeugen. Die Speicherung als Ereignisse macht die
numerischen Ausgänge nicht zu Control-Signalen.

## Ressourcen und Glättung

Eine Ressource mit Controlrate kann einen vollständigen Buffer oder ein Spektrum
enthalten. Control beschreibt hier nicht die Anzahl der enthaltenen Samples.
FFT-Fenster, Hopgröße und Aktualisierungsmodus bestimmen den zeitlichen Abstand
der Spektralframes; sie sind unabhängig von der gewöhnlichen Audioblockgröße.

Glättung ist eine eigene Funktion. Ein Ratenwechsel glättet nicht automatisch.
Die [Interpolation manueller und MIDI-gesteuerter Regler (englisch)](../manual-controls.md)
betrifft Editoränderungen; für verbundene Signale ist bei Bedarf ein passender
Glättungs-Node erforderlich. Ein nachfolgender Controlpfad kann einen geglätteten
Verlauf wieder auf Blockwerte reduzieren.

[Grundlagen](index.md) · [English](../en/rates.md)
