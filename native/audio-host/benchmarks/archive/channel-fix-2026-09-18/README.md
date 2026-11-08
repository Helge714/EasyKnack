# Korrektur der Kanalbehandlung im Export

Die Ursache des annähernden Faktors zwei ist bestätigt: `BakedInput` meldete MIDI als
Stereo, wodurch der innere Synth zweimal berechnet wurde. Zusätzlich meldete der
Wrapper `BakedGraphProcessor` auch reine Mono-Ausgänge als Stereo; nachfolgende
Mono-verträgliche Nodes verloren dadurch ebenfalls ihre Kanalwiederverwendung.

Korrektur: MIDI-Grenzen teilen ihre Ereignisse zwischen den Kanälen. Der Wrapper
übernimmt die beim Kompilieren ermittelte Kanalbelegung seiner öffentlichen Ausgänge.
Numerische Gruppeneingänge bleiben konservativ Stereo, bis ihre tatsächliche äußere
Kanalbelegung an den inneren Plan übergeben werden kann. Kontext, Pan und echte
Stereo-Signale dürfen nicht auf Mono reduziert werden.

Der Regressionstest fordert für die Referenz-Synths einen Mono-Plan innerhalb und
außerhalb des Exports sowie weiterhin Stereo für das Kontextprojekt. Er scheiterte
vor der Korrektur. Danach blieben alle 28 Audiovergleiche bitgleich, einschließlich
Mute/Parameter-/Graphwechseln und exakten MIDI-Delay-Ereignissen.

Neue Messung, 256 Frames/48 kHz, Median dreier Mittelwerte in µs:

| Projekt | Original | Export |
|---|---:|---:|
| Synth, 1 Stimme | 55,07 | 58,10 |
| Synth, 8 Stimmen | 434,62 | 482,28 |
| Synth, 32 Stimmen | 1664,39 | 1963,14 |
| Stereo-Kontext | 14,77 | 14,80 |
| Analyse | 26,63 | 27,17 |

Beim Acht-Stimmen-Export sind das etwa 11 % Restaufwand gegenüber dem Original,
statt zuvor etwa 100 %. Die Messungen liegen in unterschiedlichen Durchläufen;
die jeweilige Original-/Export-Relation ist aussagekräftiger als absolute Vorher-/Nachher-Zahlen.
Der Samplebuffer-Speicher dieses Exports sank von 334 auf 265 KiB. Verbleibende
Scheduler-, Kopier- und Bufferkosten sind noch nicht entfernt. Keine pauschale
Echtzeitgarantie aus den Mittelwerten ableiten; p99 und Maxima stehen im [Messbericht](results.md).

Rechner und Compiler wie in [der Ausgangsbaseline](../reference-2026-09-18/environment.json):
i5-1135G7, Windows, Energieprofil Ausbalanciert, Clang 20.1.0, `-std=c++17 -O2 -static`.
Messung vor dem vollständigen Build, ohne parallelen Build. Rohdaten: `offline.csv`
und `equivalence.csv`. Es wurde kein neuer Geräte-Langlauf für diese Korrektur ausgeführt.

- SHA256 `BakedGraph.hpp`: `4E6B677212B141AA00999B777230F13E02E8D1A306A4BAA201656701350A3AFB`
- SHA256 Messprogramm: `744382E86E2FA640641278ED377BD3597434FFCB9FA3B9941EF4D197CF942F11`

Die Korrektur sitzt im gemeinsamen nativen Plugin-SDK. Bestehende Quellplugins
profitieren beim Neubau; ein erneuter Gruppenexport ist nicht nötig. Ein geändertes
SDK invalidiert beim Programmstart den bisherigen Build-Cache automatisch.

Abschluss: vollständiges `mvn -Pnative-audio verify` mit Audio-, GL-, Geräte- und
Referenztests bestanden: 129 Tests ohne Fehler oder Auslassungen, außerdem sämtliche
nativen DSP-/MIDI-/WASAPI-/ASIO-/Transporttests. Host, Anwendung und Plugin-SDK neu gebaut.
Log: `target/channel-fix-verify.log`.
