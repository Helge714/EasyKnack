# Digital Noise

[User guide](README.md)

Connect **Digital Noise → Frequency** to an Oscillator's Frequency input. The
node supplies a randomly changing frequency, not an audio noise waveform.
**Factor** is the same modulation as a multiplier. **Trigger** pulses at the
sample where a new random cycle is applied. Frequency equals the base Frequency
multiplied by Factor for that sample.

**Strength** is the maximum deviation in octaves: 0 gives Factor 1, 0.5 allows
half an octave in either direction, and 1 allows factors from 0.5 to 2.

Choose **Distribution**:

- **Uniform** gives equal long-term time to equal intervals of octave deviation.
- **Gaussian** favours small deviations. The target standard deviation before
  truncation is Strength / 3; values are restricted to ±Strength (three standard
  deviations). The truncation makes the resulting standard deviation slightly
  smaller. The centre is still zero octave deviation.

Each factor lasts for one self-modulated period. The draw probabilities compensate
for these unequal durations: low factors last longer but occur less often. The
symmetry refers to elapsed time, not the number of cycles. Fractional cycle time
is retained across sample boundaries. These distributions describe stationary
Frequency and Strength settings; modulation changes their timing and statistics.

To check the centre, measure the octave value `log2(Factor)` over time, or inspect
the oscillator with **Spectral Features → Frequency scale: Logarithmic**. The
mean octave deviation should approach zero over a sufficiently long acquisition.
An FFT of the oscillator also includes effects of frequency transitions, windowing
and frequency resolution; its centroid is not guaranteed to equal the base
frequency exactly. Neither the arithmetic mean frequency nor the linear spectral
centroid is constrained by the octave symmetry.

Keep the modulated oscillator frequency within the usable frequency range at the
chosen sample rate. With Strength 1 its maximum is twice the base frequency.
Strength 0 immediately passes the base frequency without drawing new random values.
Distribution changes take effect at the next random-cycle boundary.

**Seed** -1 selects an automatic seed per logical node. Matching seeds and inputs
give matching sequences across channel/instance copies. Connect Context Channel
or Instance Index to Seed for independent streams. Changing Seed restarts the
cycle. The duration-corrected generator produces different sequences from older
versions even with the same seed. Baked/exported groups use the same generator.
