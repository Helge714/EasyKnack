# Virtueller Audiotransport – erster ausführbarer Baustein

Der Shared-Memory-Transport ist implementiert und wird zwischen zwei echten Windows-
Prozessen getestet. Er ist eine Bibliothek für die nächste Backend-/Treiberanbindung.
Er erzeugt noch kein Windows-Audiogerät und ändert keine Anwendungszuordnung.
ROUTE bleibt in EasyKnack deshalb weiterhin deaktiviert.

## Dateien und Vertrag

- `native/audio-host/src/audio/transport/AudioTransportWire.hpp`: zeigerfreies,
  fest dimensioniertes Speicherlayout, experimentelle ABI-Version 1.
- `WindowsSharedAudio.hpp`: Mapping-Besitz, Zugriffsschutz, Validierung und
  Callback-Zugriff mit Windows-Interlocked-Synchronisation.
- `native/audio-host/tests/SharedAudioTransportTest.cpp`: echte Prozesskopplung
  einschließlich Framevergleich, Ring-Wraparound und Prozessabbruch.

Ein Mapping enthält Header, zwei Teilnehmer und zwei unabhängige SPSC-Ringe:
Device → Engine und Engine → Device. Kapazität und Kanalzahl werden bei Erstellung
festgelegt: 64–8192 Frames in Zweierpotenzen, 1–8 Kanäle, interleaved Float32 LE.
Die Sample-Rate beschreibt das vereinbarte Format; der Transport führt kein
Resampling aus. Die bestehende Engine benötigt weiterhin 48 kHz Stereo, bis ein
Backendadapter ausdrücklich ein anderes Format konvertiert.

Im Header stehen Signatur, ABI-Version, Mappinggröße, Format und eine neue
128-Bit-Sitzungskennung. Vor dem Öffnen erhält der Gegenprozess Mappingname und
Sitzungskennung über einen **vertrauenswürdigen Steuerkanal**. Ein alter Name oder
eine falsche Kennung darf nicht unbemerkt an eine neue Sitzung anbinden. In `.knack`
bleibt die stabile Endpoint-ID; Mappingnamen und Sitzungskennungen gehören dort nicht hinein.

Die IPC-ACL erlaubt nur dem aktuellen Windows-Konto und SYSTEM Zugriff. Das ist
keine Isolierung gegenüber anderen Prozessen desselben Kontos. Die Treiberanbindung
muss zusätzlich ihren Handle-/IOCTL-Zugriff prüfen. Weder Kernelpointer noch
`std::atomic`-Objekte, STL-Container oder prozesslokale Handles stehen im Wire-Layout.

## Nutzung und Lebensdauer

Auf dem Steuerthread wird `WindowsSharedAudio::create(format)` ausgeführt. Der
Gegenprozess öffnet `WindowsSharedAudio::open(name, expectedSession)`. Jede Seite
beansprucht genau einmal ihre Rolle mit `SharedAudioClient(mapping, Side::...)`.
Erst wenn beide Teilnehmer online sind, wird Audio übertragen.

Ein Audio-Thread je Seite verwendet `read()` und `write()`. Die Rückgabe enthält
die tatsächlich übertragenen Frames und den Status. Der gesamte angeforderte
Lesebuffer wird zuerst mit Stille gefüllt; fehlende Frames bleiben Stille.
Ein voller Ring nimmt nur den passenden Teil an. Neue überschüssige Frames werden
nicht geschrieben; der Aufrufer kann sie verwerfen oder außerhalb des Callbacks
erneut senden. Der Produzent überschreibt niemals den Lesecursor.

Cursor sind getrennt ausgerichtet und werden ausschließlich über Interlocked-
Operationen mit Speicherbarrieren ausgetauscht. Sample-Zugriffe bleiben zwischen
Publikation und Freigabe des jeweiligen Bereichs. Unmögliche Cursorabstände werden
vor Bufferzugriffen abgewiesen. Die Callback-Methoden allozieren nicht, warten nicht,
loggen nicht und rufen weder Dateisystem noch Java/GUI auf. Mappingaufbau und
Page-Touching liegen vor Callback-Start. Das ersetzt keine garantierte Speicher-
Residency oder die späteren IRQL-Anforderungen eines echten Kernel-Treibers.

Lesen/Schreiben aktualisiert einen Heartbeat auf Basis von `GetTickCount64`.
Ein nicht gestarteter Teilnehmer bedeutet „nicht verbunden“. Ein explizites Close
liefert unmittelbar Stille; ohne Close wird der Gegenprozess nach zwei Sekunden
ohne Heartbeat als abgelaufen markiert. Nach Ablauf ist die Sitzung terminal:
kein Zurücksetzen laufender Cursor und keine Wiederverwendung alter Audiodaten.
Eine neue Verbindung benötigt ein frisches Mapping. Bei Pausen länger als die
Lease muss der spätere Steueradapter entweder eine neue Sitzung aufbauen oder den
Audio-Thread weiter regelmäßig bedienen.

Zuerst Callback-Nutzung beenden, dann Client schließen/zerstören, zuletzt das
Mapping freigeben. Beide Prozesse besitzen eigene Section-Handles und Views.
Wenn einer beendet wird, bleibt die View des anderen gültig. Das ist mit einem
abrupten Prozessende getestet; es ist noch kein Nachweis für Kernel-Treiber-Lebensdauer.

## Prüfung

Der Maven-Build mit `-Pnative-audio` und der PowerShell-Build führen den neuen
Transporttest automatisch neben den DSP-Tests aus. CMake registriert ihn ebenfalls
bei CTest. Der Test startet eine eigene unsichtbare Kopie als Gegenprozess und prüft
100.000 Stereoframes mit wechselnden Paketgrößen. Ein weiterer Gegenprozess beendet
sich ohne Destruktoren; nach Ablauf muss der überlebende Teilnehmer Stille liefern.
Zusätzlich: ungültiges Format, falsche ABI/Sitzung, doppelte Rollen, Über-/Unterlauf,
Wraparound, ungültige Cursor und fortbestehendes Mapping nach Ende des Erstellers.

## Nächster konkreter Schritt

Stand 2026-09-16: Ein erster EasyKnack-WaveRT-Treiber ist nun als separates
WDK-Projekt gebaut. Er verwendet bewusst eine eigene gepufferte IOCTL-Schnittstelle
mit treibereigenen, nicht auslagerbaren PCM16-Ringen; die experimentelle Float32-
Shared-Memory-ABI wird nicht in den Kernel übernommen. Das unsignierte Paket
besteht die Build-/INF-/API-/Katalogprüfung, ist aber noch nicht installiert oder
im Kernel getestet. Details und Buildanleitung: `native/experimental/virtual-audio/README.md`.

Die nächsten Schritte sind eine signierte Testinstallation, Laufzeitprüfung des
WaveRT-/IOCTL-Pfads und ein Audiohost-Worker beziehungsweise Broker. Das experimentelle
User-Mode-Layout ist weiterhin keine festgeschriebene Treiber-ABI.

Anschließend verbindet ein Provider die stabilen virtuellen Endpoint-IDs mit diesen
Laufzeitsitzungen im AudioBackend. Erst wenn ein tatsächlicher virtueller Endpoint
und ein funktionierender Routing-/Wiederherstellungsmechanismus vorhanden sind, darf
die Capability `applicationRouting` aktiviert werden. COPY ist kein Ersatz dafür.

Referenzen: [Microsoft SysVAD](https://learn.microsoft.com/en-us/windows-hardware/drivers/audio/sample-audio-drivers),
[Speicherbarrieren unter Windows](https://learn.microsoft.com/en-us/windows/win32/sync/synchronization-and-multiprocessor-issues).
