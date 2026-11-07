# Stochastic Bin Width / Stochastische Bin-Breite

## Deutsch

Öffne `stochastic-bin-width.knack` als Beispiel oder importiere daraus die Gruppe
**Stochastic Bin Width**. Das Beispiel verbindet einen Oszillator über FFT,
Stochastic Bin Width und IFFT mit dem Audioausgang. Die Gruppe lässt sich auch in
einen eigenen Graphen kopieren. Es werden ausschließlich vorhandene Nodes benutzt.

- **Width:** maximaler Abstand zum Quell-Bin in FFT-Bins, also eine halbe
  Kernelbreite. Standard: 8, entsprechend höchstens ±8 Bins. Kleine Breiten sind
  durch die ganzzahlige Bin-Auswahl quantisiert. Width 0 liest denselben Bin.
- **Gaussian:** an = angenähertes Gauß-Kernel, auf ±3σ begrenzt; aus = Box-Kernel.
  Bei Gauß entspricht σ einem Drittel von Width, vor der Rundung auf ganze Bins.
- **Seed:** reproduzierbare Zufallsverteilung.
- **Vary by frame:** aus = feste Auswahl pro Ausgangs-Bin; an = neue Auswahl pro
  FFT-Frame. Neue Auslosungen können hörbare Sprünge erzeugen.

Innen berechnet **Bin Random → Formula „Kernel position“** für jeden Ausgangs-Bin
einen Quellindex. **Bin Reader** liest dort Magnitude und Phase des ausdrücklich
verbundenen Eingangsspektrums. **Formula „Phase compensation“ → Bin Output**
kompensiert bei einer festen Zuordnung den Phasenfortschritt der Verschiebung.
DC und Nyquist bleiben unverändert. Außerhalb des Spektrums wird nicht gelesen;
an den Rändern wird die Auswahl auf die vorhandenen inneren Bins begrenzt.

**Skip empty input bins bleibt aus**, damit ein leerer Ausgangs-Bin eine andere
Quelle lesen kann. Mehrere Ausgangs-Bins dürfen dieselbe Quelle wählen; andere
Quellen können unberücksichtigt bleiben. Das ist eine stochastische Abtastung,
keine bijektive Permutation und keine Leistungsnormalisierung. Es wird pro
Ausgangs-Bin ein ganzer Quell-Bin gelesen, statt dessen Inhalt über alle
Nachbar-Bins zu mitteln. Die Breite ist hier in Bins, nicht in Oktaven.

Seed und Vary by frame sind gekoppelte Editoren. Dafür gilt die bestehende
Einschränkung von EasyKnack: diesen Aufbau live verwenden; Bake/Quellcodeexport
gekoppelter Slider und Checkboxen ohne Socket ist noch nicht unterstützt.

## English

Open `stochastic-bin-width.knack` as a working example, or import its
**Stochastic Bin Width** group. The example connects an oscillator through FFT,
the group and IFFT to Audio Out. The group uses only existing nodes.

Width is a kernel radius in FFT bins (default 8, at most ±8 bins). Gaussian selects
an approximate truncated normal distribution with σ = Width/3 before rounding;
otherwise the kernel is uniform. Seed makes the selection reproducible. Vary by
frame selects new source bins for each FFT frame; off keeps the mapping fixed.
Whole source bins are read, without complex interpolation between neighbours.

The explicit Bin Reader spectrum connection and disabled Skip empty input bins
allow empty output locations to read nonempty neighbours. DC and Nyquist remain
unchanged. Edge selections are limited to available interior bins. Phase
compensation advances static mappings across successive frames.

This is random sampling: sources may be duplicated or omitted. It is not a
one-to-one permutation or power normalization. Width 0 is the identity mapping,
subject to the normal numerical precision of spectral Formula and polar output.
The copied standalone Seed/checkbox controls retain the existing live-only
restriction for Bake/source export. No new native node or host rebuild is needed.
