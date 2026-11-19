# EasyKnack – Linux-Testpaket

Für Linux x86_64 (Intel/AMD), mit gebündeltem Java-JDK und C++-Compiler. Das Paket wurde unter
Windows für Linux kompiliert. Es ist noch kein unter Linux geprüfter Release.

1. Das gesamte `.tar.gz` entpacken, beispielsweise mit dem Archivprogramm.
2. Im entpackten Ordner `EasyKnack` ein Terminal öffnen.
3. `./Check-Linux.sh` ausführen. Der erste Compiler-Test kann mehrere Minuten
   dauern; Fortschritt und Ergebnisse stehen im Terminal. Die Datei
   `linux-diagnostics.txt` bitte weitergeben.
4. `./Start-EasyKnack.sh` ausführen. Zum Mitschreiben von Startfehlern:
   `./Start-EasyKnack.sh > linux-start.txt 2>&1`.

Es wird nichts installiert. Eine grafische Linux-Sitzung mit X11 beziehungsweise
XWayland, funktionierendem OpenGL 3 und den üblichen X11-/OpenGL-/Fontbibliotheken
wird benötigt. Der Audiohost benötigt glibc 2.28 oder neuer; Java und Grafiktreiber
können zusätzliche Systemanforderungen stellen. Alpine/musl und ARM werden von
diesem Paket nicht unterstützt. Nicht von einem `noexec`-Datenträger starten:
gegebenenfalls den Ordner auf die lokale Festplatte kopieren.

Für Geräteauswahl, mehrere Ein-/Ausgänge, System-/Anwendungsaufnahme und
Gerätelautstärke wird PulseAudio oder PipeWire mit PulseAudio-Kompatibilität
(`libpulse.so.0` und laufender Benutzer-Audiodienst) benötigt. Hardware- und
virtuelles MIDI verwenden ALSA (`libasound.so.2` und den Kernel-Sequencer).
Ohne Desktop-Audiodienst bleibt nur der Standardgeräte-Fallback verfügbar; das
erscheint im Startprotokoll. MP3-Aufnahme verwendet den enthaltenen LAME-Encoder.
Windows-ASIO-Treiber werden unter Linux nicht geladen; die Audiofunktionen nutzen
die Linux-Schnittstellen. Verknüpfte Lautstärketasten folgen unter Linux Änderungen
der Desktop-Standardlautstärke, auch Änderungen im Lautstärkemenü. Das ist kein
globaler Windows-Tastaturhook. MIDI-Geräte und Audioquellen gegebenenfalls neu wählen,
wenn das Dokument Windows-Gerätekennungen gespeichert hat.
Die Beispiele sind im Menü verfügbar und liegen außerdem unter `Examples`.

Zum Testen: einfachen Oszillator mit Audio Output verbinden, abspielen, stoppen,
eine Beispieldatei öffnen und Tastatur-MIDI ausprobieren. Bei Problemen bitte
`linux-diagnostics.txt`, gegebenenfalls `linux-start.txt` und eine kurze Beschreibung
mitschicken. Der automatische Test benötigt keine Audiogeräte und keine Anzeige;
sein Protokolltest benötigt optional `python3`.

JDK mit `javac`, der C++-Compiler sowie die MP3-Bibliothek sind enthalten;
Formula-Kompilierung, Bake und Quellcode-Plugins benötigen keine zusätzliche
Compilerinstallation. Einen eigenen JDK
kann man mit `EASYKNACK_JAVA=/pfad/zum/jdk/bin/java ./Start-EasyKnack.sh` verwenden.
Formeln haben außerdem weiterhin den Interpreter als Rückfallpfad.

Lizenzinformationen liegen in `LICENSE`, `THIRD-PARTY.md`, `licenses` und
`runtime/legal`; die EasyKnack-Quellen und das externe GLSwing-Quellarchiv unter
`sources`, ebenso die passenden OpenJDK-Quellen zur enthaltenen Java-Laufzeit.
Weitere Quellenlinks enthält `THIRD-PARTY.md`.
