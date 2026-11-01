> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Level Analysis

Unter **Analysis → Level Analysis**: Audio direkt analysieren, ohne FFT.

- **Peak**: absoluter Spitzenwert des aktuellen DSP-Blocks.
- **RMS**: Wurzel des mittleren Quadrats im gleitenden RMS-Fenster (0,1–2000 ms, Standard 50 ms). Beim Start zählen nur schon empfangene Samples.
- Beide Ausgänge sind Controlrate und liefern lineare Amplituden: 0 bedeutet Stille; ein Sinus mit Amplitude 1 hat Peak 1 und RMS ungefähr 0,707. Pegel über 1 bleiben erhalten.
- **Left / Right / Mix**: Mix misst `(L + R) / 2`, nicht die Summe der Kanalenergien. Gegenphasige Signale können sich auslöschen. Mono behält seinen Pegel. Polyphone Stimmen werden vor der Messung summiert.
- **Smoothing** aktiviert getrennte Attack/Release-Hüllkurven für beide Ausgänge. Die Zeiten sind exponentielle Zeitkonstanten (etwa 63 % nach einer Zeitkonstante), 0 bedeutet sofortige Reaktion. Ohne Smoothing erscheinen die unveränderten Messwerte.
- Änderungen an Kanal oder RMS-Fenster setzen die Messhistorie zurück. Im Audiothread wird dafür kein Speicher alloziert. Nicht endliche Eingangssamples werden als Stille behandelt.

Kein LUFS und keine Frequenzgewichtung. Der Node wird wie andere Berechnungsnodes nur ausgeführt, wenn sein Ergebnis benötigt wird. Mute liefert Null; samplegenaue Feedbackschleifen werden nicht unterstützt.
