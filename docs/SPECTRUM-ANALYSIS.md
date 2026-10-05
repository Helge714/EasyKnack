> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Spectrum analysis

New graphs use **Audio → FFT → Analysis Viewer / Spectrum Statistics**. No Buffer
or Analysis View node is needed. Both consumers select Magnitude, Phase or Power
independently and share the same FFT result. Analysis Viewer defaults to Auto and
also displays **FFT → Autocorrelation → Analysis Viewer**, with a mirrored lag axis.
Autocorrelation consumes the existing complex spectrum and computes `IFFT(|X|²)`
on the analysis worker. Additional viewers share that result. The optional
**Autocorrelation → Pitch Detection** branch supplies frequency and confidence;
pitch bounds belong to Pitch Detection. The FFT does not run a pitch detector.

FFT **Zero padding → Linear correlation (2x)** uses an FFT twice the input-window
length, giving linear correlation for all lags 0 through N−1. Without padding,
correlation is circular. Padding does not improve actual frequency resolution.
For pitch detection, start with Rectangular, 2x padding and a window covering
several periods of the lowest expected pitch. Correlation is normalized by lag
zero, silence produces zero, and DC is retained by default. This is correlation of the
windowed signal, not the old DC-centered, unbiased estimator. Pitch Detection
selects the earliest strong local peak with parabolic interpolation; harmonic
signals can still produce octave ambiguity. Invalid pitch bounds produce zero.

To reproduce the legacy curve, select **Rectangular**, **8192 samples**,
**Linear correlation (2x)**, **Remove DC** in FFT and **Unbiased** in
Autocorrelation. Remove DC subtracts the input-window mean before windowing.
Unbiased multiplies each linear-correlation lag by `N/(N-lag)` and clamps to
±1, matching the legacy implementation. Circular correlation has constant overlap
and receives no correction. This does not undo Hann/Hamming/Blackman tapering;
large-lag estimates remain noisy because few sample pairs contribute.
Defaults remain Remove DC off / Biased for existing documents and source plugins.

Autocorrelation **Smoothing factor** blends complete curves on the worker:
`old * (1-factor) + new * factor`. Default 1 is immediate; 0 holds the first
curve. Initialize from the first snapshot, reset when the source FFT configuration
or normalization changes, and keep the accumulated curve on factor-only edits.
The factor applies once per received analysis frame, so overlap and skipped frames
affect its response time. Viewers and Pitch Detection consume the same smoothed result.

Double-click an analysis plot or choose **Open enlarged graph** from its context
menu to view it across the editor area. **Back to nodes**, Escape or View → Back
returns to the existing editor/group view. The large plot shares snapshots,
vertical scale and trigger visibility with its source; it does not create another
analyzer or change the graph. This is available for Oscilloscope and Analysis
Viewer, including legacy analysis plots.

The FFT owns a preallocated audio ring, sums input voices and analyzes the selected
left, right or **Mix** channel. Mix is `(Left + Right) / 2`, preserving mono level;
opposite-phase stereo content can cancel. Window sizes are 256, 512, 1024, 2048, 4096 and 8192 samples.
Available periodic windows: Hann (default), Hamming, Blackman and Rectangular.
Overlap is a 0–1 slider (default 0.75), with percent available through its unit menu.
The hop is `max(1, round(N * (1 - overlap)))`; finer values are supported, including
non-power-of-two hops. The tooltip reports the actual hop in samples. Greater
overlap increases update frequency and CPU cost, not frequency resolution.
The first frame
ends at sample N, then each hop advances its absolute sample position. Configuration
changes to window size/type or channel restart the window; changing only overlap
retains buffered audio and updates parameters live without allocating or rebuilding
the graph. Snapshots from previous settings are rejected using their source identity.
Unchanged graph replacements adopt existing state. Old overlap preset strings and
their embedded native contracts remain readable.

Work is asynchronous. The audio thread copies complete windows into bounded,
preallocated job slots without waiting. A busy worker may drop snapshots. Analysis
consumers hold the latest complete result; this is not a lossless audio-rate STFT
pipeline. Separate real-time scheduling and synthesis remain prerequisites for an
IFFT audio node.

## Spectrum frame contract

`SpectrumFrame` retains **all M complex bins**, including DC, Nyquist and negative
frequencies. The forward transform is unnormalized; the inverse is normalized by
1/M. M is N without padding and 2N with padding (up to 16384 bins). Samples are multiplied by the selected periodic window, without removing DC,
centering, rotating or discarding phase. The phase origin is `startSample`.

Metadata includes FFT size, window size/type, hop size, sample rate, source identity,
start and exclusive end sample, frame index, window sum and sum of squared weights.
A gap in frame indices identifies skipped windows. This allows a future inverse
processor to reconstruct the windowed waveform and apply a synthesis window with
weighted overlap-add. Raw-bin inverse and weighted overlap-add are numerically
tested; an IFFT node and its real-time stream are not implemented yet.

Display/Statistics use the M/2+1 nonnegative-frequency bins, including Nyquist.
Magnitude divides by the window sum and doubles interior bins, so an isolated
bin-centered unit cosine has magnitude one. DC and Nyquist are not doubled. Phase
is in radians; Power is squared displayed amplitude, not a power spectral density.
Frequency spacing is sampleRate/M. Statistics aggregate these displayed bins:
absolute peak, arithmetic mean, RMS and rectangular bin sum times spacing. Spectral
RMS must not be confused with time-domain audio RMS.

## Compatibility

The former FFT/Buffer/View chain and audio-input Autocorrelation remain registered under their original type IDs,
native processor names and parameter contracts. Its nodes are named Legacy and
hidden from the add-node catalog. Existing documents and source plugins retain
their previous behavior; no wires or shared buffers are removed automatically.
The new nodes use independent IDs (`spectrum`, `spectrumviewer`,
`spectrumstatistics`, `spectrumcorrelation`, `pitchdetection`), so old generated C++ plans also remain valid.
Both earlier Spectrum parameter contracts (preset and numeric overlap without
padding) are explicitly accepted and retain their unpadded behavior.
