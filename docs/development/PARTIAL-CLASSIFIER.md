# Adaptive partial peak classifier

**Reproduction status:** this implementation uses our own quantile tables and is
not the requested reproduction of the paper's deterministic finite-SNR threshold
construction. Replacing those tables is pending the source clarifications recorded
in [ZRR07-SOURCE-AUDIT.md](ZRR07-SOURCE-AUDIT.md). Do not describe the current
implementation as identical to the literature example.

## Model and controls

Partial Analysis implements the normalized-bandwidth (NBD), normalized-duration
(NDD) and frequency-coherence (FCD) descriptors and two-stage decision regions
described by Zivanovic, Roebel and Rodet,
[Adaptive Threshold Determination for Spectral Peak Classification, DAFx 2007](https://www.dafx.de/paper-archive/2007/Papers/p047.pdf).

`Classify peaks` enables classification; disabling it exposes general sinusoidal
peak tracking without a noise classifier. `Minimum peak SNR` is the assumed
peak-to-noise contrast used to select bounds, 5-60 dB, default 30 dB. It is not an
automatically measured SNR or a promise about residual purity. Increasing it
tightens the decision region. The amplitude threshold and minimum track duration
remain independent controls.

The implementation follows the experimental descriptor-distribution approach in
sections 2-4. It does **not** reproduce the polynomial approximations to noise
patterns in sections 5-6 or claim identical results to the paper's experiments.
Our tables are calibrated for the actual discrete bin convention and supported
1x/2x FFT padding, including stationary sinusoids. This is a numerical realization
of that model, not a renamed window-correlation or phase-confidence heuristic.

## Descriptor conventions

Let X be the raw DFT of x times the periodic window w, M the window length and N
the FFT size. A peak's region runs between adjacent local power minima, inclusive;
L is its number of bins. All moment sums below use that region.

- P(k) = |X(k)|^2, E = sum(P), kbar = sum(k P)/E.
- NBD = sqrt(sum((k-kbar)^2 P)/E) / L.
- Xt is the DFT of (n-M/2) x(n) w(n). Let tbar =
  real(sum(Xt conjugate(X)))/E. Then NDD =
  sqrt(sum(|Xt|^2)/E - tbar^2) / M, with a zero floor for roundoff.
  This is the moment form of equation 5; centering time avoids cancellation.
- Xdt is the DFT of x(n) w'(n), using the analytic derivative of the raised-cosine
  window. FCD = N/(2 pi) min(abs(imag(Xdt conjugate(X))/P)). Bins below 1e-12
  times the region's total power are excluded from this division.

FCD >= N/M marks a sidelobe. Otherwise NBD must not exceed its upper bound and
NDD must fall between its two bounds. Nonfinite/empty regions fail classification.

`PartialClassifier` recovers the windowed samples by inverse FFT and computes the
two auxiliary transforms from them. For smooth windows, x w' is obtained as
(x w)(w'/w); the ratio is zero where w <= 1e-12. It is bounded near the periodic
cosine-window zeros and is verified against transforms constructed directly from
the original samples. There is no extrapolation of missing original samples.
All current FFT DC removal subtracts the mean before windowing and therefore
preserves this convention.

Rectangular frames are reanalysed with Hann for classification, because this
smooth-window derivative model does not cover rectangular edge impulses.
Interpolation, complex lobe fitting, residual subtraction and output metadata
still use the original frame and window.

Classification uses the original spectrum, not a progressively modified noise
distribution. Accepted candidates are then fitted in descending amplitude order
in the existing scratch spectrum before tracking. The residual loses only
confirmed tracks. Tracking retains its centre-referenced phases and causal
minimum duration/gap handling; the removed EWMA confidence gate is not retained.

The Context output with stable ID `tonality` is now labelled Classification margin.
It is the minimum of the normalized distances to the NBD, both NDD and FCD
boundaries, clamped to 0-1. NDD distances are normalized by half the permitted
interval. The value is diagnostic and does not gate tracks a second time.
It is not a calibrated probability. With classification disabled its value is 1.

## Reproduce the calibration

Use the optional environment described in [PARTIAL-REFERENCE.md](PARTIAL-REFERENCE.md):

```powershell
target/partial-reference-env/Scripts/python.exe scripts/partial_classifier_calibration.py --header native/audio-host/src/PartialClassifierCalibration.hpp
```

This explicitly replaces the small embedded numeric table. Ordinary builds and
application starts never calibrate or install Python dependencies. Generated
diagnostics and provenance go under `target/partial-classifier-calibration/`.

The generator evaluates equation 11 with AM depth 0.5, FM index 10 radians,
FM frequency 1/(4.2 M), AM frequency twice that, plus stationary carriers. It uses
M=1024, three periodic windows, both padding factors, eight AM/FM phases each,
three fractional-bin positions, three independent Gaussian-noise realizations
and nine peak-SNR levels: 5, 10, 15, 20, 25, 30, 40, 50 and 60 dB. Seed: 2007047.
Noise variance is scaled so the clean dominant peak power divided by expected
noise-bin power equals the prescribed peak SNR.

The upper NBD bound is its 99.5th percentile; the NDD interval uses the 0.25th and
99.75th percentiles. A monotone envelope ensures lower assumed SNR never tightens
the decision region. Runtime bounds interpolate linearly between SNR samples.
These finite calibration quantiles are engineering choices, not a universal
false-positive guarantee. They are documented rather than hidden as magic scores.

The generator uses NumPy only. Pinned versions for the shared reference environment
live in `scripts/requirements-partial-reference.txt`. The JSON manifest records
the generator hash, seed, model, versions, quantiles and all resulting bounds.
The embedded header is intentional source data; no calibration recordings or
executable reference code are bundled.

## Validation and limits

`PartialClassifierTest.cpp` checks analytic time-domain/DFT parity for all four
windows, both padding factors and lengths 256/1024/4096/16384, SNR monotonicity,
allocation-free operation and independent AM/FM/noise samples. On the recorded
run, all 192 held-out modulated sinusoids at 35 dB peak SNR passed the default
30 dB classification. 41 of 29081 Gaussian-noise peaks passed. These counts are
fixture-specific, not a guarantee for music.

The full eight-window comparison at default 30 dB retains all eight stationary
110 Hz-spaced components from 2048 samples upward. The four-component vibrato
fixture at 8192 improves from the prior window-fit classifier's 87.7% recall to
100%; pure-noise residual power there is 99.895%. At 2048, vibrato recall is 98.3%
and the noisy-tone fixture reaches 99.1%, rather than a universal 100%. These
trade-offs remain visible in the report. At 65536, noise can still produce roughly
18 accepted tracks per frame while contributing under 0.4% of its power to the
partial model. A low per-peak acceptance rate does not imply zero false tracks in
a large spectrum, and a long modulated frame can contain genuine FM sidebands.

`PartialsTest` also exercises full native reconstruction, tracking, prepared
capacities and embedded legacy parameter contracts. `PartialAnalysisSettingsTest`
covers the new controls, retired parameters, raw-analysis preservation and copied
group-editor detachment without invalid composite properties. The broader
[SMS comparison](PARTIAL-REFERENCE.md) retains short-window, long-window and
modulated counterexamples. Use `--wav` for local recordings; no ground-truth
separation score can be inferred for an arbitrary recording.

Descriptors require three auxiliary transforms per classified frame (inverse FFT,
time-moment FFT, derivative-window FFT), plus a Hann reanalysis for rectangular
frames. Memory is reserved during preparation and processing allocates nothing.
Large windows/voice counts increase CPU and prepared memory use. The model assumes
a dominant, locally sinusoidal lobe; unresolved neighbours and strong within-window
modulation remain fundamental limitations. Higher FFT resolution cannot remove
the time/frequency trade-off.

## Compatibility

The removed sinusoidality, prominence, assessment-time and tonality-threshold
settings have no meaningful physical conversion to SNR. Loading old documents
uses 30 dB, retains explicitly raw analysis, removes obsolete properties and
detaches copied bindings to them with a log warning. Signal wires and unrelated
settings remain. Existing encoded legacy commands are accepted and use the same
new policy. The Java/native parameter fingerprint changes; rebuild them together.
No document schema, plugin binary ABI or host transport version is changed.
