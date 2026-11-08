# Spectrum Blur

Connect an FFT spectrum to **Spectrum Blur**, then to a viewer or IFFT.
Each output bin gathers neighbouring input magnitudes using the selected kernel.

## Synthesis

- **Noise** (new-node default) shapes a continuous white-noise sequence using the
  blurred spectrum. Energy fills the spaces between bin centres over time, instead
  of producing a comb of stationary tones. Overlapping frames use the same noise
  samples, analyzed with the input window. This costs one additional FFT per frame.
- **Continuous** generates bin-centred tones with continuously advancing phases.
  Fixed, decorrelated phase offsets avoid aligning all bins into impulses.
- **Preserve** keeps destination phases and changes magnitudes only. Newly filled
  bins can cancel during IFFT overlap-add; use this with a suitable phase-processing chain.

Existing nodes with a Preserve phase setting retain their previous synthesis mode.
Choose **Noise** explicitly to try the new behaviour. Zero width always bypasses
both magnitude and phase processing exactly, regardless of synthesis mode.

Below one physical frequency cell, Noise limits the phase deviation from the
original bin, the speed of that deviation, and random magnitude fluctuations.
All three diminish towards zero with the width. This modulates the bin's phase
and magnitude directly; it does not mix a separate noise signal with the original.
The limit is frequency-dependent in octave mode and uses the physical window
resolution, so zero padding does not change the transition's frequency scale.
At one cell and above the full noise synthesis is retained. DC and Nyquist retain
their original values in the partial-depth range because these bins must be real.

Noise is uniform statistically, not in every instantaneous spectrum. Short windows
still limit frequency selectivity: very narrow bands have soft boundaries and can
sound pitched. Noise and Continuous replace the original phase relationships,
including stereo phase differences. Both channels use the same underlying noise
timeline; their spectral envelopes can differ.

## Stabilizing narrow-band noise

Use **Stability** on the **IFFT** node to reduce short loudness fluctuations in
the reconstructed audio. Start around 0.5 and increase it as needed. Zero is the
default and leaves the reconstruction unchanged, including existing documents.

The continuous RMS follower compares a 10 ms power estimate with a 500 ms
reference and smooths gain changes over 2 ms. Full correction is bounded to a
gain of 0.25–4 (approximately +/-12 dB), so deep cancellation cannot provoke
unlimited amplification. This adds no latency. It does not guarantee constant
level or impose a peak ceiling. Lower overall gain if necessary.

Stability affects all audio reconstructed by this IFFT, including intentional
dynamics. Strong correction can broaden the spectrum; stereo channels are
corrected independently. The spectrum upstream of IFFT shows the uncorrected
signal. Use an analyzer after IFFT to inspect the result.

## Kernel and power

- **Gaussian:** weighted average, strongest near the centre. The radius covers
  three standard deviations. A 32-shell approximation bounds processing cost.
- **Box:** average with equal weight per frequency-axis interval.
- **Maximum:** takes the largest neighbouring amplitude (amplitude density in
  logarithmic Noise mode). It widens peaks without averaging their height down.
- **Normalize power:** matches input spectral power, accounting for the mirrored
  real spectrum and DC/Nyquist. In Noise mode this normalizes expected power;
  individual frames still fluctuate. It does not measure perceived loudness or
  guarantee unchanged peaks or exact power after IFFT. Off by default.

## Distribution

**Octaves** is the default. Radius 1 spans half to twice the centre frequency:
at 220 Hz, the neighbourhood covers 110–440 Hz. Averaging uses log-frequency cell
widths. Noise mode works with amplitude density per octave, converting back to
power per FFT cell afterwards. A flat octave envelope therefore targets equal
power in equally wide octave intervals, rather than giving high frequencies more
power simply because they contain more bins. Windowing and boundaries affect the
realized distribution. Continuous and Preserve retain their magnitude-based behaviour.

**Bins** uses linear bin distance. **Factor (legacy)** expresses logarithmic radius
as a frequency ratio: factor 2 equals one octave. Width inputs are sampled once per
audio block. Zero width (factor 1) is an exact bypass.

DC is unchanged in logarithmic modes. At spectrum boundaries averaging uses only
available bins and renormalizes its weights. Nyquist remains real-valued.
