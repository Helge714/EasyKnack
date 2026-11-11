# Signale und Verarbeitung

Eine Verbindung hat **Datentyp**, **Rate**, **Einheit** und gegebenenfalls **Kanäle**. Diese Eigenschaften sind unabhängig. Audio und numerische Werte können dieselben Rechen-Nodes verwenden. Buffer, Wavetable, Spectrum und Partials sind Ressourcen und benötigen passende Reader oder Prozessoren.

**Audio** berechnet pro Sample, **Control** einmal pro Block. **Auto** wählt die erforderliche Präzision. Ein Control-Signal gewinnt durch nachträgliche Umstellung keine verlorenen Zwischenwerte zurück. Stateful Formeln benötigen Audio-Präzision, wenn ihr Zustand pro Sample fortschreiten soll.

Ein **Gate** hält einen Zahlenwert, gewöhnlich 0 oder 1. Ein **Trigger** ist ein einzelnes Ereignis und kann erneut auftreten, während ein Gate noch 1 ist. MIDI-Nachrichten werden erst durch passende MIDI-Nodes zu Zahlen oder Triggern.

## Einheiten

Wähle Einheiten im Kontextmenü unterstützter Regler oder Ports. Bei manuellen Werten erhält ein Einheitenwechsel die physikalische Größe. Verbindungen übertragen dagegen Zahlen ohne automatische Einheitenanpassung: `1` aus einem Sekunden-Ausgang bedeutet an einem Millisekunden-Eingang `1 ms`. Wähle passende Einheiten oder verwende Unit Converter. Gruppengrenzen rechnen zusätzlich in die intern erwarteten Einheiten um.

Wichtige Beziehungen: `1 kHz = 1000 Hz`, `1 s = 1000 ms`, `1 turn = 360° = 2π rad`, `1 oct = 12 st = 1200 cent`, Amplitude `dB = 20 * log10(Faktor)`. Binbreiten in Bins und Oktaven sind verschiedene Interpretationen; eine Umrechnung braucht Binabstand und Mittenfrequenz.

## Mute und Feedback

Mute folgt dem Node: Effekte können durchreichen, Generatoren verstummen, Ressourcen können ausbleiben. Es ist kein universeller Nullwert. Gruppen verwenden ihr eingestelltes Bypass-Routing. Mute ändert nicht den Windows-Gerätemute.

Feedback braucht einen schützenden Delay und unterstützte Prozessoren. Sehr kurze Delays erhöhen den Aufwand. Wird der einzige Delay umgangen, kann die Rückverbindung mit einer Warnung deaktiviert werden.

Reglergrenzen sind nicht automatisch Grenzen verbundener Signale. In Node-Referenzen bedeutet ±∞ beliebige endliche Werte, nicht die Annahme von Infinity oder NaN.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/signals.md)
