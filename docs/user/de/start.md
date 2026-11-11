# Einstieg und Bedienung

Wähle **Datei → Neu**, stelle am **Audio Output** dein Ausgabegerät und eine niedrige Lautstärke ein und starte Audio. Der Anfangsgraph verbindet einen Oscillator mit dem Ausgang. Für Mikrofon oder externe Signale verwende Audio Input.

Füge Nodes über das Add-Menü hinzu. Ziehe vom Ausgang zum passenden Eingang; Warnungen erklären abgelehnte Verbindungen. Der Inspector zeigt die Einstellungen der Auswahl. **N** schaltet ihn um, **Strg+Umschalt+L** ordnet den aktuellen Graphen an. Menüs zeigen weitere Tastenkürzel. Ausblenden spart Platz, stoppt aber keine Berechnung.

Speichere als `.knack`. Beispiele öffnen ein neues Dokument; speichere eigene Varianten unter einem neuen Namen. Undo/Redo gilt für den geöffneten Graphen, Speichern für das gesamte Dokument. Kamerabewegungen allein machen ein Dokument nicht geändert. Nach einem Absturz bietet der nächste Start vorhandene Wiederherstellungskopien an; sie ersetzen kein regelmäßiges Speichern.

## Audio und Dateien

WASAPI verwendet Windows-Geräte; ASIO benötigt einen installierten passenden Treiber. Gerätestärke am WASAPI-Ausgang verändert den Windows-Endpunkt; Gain verändert das Signal im Graphen. Copy-Aufnahme von System-/Anwendungsaudio lässt den ursprünglichen Wiedergabeweg bestehen. Für ausschließlich bearbeiteten Ton leite die Quellanwendung über ein separat installiertes virtuelles Kabel zu EasyKnack und von dort zum physischen Ausgang.

Audiodateien und Plugins werden nicht in `.knack` eingebettet. Prüfe sie nach dem Weitergeben oder Verschieben. Bei Aussetzern prüfe Gerätestatus und Renderdiagnose. Für Fehlerberichte helfen **Hilfe → Über**, die Meldung und ein kleines Beispieldokument.

## Einstellungen und Hilfe

Programmeinstellungen betreffen Sprache und Darstellung. Dokumenteneinstellungen betreffen unter anderem Steuerungsglättung, unbenutzte Gruppen und experimentelle Funktionen. Alte Dokumente mit experimentellen Nodes aktivieren die Freigabe automatisch, sofern kein ausdrückliches Aus gespeichert ist. Ausschalten bei vorhandenen experimentellen Nodes verlangt eine Warnungsbestätigung; vorhandene Nodes bleiben erhalten.

Die Dokuansicht speichert **Wie Anwendung / Deutsch / English** unabhängig von der Programmsprache. Node-Kontextmenü und Hilfe öffnen die passende Kurzreferenz; Zurück/Vorwärts folgt lokalen Links.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/start.md)
