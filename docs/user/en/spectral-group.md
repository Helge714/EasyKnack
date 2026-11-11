# Spectral Group

A Spectral Group processes the bins of an incoming Spectrum. The Spectrum input determines bin count and frame times. **Bin Context** provides real/imaginary components, magnitude, phase, frequency and index; **Bin Reader** reads other bins from an explicitly connected spectrum.

For gain, connect `Magnitude * factor` to Bin Output and preserve Phase. **Bin Output** writes complex frame values in Real/Imaginary or Magnitude/Phase mode. Multiple Bin Outputs create independent spectra. Two complementary masks `m` and `1-m` applied to the original complex values sum back to the input spectrum.

**Bin Discard** discards a bin for every output. **Skip empty input bins** skips exactly zero input coefficients before processing: disable it when creating bins or reading other bins. **Parallel bins** can parallelize suitable independent processing; small programs or per-bin audio remain serial.

External numeric inputs are sampled at the window end per frame and shared across its bins. Frame triggers belong to the corresponding frame. Bin Random, Bin Sample & Hold and Bin Smoothing support per-bin randomness, holding and smoothing.

Frame processing runs per bin and frame. Oscillators inside the group instead generate continuous audio per bin; use **Instance Merge** for that output. Bin Output does not accept this audio. An oscillator bank driven by magnitudes is not a phase-equivalent IFFT.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/spectral-group.md)
