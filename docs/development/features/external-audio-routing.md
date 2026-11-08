> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Externe Audioverbindungen unter Windows

EasyKnack verwendet Standard-Schnittstellen statt spezieller Integrationen für
einzelne Routingprogramme. Der eigene virtuelle Kernel-Treiber ist zurückgestellt;
der Audiohost lädt ihn nicht. Es wird keine fremde Routingsoftware installiert,
gebündelt oder automatisch konfiguriert.

## Geräte im Nodeeditor

Im Audioeingang und Audioausgang steht vor jedem Eintrag die Schnittstelle:

- **WASAPI**: sämtliche von Windows angebotenen Aufnahme-/Wiedergabegeräte,
  einschließlich virtueller Geräte externer Programme. Namen werden nicht zur
  Erkennung bestimmter Hersteller benutzt. Windows-Geräte werden intern weiter
  mit den bisherigen Hardware-Endpoint-Typen geführt; das bedeutet nicht, dass
  es sich um physische Geräte handeln muss.
- **ASIO**: Ein-/Ausgänge installierter 64-Bit-Treiber, als Kanalpaare 1–2, 3–4
  usw. Ein einzelner letzter Eingangskanal wird auf beide Stereo-Kanäle gelegt;
  ein einzelner Ausgang erhält den Mittelwert von links/rechts.
- **Mitschnitt**: System-/Prozess-Loopback, weiterhin COPY. Dieser Weg unterbindet
  die ursprüngliche Ausgabe nicht.

Die Auswahl bleibt im `.knack`-Dokument gespeichert. ASIO-IDs enthalten die
Treiber-CLSID, Richtung und den nullbasierten ersten Kanal. Anzeigenamen sind
keine Identitäten. Fehlende Geräte liefern Stille und einen Statushinweis; es
gibt keinen automatischen Wechsel zum Lautsprecher. Alte Dokumente bleiben gültig.
Der Eintrag „Copy“ erscheint nur bei System-/Prozess-Mitschnitten, nicht bei
Geräteeingängen. Kein Backend behauptet, fremde Anwendungen automatisch umzuleiten.

## Beispiel: virtuelles Kabel über WASAPI

### Gerätepegel und Lautstärketasten

WASAPI-Ausgangsnodes zeigen einen live synchronisierten Windows-Gerätepegel in
Prozent und den EasyKnack-Ausgangssignalpegel in dBFS (Summe der Nodes dieses
Endpunkts, vor dem Windows-Gerätepegel). Externe Pegeländerungen verändern weder
das Dokument noch die Undo-Historie. Ein in Windows stummgeschaltetes Gerät wird
im Reglertext als „stumm“ angezeigt; Bewegen des Reglers auf einen positiven Wert
hebt diese externe Stummschaltung auf. Es gibt keinen zusätzlichen Geräte-Mute-Button.
Der Windows-Gerätepegel gilt auch für andere Programme auf demselben Gerät.

„Mit Lautstärketasten steuern“ wird pro Ausgangsnode im Dokument gespeichert,
auch innerhalb von Gruppen. Während EasyKnack läuft, werden Lauter/Leiser an
die ausgewählten verfügbaren WASAPI-Geräte geleitet, auch wenn EasyKnack nicht
im Vordergrund ist. Ein Tastenschritt verändert den gemeinsamen Wert um zwei
Prozentpunkte am lautesten ausgewählten Ausgang. Alle anderen ausgewählten
Geräte werden proportional angepasst: 20/40 % wird beispielsweise zu 21/42 %.
Das Verhältnis bleibt auch beim gemeinsamen Absenken auf 0 % für das nächste
Aufdrehen erhalten, solange die Zielauswahl unverändert bleibt und der Host läuft.
Sind anfangs alle Geräte auf 0 %, werden sie gleichmäßig angehoben.
Allein das Ankreuzen ändert keine Lautstärke.
Die Regler selbst steuern weiterhin jeweils ihr eigenes Gerät. Ohne verfügbare
Ziele bleiben die Tasten bei Windows. Die Windows-Stummtaste wird nicht umgeleitet.
Die aktuellen Werte erscheinen in den Nodes; EasyKnack zeigt keinen zusätzlichen
Pegelbalken für abgefangene Tasten.
ASIO bietet hier keinen Windows-Gerätepegel und daher keine entsprechende Kopplung.

Die Erfassung nutzt einen Windows-Low-Level-Tastaturhook. Er führt keine
Geräte-/COM-Operationen aus; diese laufen auf dem Host-Steuerthread. Bei fehlendem
Steuer-Heartbeat wird die Erfassung nach spätestens 1,5 Sekunden inaktiv, beim
Schließen entfernt. Ein Hook-Fehler wird gemeldet. Tastaturen, die ihre Lautstärke
nicht als Windows-Volume-Tasten liefern, benötigen gegebenenfalls eine weitere
Integration; die physischen Surface-Tasten sind noch manuell zu prüfen.

Die vorhandene Node-Mute-Aktion bleibt lokal im Graphen: Ein gemuteter Input wird
im Rechenplan zur verbundenen Nullquelle, damit nachfolgende Nodes nicht auf ihre
Slider-Standardwerte zurückfallen. Ein gemuteter Output liefert keinen Beitrag
mehr. Windows-Geräte-Mute und die sichtbaren Verbindungen werden dadurch nicht
verändert; andere Nodes dürfen dasselbe Gerät weiter verwenden.

### Kabel verbinden

1. Routingtool/virtuelles Kabel nach dessen Anleitung separat installieren.
2. In Windows oder dem Player dessen virtuelles Wiedergabegerät auswählen.
3. Im EasyKnack-Audioeingang das zugehörige Aufnahmegerät auswählen.
4. Nodes verbinden und im Audioausgang explizit die Kopfhörer/Lautsprecher wählen.
5. Audio starten. Direkte Monitoring-/„Dieses Gerät als Wiedergabequelle verwenden“-
   Verbindungen im externen Programm würden einen zusätzlichen unbearbeiteten Weg
   erzeugen; die gewünschte Verschaltung liegt beim Benutzer.

Nicht „System Default“ als EasyKnack-Ausgang wählen, wenn das Standardgerät selbst
der virtuelle Kabeleingang ist: Das erzeugt eine Rückkopplung.

## Beispiel: ASIO-Insert

In einem Routingtool mit ASIO-Insert den Send/Return für die gewünschten Kanäle
aktivieren. In EasyKnack Eingang und Ausgang des gleichen Insert-Treibers und die
passenden Kanalpaare wählen. Das Routingtool muss seine direkte Originalausgabe
durch den Return ersetzen. Die konkrete Belegung bestimmt dessen Dokumentation.

Ein Graph verwendet **höchstens einen ASIO-Treiber**, aber mehrere seiner
Kanalpaare. Dessen Duplex-Callback taktet direkt den DSP; die ASIO-Verbindung hat
keine zusätzliche Ringbuffer-Warteschlange. Parallel sind WASAPI-Geräte nutzbar;
diese werden über ihre eigenen Ringbuffer an die ASIO-Clock angepasst.

Die Engine bleibt bei **48 kHz und Stereo pro Nodeport**. ASIO verwendet die vom
Treiber bevorzugte Buffergröße (1 bis 8192 Frames); die Engine teilt sie intern
in maximal 256 Frames auf. Andere Sampleraten/DSD werden nicht stillschweigend
angenommen. Der Treiber muss 48 kHz liefern können. Mehrkanalgeräte sind über
mehrere Stereo-Nodepaare erreichbar, nicht als ein einzelner Surround-Port.

Treiber-Reset-/Resync-Anfragen werden im Callback nur markiert; der Steuerthread
baut die Buffer neu auf. Währenddessen werden ASIO-Ausgänge stumm gehalten.
Die periodische Geräteabfrage stoppt die Wiedergabe nur bei tatsächlichen Änderungen.
ASIO-Kanäle werden bei der ersten Treibererkennung abgefragt, um Treiber nicht alle
zwei Sekunden erneut zu laden. Nach Änderungen der Treiber-Kanalkonfiguration den
Audiohost neu starten. Nicht initialisierbare Treiber erhalten Diagnoseeinträge.

## Clock, Pegel und Grenzen

WASAPI konvertiert Gerätesignale nach Float/Stereo/48 kHz. Ein begrenzter SPSC-Ring
gleicht den Takt unabhängiger Geräte mit einer langsamen Füllstandsregelung und
linearer Interpolation aus (maximal ±1 %). Das ist ein rudimentärer Drift-Ausgleich,
kein hochwertiger Sample-Rate-Konverter für große Ratenunterschiede. Unterläufe
liefern Stille und puffern neu, Überläufe verwerfen neue Frames. Die Latenz hängt
von Gerät, Treiber, Routingtool und Puffern ab; sie ist nicht null.

Im reinen WASAPI-Betrieb taktet ein erfolgreich geöffneter Ausgang den Graphen
direkt in seinem Callback. Sein Ausgangsring entfällt. Die Auswahl erfolgt stabil
nach Endpoint-ID; zusätzliche Ausgänge bleiben unabhängig gepuffert. Ohne aktiven
Ausgang dient der stille Timer als Ersatz. Bei ASIO bleibt allein ASIO Taktgeber.
Vor Änderungen werden alle betroffenen Callbacks gestoppt; Eingänge und zusätzliche
Ausgänge starten vor dem taktgebenden Ausgang.

WASAPI fordert eine Geräteperiode von 128 Frames an (2,67 ms bei 48 kHz), akzeptiert
aber die vom Treiber tatsächlich unterstützte Größe und variable Callbacklängen.
Die Engine zerlegt größere Callbacks weiterhin in höchstens 256 Frames. Miniaudio
verwendet für diese WASAPI-Threads die MMCSS-Kategorie „Pro Audio“ und übernimmt
notwendige Sampleratenkonvertierung, damit Low-Latency Shared Mode nutzbar bleibt.

Geräteeingänge halten zwei tatsächlich beobachtete Capture-Pakete als Sollfüllstand,
mindestens 256 Frames (`inputReserve` in `WindowsAudioBackend.hpp`). Der adaptive
Leser benötigt außerdem mindestens zwei DSP-Leseblöcke. VB-CABLE liefert hier
480-Frame-Pakete; daraus ergeben sich 960 Frames bzw. 20 ms Sollfüllstand. Geräte
mit kleineren Paketen erhalten entsprechend kleinere Reserven. Prozess-Loopback
behält 2048 Frames Reserve. Zusätzliche, unabhängig getaktete
Ausgänge behalten 2048 Frames Reserve (`adaptiveTargetFrames` in `AudioBackend.hpp`).
Der Sollfüllstand ist keine gemessene Ende-zu-Ende-Latenz. Geräteperioden, Paketgrößen,
VB-CABLE und Windows tragen weitere Verzögerung bei. Unter 10 ms insgesamt sind
mit dieser Konfiguration nicht zugesichert. Der direkte ASIO-Pfad verwendet diese
Ringe nicht.

Der bestehende Master-Sicherheitspegel von 0,2 und das finale Clamping bleiben
erhalten. Ein reiner Durchleitungsgraph ist dadurch aktuell nicht pegelneutral.
Diese Begrenzung ist kein dynamischer Lautheitskompressor/Limiter.

ASIO-Sampleformate: 16/24/32-Bit Integer, 32/64-Bit Float sowie rechtsbündige
16/18/20/24-Bit Integer im 32-Bit-Container; Little- und Big-Endian. NaN/Inf wird
abgefangen, Ausgänge werden auf ±1 begrenzt.

VST3 und JACK sind **nicht implementiert**. VST3 ist der nächste eigenständige
Betriebsmodus für Hosts, die Plugins statt Aufnahmegeräte/ASIO-Inserts anbieten.
Nicht jedes Routingtool bietet automatisch einen dieser Wege an.

## Build und Prüfung

`mvn -Pnative-audio verify` baut den Host mit den mitgelieferten ASIO-Interface-
Headern und führt den simulierten ASIO-Backendtest aus. Keine SDK-Downloads beim
Build. CMake enthält denselben Test. Lizenzhinweise stehen unter
`native/audio-host/third_party/asio/PROVENANCE.md`; der ASIO-fähige native Build
verwendet die GPLv3-SDK-Option. Fremde Routingtools behalten ihre eigenen Lizenzen.

Der Fake-Treiber prüft echte Graphverarbeitung im Duplex-Callback, beide Buffer,
513-Frame-Buffergrenzen, Stereo-Kanalzuordnung, PCM-Konvertierung, Reset,
Fehlerbereinigung und mehrfaches Start/Stop. Der Clock-Test simuliert längere
Wiedergabe mit abweichender Geräteclock. Das ersetzt keinen Hör-/Latenztest mit
einem konkreten externen Routingtool und dessen Treiber.
