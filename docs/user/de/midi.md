# MIDI und Steuerung

Verbinde **MIDI Input** mit passenden MIDI-Nodes. **MIDI Note** liefert musikalische Steuerwerte; **MIDI CC** liest Controller. MIDI-Nachrichten lassen sich nicht direkt als Audio oder Frequenz interpretieren. Verwende Graph-Nodes, wenn Ereignisse samplegenau in der Verarbeitung benötigt werden.

## MIDI Learn

Öffne am Editor **MIDI Driver → Learn MIDI** und bewege den Controller. Slider lernen CC; andere unterstützte Editoren können Note On lernen. Die Lernbewegung weist nur zu. Gerät, MIDI-Kanal und Controller-/Notennummer werden im Dokument gespeichert.

Slider bilden CC 0–127 auf ihren Zielbereich ab. Comboboxen wählen per CC einen Eintrag oder schalten pro Note On weiter. Checkboxen schalten beziehungsweise halten per Note und verwenden bei CC einen Schwellwert. Trigger feuern bei Note On oder aufsteigender CC-Schwellwertüberschreitung. Note On mit Velocity 0 gilt als Note Off.

**Configure MIDI driver** bietet Zielbereich, Umkehrung, logarithmisches Mapping für positive Bereiche und **Pickup**. Pickup wartet auf das Erreichen/Kreuzen des aktuellen Softwarewertes. Mappingwerte verwenden die angezeigte gespeicherte Parametereinheit. Eine Graphverbindung hat Vorrang vor dem Editor; nach dem Trennen wird MIDI mit einer neuen Nachricht wieder aktiv. Gekoppelte Gruppeneditoren erlauben keinen konkurrierenden Treiber.

## Glättung

Dokumenteneinstellungen bieten gepufferte Interpolation, Kantenrundung oder adaptive Übergänge. Pufferung glättet mit zusätzlicher Steuerlatenz; adaptive Übergänge benötigen keinen Lookahead. Ein MIDI-Treiber kann Dokumentstandard, eigene Werte oder Aus verwenden. Diskrete Auswahlen, Counts und Seeds wechseln direkt. Für Änderungen pro Sample braucht der Signalweg Audio-Rate.

Zuordnung und Mappingänderungen sind undo-fähig; eingehende MIDI-Werte erzeugen keine Undo-Einträge, werden aber als Parameterwerte mitgespeichert.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/midi.md)
