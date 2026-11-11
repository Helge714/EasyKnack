# Buffer und Aufnahme

Ein Buffer enthält Audiosamples. **File Buffer** stellt Dateidaten bereit; **Recording Buffer** hält einen laufend beschriebenen Ringbuffer. **Audio File Player** ist die direkte Wiedergabealternative. Audiodateien bleiben extern; der Player unterstützt Mono/Stereo, kein beliebiges Mehrkanalformat.

**Buffer Reader (Speed)** bewegt seinen eigenen Lesekopf mit der gewählten Geschwindigkeit. Speed ändert auch die Tonhöhe und ist kein unabhängiges Time Stretching. **Buffer Reader (Position)** liest die angegebene Position; springende Steuerwerte können klicken.

Unterscheide reale Leseposition, Schreibposition und den im Reader angebotenen Abstand. Die Leseposition allein beschreibt nicht die Distanz zum laufenden Schreibkopf. **Buffer Context** liefert Größe und Schreibposition des verbundenen Buffers; nutze diese, um eigene relative Abstände zu berechnen. Bei Ringbuffern sind Anfang und Ende benachbart; lineare Subtraktion allein berücksichtigt das nicht.

In einer Instance Group können mehrere Reader denselben externen Buffer mit eigenem Zustand lesen. Ein interner Recording Buffer besitzt dagegen pro Instanz eigenen Speicher. Viele oder große Buffer erhöhen den Speicherbedarf.

**Audio Recorder** zeichnet das verbundene Signal mit Start/Stop auf; warte auf den abgeschlossenen Take vor Export oder Weiterverwendung. Speicherort und unterstützte Formate stehen am Node. Für Messwertreihen statt Audiodateien verwende Measurement Recorder.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/buffers.md)
