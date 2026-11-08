> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Audio Routing – Windows-MVP

Aktueller Ausbau: [WASAPI/ASIO und externe Routingtools](external-audio-routing.md).
Der eigene virtuelle Kernel-Treiber ist zurückgestellt. Die unten beschriebene
ROUTE-/Treiberarchitektur ist eine spätere Option, keine Voraussetzung für IO.

## Grenzen zwischen Komponenten

`AudioBackend.hpp` enthält die plattformneutralen Endpoint-Typen, Fähigkeiten,
Capture-Modi, Kanalansichten, die Stream-Konfiguration und das DSP-Buffer-Interface.
`WindowsAudioBackend.hpp` implementiert Geräteerkennung, Audiositzungserkennung und
WASAPI-Hardwarestreams. `WindowsProcessCapture.hpp` kapselt Prozess-Loopback mit
`ActivateAudioInterfaceAsync`, einschließlich asynchroner COM-Lebensdauer und Abbruch.
Ein künftiges PipeWire-/Core-Audio-Backend ersetzt diese beiden Windows-Dateien;
Node-Definitionen und DSP-Graph benötigen dafür keine Plattformabfragen.

Der bestehende authentifizierte TCP-Kanal verwendet Protokoll 6. Java fragt
ENDPOINTS und ROUTING_STATUS ab und übergibt IO-Konfiguration als Teil des atomaren
GRAPH-Plans. Es gibt keine zweite JNI-Brücke. `.knack` enthält einen typisierten
Endpoint-Verweis mit stabiler ID und Fallback-Namen. Verfügbarkeit, Prozess-IDs und
Streamhandles sind ausschließlich Laufzeitdaten und erzeugen keine Undo-Einträge.

Ein Signalport transportiert Stereo. Bestehende Sample-Schleifen bleiben erhalten:
Die Engine hält pro Kanal eine eigene Prozessorinstanz und setzt `channelIndex`.
Input-Nodes lesen gemeinsame, pro Endpoint/Block vorbereitete Eingangsbuffer. Zwei
Nodes mit derselben Quelle konsumieren dadurch keine unterschiedlichen Bufferteile.
Output-Nodes addieren in den Buffer ihres Ziels; unterschiedliche Ziele bleiben getrennt.

## Thread- und Bufferbesitz

- Steuerthread: entdeckt Endpoints, löst persistierte Anwendungs-IDs in laufende
  Prozessbäume auf, öffnet Streams, kompiliert Graphen und bearbeitet TCP.
- DSP-Takt: bei reinem WASAPI der Callback eines erfolgreich geöffneten Ausgangs;
  bei ausgewählten ASIO-Kanälen der Callback genau eines ASIO-Treibers. Nur ohne
  aktiven WASAPI-Ausgang bleibt der stille Timer als Ersatz. Vorbereitete Graphen
  wechseln an Blockgrenzen; es rendert immer nur ein Taktgeber.
- Das taktgebende WASAPI-Gerät erhält seine Samples direkt, ohne Ausgangsring.
  Capture und zusätzliche Playback-Geräte übertragen Frames durch SPSC-Ringbuffer. Jeder
  Ring hat genau einen Produzenten und einen Konsumenten. Prozess-Loopback wartet
  außerhalb der Bufferverarbeitung auf das WASAPI-Ereignis.
- Kein Heap, Logging, GUI/Java-Aufruf oder blockierender Mutex in den eigenen
  Sample-Callbacks. Streamänderungen stoppen den DSP-Takt vor dem Austausch der
  Streamliste; Destruktoren und Thread-Joins laufen auf dem Steuerthread.
- Ringbuffer: 8192 Stereoframes Kapazität; Reserven siehe externe Routingdokumentation. Voll: neue Frames
  verwerfen; leer: Stille. Keine gegenseitige Änderung der Cursor. Kein Zero-Copy-
  Zugriff auf Bufferspeicher, dessen Besitzer ihn gleichzeitig überschreiben kann.

Der MVP ist auf 32 ausgewählte Endpoints bzw. 64 native Streams begrenzt. Die DSP-
Anzeige misst Graph/Bufferübergabe, nicht die gesamte Audio-Treiberarbeit. Geräte-
Sampleraten werden im Backend konvertiert. Adaptive Driftkorrektur gleicht kleine
Taktabweichungen aus; lange Sessions und Bluetooth benötigen zusätzliche
Belastungstests. Streamänderungen sind noch nicht überblendet.

## Feedback und Verfügbarkeit

System-Capture schließt den eigenen Host-Prozess samt Kindprozessen aus. Die eigene
Audiositzung wird nicht als Anwendungsquelle angeboten. Mehrere unabhängige Prozesse
mit gleicher EXE werden aggregiert; überlappende Prozessbäume werden einmal erfasst.
Graphzyklen werden weiterhin abgewiesen; ein späterer Feedback-Node müsste expliziten
Zeit-/Zustandsspeicher besitzen. Akustische Rückkopplung über Mikrofon/Lautsprecher
ist keine digital erkennbare Graphkante.

Explizite Geräte-IDs fallen nie auf den Standard zurück. Die logische ID `default`
folgt dagegen absichtlich Windows. Ausgesteckte Geräte und beendete Anwendungen
bleiben im Dokument erhalten; die Auswahl zeigt ihren gespeicherten Namen und
„nicht verfügbar“. Erneute Erkennung derselben ID öffnet die Quelle wieder. Fehler
beim Öffnen werden an Java gemeldet. Es gibt kein automatisches Projekt-Rewriting.

## Prüfungen

Automatisiert: Ringbuffer-Grenzen, Mono/Stereo, mehrere Quellen/Ziele, alte DSP-Nodes,
Endpoint-Verweise, fehlende Quellen, Undo, `.knack`-Roundtrip, UI-Rendering und
Protokollintegration. Windows-Gerätetests öffnen den Standardausgang stumm und
prüfen System-/Prozess-COPY mit einer eigenen stummen Testanwendung; Audiodaten
werden weder gespeichert noch aus dem Test-Capture wiedergegeben.

Manueller Abnahmetest für die konkrete Hardware:

1. Hardwareeingang wählen, Mono/Stereo prüfen, an den gewünschten Ausgang verbinden.
2. Eine Anwendung starten und deren Quelle wählen. COPY muss die Originalausgabe
   unverändert lassen; Start/Stop in EasyKnack beeinflusst nur EasyKnack.
3. Zwei unterschiedliche Eingänge über Math/Add mischen; zwei Output-Nodes auf
   verschiedene physische Geräte legen. Kanaltrennung und Lautstärke prüfen.
4. Ein ausgewähltes USB-Gerät entfernen: Status und Stille; erneut verbinden:
   Wiederverbindung derselben ID. Windows-Standard ändern: nur `default` folgt.
5. Anwendung beenden/neustarten, Projekt speichern/öffnen und einen nicht mehr
   vorhandenen Endpoint ersetzen. Historie und gespeicherte Auswahl prüfen.

## Zurückgestellte Option: eigenes ROUTE / virtuelle Endpoints

Noch kein Treiber und keine Änderung der Windows-Anwendungszuordnung. ROUTE bleibt
eine eigene, momentan nicht verfügbare Fähigkeit. Der zukünftige virtuelle Treiber
soll ausschließlich Samples transportieren. DSP bleibt im User-Space.

Ein separater Transportadapter soll gemeinsam genutzte, versionierte Buffer verwalten:
Format/Rate/Kanäle, Kapazität, getrennte Cursor, Generation/Session-ID und Health-Zähler.
Treiber- und Engine-Lebensdauer dürfen nicht voneinander abhängen; eine verlorene
Engine liefert Stille. Unser In-Process-Ring ist **kein** fertiges Kernel-IPC-Protokoll.
Treibertransport benötigt eigene Speicherbarrieren, Zugriffsrechte und ABI-Tests.

Der erste ausführbare Schritt ist inzwischen implementiert: ein eigener versionierter
Windows-Shared-Memory-Transport mit ACL, zwei Sample-Richtungen, Heartbeats und
Zwei-Prozess-/Abbruchtests. Details und Grenzen stehen in
[virtual-audio-transport.md](virtual-audio-transport.md). Das ist noch keine
Kernel-Anbindung und noch kein installiertes virtuelles Audiogerät.

Ein Routing-Manager außerhalb des DSP muss vor jeder Umleitung Originalzuordnung
und Besitz speichern, nur unterstützte Systemwege benutzen und beim Ende genau
seine eigenen Änderungen wiederherstellen. Virtuelle Endpoint-IDs erhalten eine
eindeutige EasyKnack-Zuordnung zur Feedbackprüfung. Erst mit diesem Transport und
Manager darf das Backend `applicationRouting`/`virtualInputs`/`virtualOutputs`
melden und die UI ROUTE anbieten. Linux, macOS und Netzwerk-Audio bleiben später.

API-Referenz: [Microsoft – Prozess-Loopback](https://learn.microsoft.com/en-us/windows/win32/api/audioclientactivationparams/ns-audioclientactivationparams-audioclient_process_loopback_params).
