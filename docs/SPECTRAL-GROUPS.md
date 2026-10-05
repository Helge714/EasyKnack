> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Spectral Groups: FFT-Werte und Audio pro Bin

Eine Spectral Group verwendet die positiven Bins ihres Spectrum-Eingangs als
Instanzen: `FFT size / 2 + 1`, einschließlich DC und Nyquist. Es gibt keinen
Count-Regler. Eine normale Instance Group bleibt unverändert.

## Zwei Zeitbereiche, explizite Ausgänge

- **FFT / Bin:** Bin Context, Bin Read, Bin Random und davon abhängige numerische
  Berechnungen laufen einmal pro neuem FFT-Frame und Bin. Ihre Ergebnisse bleiben
  zwischen Frames erhalten. Instance Context liefert hier Bin-Index, Position und
  Bin-Anzahl.
- **Audio / Bin:** Oscillator, Noise und Digital Noise besitzen eigenen Zustand
  pro Bin und laufen zwischen FFT-Frames kontinuierlich weiter. Nachgeschaltete
  numerische Berechnungen laufen ebenfalls im Audiopfad.
- **Instance Combine** summiert die Audiobeiträge mit Sum, Mean oder Weighted Mean.
  Mehrere Combines und gemeinsame Verarbeitung dahinter sind möglich. Per-Bin-Werte
  dürfen diesen Übergang nicht umgehen.
- **Bin Output** erzeugt weiterhin ein Spectrum für IFFT oder weitere
  Spektralverarbeitung. Es akzeptiert FFT-Frame-Berechnungen, kein erzeugtes Audio.
  Spectrum- und Audioausgänge können in derselben Gruppe bestehen.

Die Nodes zeigen ihren Ausführungsbereich im Editor. Eine additive
Oszillatorbank ist keine identische inverse FFT: frei laufende Phasen und gehaltene
Amplituden unterscheiden sich von der komplexen, gefensterten IFFT-Rekonstruktion.

Der normale Oscillator besitzt einen Phase-Eingang (Turns, Grad oder Radiant).
Dieser addiert einen Offset zur laufenden Phase und setzt den Oszillator nicht
zurück. Bin Read.Phase liefert Radiant, bezogen auf den FFT-Fensterbeginn.
Für die direkte Verkabelung muss der Eingang daher auf Radiant gestellt werden;
das allein stellt noch keine zeitliche Synchronisation her. Eine phasentreue
Resynthese benötigt zusätzlich die Fortschreibung vom Fensterzeitpunkt zum
aktuellen Sample und kontinuierliche Übergänge zwischen Frames. Für eine exakte
Rückwandlung weiterhin IFFT verwenden.

## Bin Discard

Ein **Bin Discard** prüft seinen numerischen Eingang zuerst: 0 behält den Bin,
jeder andere Wert verwirft ihn. Ohne Verbindung dient eine Checkbox als Wert.
Beispiel: `Bin Read.Amplitude -> Comparator(< threshold) -> Bin Discard`.
Mehrere Bedingungen lassen sich vor dem einen Discard kombinieren.

Nur die für die Bedingung benötigten Nodes werden vorgezogen. Bei verworfenen
Bins entfallen die restlichen FFT-Berechnungen; deren Spectrum-Koeffizient ist 0.
Der Audiobeitrag wird über 5 ms ausgeblendet und danach nicht mehr berechnet.
Einblendung dauert ebenfalls 5 ms. Mean/Weighted Mean berücksichtigen die
aktiven Beiträge und den Übergang. Framebasierte Combine-Gewichte werden kurz
geglättet, um Amplitudensprünge zu reduzieren.

Die Bedingung darf nicht vom erzeugten Audio abhängen. Das würde die teure
Berechnung vor dem Verwerfen erforderlich machen und wird als Fehler angezeigt.
Bei inaktiven Bins pausiert Generatorzustand. Für Oszillatoren mit gehaltenem,
framebasiertem Frequenzwert wird die Phase beim Wiedereintritt rechnerisch
weitergeführt; bei zeitlicher Frequenzmodulation bleibt sie pausiert.

## Bin Read und Beispiel

Bin Read behält Real, Imaginary, Magnitude und Phase. Zusätzlich liefert es:

- **Amplitude:** mit der Fenstersumme normierter Betrag; positive innere Bins
  erhalten den Faktor 2, DC/Nyquist nicht. Das ist eine Amplitudenschätzung,
  keine Trennung überlappender Frequenzanteile.
- **Frequency:** Bin-Position mal Samplerate geteilt durch FFT-Größe.

Unter **Datei → Spektrale Resynthese öffnen …** liegt ein editierbares Beispiel:
FFT → Spectral Group → Gain → Audio Out. In der Gruppe steuert Bin Read die
Oszillatorfrequenz und das Combine-Gewicht; Comparator und Bin Discard unterdrücken
kleine Bins. Das bestehende Bin Output bleibt zusätzlich erhalten.

## Zeitzuordnung und Grenzen

Synchrone und asynchrone FFTs sind möglich. Numerische Gruppeneingänge werden
weiterhin dem Ende des FFT-Fensters zugeordnet (bei Audio das letzte Sample).
Verspätete Analyseframes verändern bereits ausgegebenes Audio nicht rückwirkend;
sie werden ab ihrer Verfügbarkeit übernommen. Eine verlorene Spectrum-Verbindung
liefert Stille.

Der Audiopfad unterstützt zunächst Oscillator, Noise, Digital Noise und die
vorhandenen skalaren Bin-Operationen. Filter, Delay, Feedback und frei verbundene
Wavetables sind in diesem Kontext noch nicht unterstützt. Normale Untergruppen
werden aufgelöst; verschachtelte eigenständige Instanzkontexte bleiben separat.

Grenzen: 16 numerische Frame-Eingänge, 7 Combine-Ausgänge, 64 Nodes im
Per-Bin-Programm mit Audiostimmen; reine Spektralprogramme weiterhin 256 Nodes.
Zustände werden vorab bis zur maximalen FFT-Größe reserviert. Audiopuffer werden
zwischen Bins wiederverwendet; vollständig ausgeblendete Bins stehen nicht mehr
in der aktiven Berechnungsliste. Bake und Export verwenden denselben nativen
Spektralkern. Eine vollständige Fusion dieser Oszillatorbank zu einem
spezialisierten C++-Loop ist noch nicht implementiert.
# Spectrum Blur and Bin Random

**Distribution: Octaves** is the musical alternative: 0 bypasses, 1 spreads one octave
down and up, 0.5 spreads half an octave each way. Its connectable Control input has no
upper editor bound. Internally the radius is 2^Octaves, capped once the entire available
spectrum is covered. **Factor (legacy)** remains available so existing documents and
connected factor signals are not reinterpreted. Switching modes exposes separate controls.

**Spectrum Prune**, outside the Spectral Group, keeps the loudest fraction of the nonnegative
bins (DC and Nyquist included). **Keep** is a connectable Control input from 0 to 1;
the count is round(Keep * (FFT size / 2 + 1)). Rank uses single-sided amplitude, with
lower bin index breaking exact ties. Surviving complex coefficients retain magnitude
and phase, discarded bins and their negative-frequency mirrors are zero. No loudness
compensation is applied. Selection happens independently for each FFT frame/channel;
fast changes in membership can sound rough. Keep 1 is an exact bypass, Keep 0 clears
the spectrum. Buffer storage is prepared before playback; partial selection avoids a
full sort. Delayed asynchronous frames use the current block's Keep value.

**Distribution: Factor** exposes a connectable Control input **Factor**, with default/minimum 1
and no upper slider limit. Connected values above 1 are accepted; values below 1 behave as 1.
Each nonzero bin k spreads to bin centres within [k / Factor, k * Factor], clipped to Nyquist.
Destinations receive power proportional to 1/k (the discrete logarithmic density), normalized
per source bin. Thus Factor 2 spans one octave down/up. The discrete FFT grid limits the precision
at low bins and produces steps when the destination range changes. DC is preserved independently.
The original Bins mode and old documents keep their linear-radius behaviour.

`FFT -> Spectrum Blur -> Spectral Group -> IFFT` broadens spectral power before per-bin processing.
Spectrum Blur's Control input **Width (bins)** is an integer radius: 1 distributes each bin's
power uniformly across itself and its two neighbours. The radius is clamped to the spectrum.
At the edges the available destinations are renormalized. DC/Nyquist multiplicities are included
so the full complex spectrum's summed squared magnitude is preserved. Width 0 copies exactly.
Destination phases are retained (zero coefficients use phase zero); this node does not itself
randomize phases. Spectral power preservation does not guarantee equal perceived loudness or
identical overlap-add output energy. Processing is linear in bin count, without audio-thread allocation.
The width is sampled from the current audio block even for delayed asynchronous FFT results.

Bin Random now has **Vary by bin** and **Vary by frame** checkboxes. Both off means a constant
seed-dependent value; bin only means a fixed value per bin; frame only means one shared value
per FFT window; both on means independent values per bin and FFT window. Values are in [0,1).
The old `fixed` and `frame` modes retain their behaviour (bin only / both on). No temporal smoothing
is implied by the frame option.

## Empty bins and execution cost

Group-General now offers **Skip empty input bins** (off by default, saved in the
document). With this enabled, an exactly zero complex input coefficient skips
the entire bin program, including the Discard condition. Thus zeroed bins from
Spectrum Prune are cheap in the following group. Existing per-bin audio voices
fade out using the usual discard behavior. A nonzero bin is never skipped merely
because its magnitude is small.

Keep this option disabled for programs which create new coefficients or use Bin
Read to populate an empty bin from another bin. Zero input does not imply zero
output for an arbitrary program. Bin Discard remains available for custom rules.

The live runtime resolves processor types and scalar contexts during graph
preparation. It prepares separate condition/value schedules and evaluates
bin-independent expressions once per FFT frame. Bin values are independent
evaluations, not successive audio samples: fixed parameter values are not
smoothed from one bin to the next.

Bake and plugin export now generate one typed bin loop for static frame programs:
Math, Comparator, Map Range, Mix, Clamp, Curve, Unit Converter, Value and the bin
processors. The emitted code shares the native node implementations. External
numeric values still update per frame. Programs selected by dynamic editor choices
and programs containing temporal audio voices retain the prepared runtime fallback.
Previously saved bakes need a new bake to receive the generated loop.

Measurements and the parallel-execution experiment: [SPECTRAL-PERFORMANCE.md](SPECTRAL-PERFORMANCE.md).

## Parallel bin execution

**Group-General → Parallel bins (automatic)** is enabled by default and saved with
the group. Pure frame programs can split their independent bins between the audio
thread and one persistent shared worker. Each side owns its processors, temporary
values and Bin Output state; the input frame is read-only and output ranges do not
overlap. Uniform inputs are captured on the audio thread, then copied into the
worker's frame context. Symmetry restoration happens after both ranges finish.

There is one helper for the process, not one thread per group/bin. Preparation
allocates its independent scalar state before playback. A busy helper means
immediate serial execution. Small workloads remain serial: the current threshold
is at least 512 nonnegative bins and 8192 bin-step evaluations per frame. Thus a
512-point FFT stays serial. Turning the setting off forces serial execution.

Temporal per-bin audio voices, shared effects, feedback islands and arbitrary
stereo graph nodes are not parallelized by this mechanism. Their state and timing
continue to use the existing scheduler. Bake generates the same range kernel for
the main processor and its worker copy. No independent random seed or separate
frame timing is introduced by splitting the range.
