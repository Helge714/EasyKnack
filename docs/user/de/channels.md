# Channel und Stimmen

Ein Audiokanal ist ein Signalverlauf innerhalb einer Verbindung: Mono enthält einen, Stereo Links und Rechts. Eine Stereo-Verbindung braucht deshalb nicht zwei sichtbare Leitungen. **Channel Separate** trennt Kanäle; **Channel Combine** setzt sie zusammen. **Pan** verteilt Audio zwischen Links und Rechts.

**Auto** bezeichnet eine vom Eingang und Node-Verhalten abgeleitete Kanalausprägung. Explizites Left/Right wählt eine Seite; Mix kann beide mitteln und gegenphasige Signale auslöschen. Prüfe den Kanalmodus bei Messungen und beim Zusammenführen.

**Stimmen/Instanzen** sind zusätzliche unabhängige Verarbeitungszustände. Eine Instance Group kann viele Instanzen mit jeweils Stereo-Audio besitzen. Instance Merge führt Instanzbeiträge zusammen; Channel Combine führt Kanäle zusammen. Diese Funktionen sind nicht austauschbar. Voice Merge betrifft die Stimmenebene.

Ein **MIDI-Kanal** adressiert Nachrichten und ist weder linker/rechter Audiokanal noch automatisch eine eigene DSP-Stimme. Die verwendeten MIDI- und Gruppen-Nodes bestimmen, wie daraus Klang entsteht.

[Handbuch](index.md) · [Node-Verzeichnis](../../nodes/de/index.md) · [English](../en/channels.md)
