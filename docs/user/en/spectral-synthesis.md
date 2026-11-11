# Spectral synthesis and processing

Start transparent processing with `Audio → FFT → IFFT → Audio Output`. Then insert Spectral Group or Spectrum processors between FFT and IFFT. Use **synchronous FFT** for continuous reconstruction; asynchronous analysis can lose frames, causing gaps or level changes.

Bin spacing is `sample rate / FFT size`. Larger windows separate close frequencies better but span more time and increase memory and latency. Zero padding gives denser display points, not new information. Overlap determines frame spacing and processing work. FFT/IFFT adds approximately one window of latency beyond device latency.

A complex coefficient contains magnitude and phase. For gain, scale both complex components or change magnitude while retaining phase. Raw FFT values are neither automatically normalized amplitudes nor dB. Window function, channel selection, DC removal and phase change the result.

**Bin Width** spreads energy into neighboring frequencies; Bins gives linear width, octaves logarithmic width. Power normalization does not guarantee constant peaks or unchanged loudness. **Spectrum Prune** removes weaker bins. Use Bin Reader in a Spectral Group for custom redistribution and inspect the reconstructed audio too.

IFFT reconstructs complex frames with their timing and window relationship. An additive oscillator bank can use different frequencies and continuous individual states, so it is not equivalent. **Sine Cloud** is a specialized source for very many evenly spaced sinusoids; its finite period buffer repeats unchanged. Phase randomization and Gaussian frequency density are optional; individual sinusoids retain equal amplitude.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/spectral-synthesis.md)
