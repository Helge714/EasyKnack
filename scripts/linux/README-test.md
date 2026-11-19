# EasyKnack – Linux-Testpaket

Für Linux x86_64 (Intel/AMD), mit gebündelter Java-Laufzeit. Das Paket wurde unter
Windows für Linux kompiliert. Es ist noch kein unter Linux geprüfter Release.

1. Das gesamte `.tar.gz` entpacken, beispielsweise mit dem Archivprogramm.
2. Im entpackten Ordner `EasyKnack` ein Terminal öffnen.
3. `./Check-Linux.sh` ausführen. Die Datei `linux-diagnostics.txt` bitte weitergeben.
4. `./Start-EasyKnack.sh` ausführen. Zum Mitschreiben von Startfehlern:
   `./Start-EasyKnack.sh > linux-start.txt 2>&1`.

Es wird nichts installiert. Eine grafische Linux-Sitzung mit X11 beziehungsweise
XWayland, funktionierendem OpenGL 3 und den üblichen X11-/OpenGL-/Fontbibliotheken
wird benötigt. Der Audiohost benötigt glibc 2.28 oder neuer; Java und Grafiktreiber
können zusätzliche Systemanforderungen stellen. Alpine/musl und ARM werden von
diesem Paket nicht unterstützt. Nicht von einem `noexec`-Datenträger starten:
gegebenenfalls den Ordner auf die lokale Festplatte kopieren.

Zunächst Standard-Audioausgang, gegebenenfalls Standard-Audioeingang und
Computer-Tastatur-MIDI testen. Hardware-MIDI, mehrere Audiogeräte, System- und
Prozessaufnahme sowie MP3-Aufnahme sind im portablen Backend noch nicht verfügbar.
Die Beispiele sind im Menü verfügbar und liegen außerdem unter `Examples`.

Zum Testen: einfachen Oszillator mit Audio Output verbinden, abspielen, stoppen,
eine Beispieldatei öffnen und Tastatur-MIDI ausprobieren. Bei Problemen bitte
`linux-diagnostics.txt`, gegebenenfalls `linux-start.txt` und eine kurze Beschreibung
mitschicken. Der automatische Test benötigt keine Audiogeräte und keine Anzeige;
sein Protokolltest benötigt optional `python3`.

Die Laufzeit ist enthalten; Quellcode-Plugins benötigen zusätzlich einen Linux-JDK
mit `javac` und einen C++-Compiler (z. B. `g++` oder `clang++`). Einen eigenen JDK
kann man mit `EASYKNACK_JAVA=/pfad/zum/jdk/bin/java ./Start-EasyKnack.sh` verwenden.
Formeln funktionieren auch ohne installierten Compiler über den Interpreter.

Lizenzinformationen liegen in `LICENSE`, `THIRD-PARTY.md`, `licenses` und
`runtime/legal`; die EasyKnack-Quellen und das externe GLSwing-Quellarchiv unter
`sources`, ebenso die passenden OpenJDK-Quellen zur enthaltenen Java-Laufzeit.
Weitere Quellenlinks enthält `THIRD-PARTY.md`.
