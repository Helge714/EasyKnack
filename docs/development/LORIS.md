# Loris partial analysis integration

## Source and build

The host compiles the original analysis subset from
[Kelly Fitz's Loris repository](https://github.com/kellyfitz/loris), pinned at
`eccd42ac847b08769643b0d0c36442ef0fd27f38`. Sources, licenses, authors and file
hashes are under `native/audio-host/third_party/loris`. The two local header
patches only grant access to the streaming bridge; analysis formulas are intact.
The library is GPL-2.0-or-later, used under the GPLv3 option alongside EasyKnack.
The bundled Ooura FFT permission notice is retained. FFTW, Python and SWIG are
not runtime or build dependencies.

`src/nodes/LorisLibrary.cpp` compiles the analysis dependency subset as one
translation unit. It renames file-local constants and one private helper for
unity-build compatibility and supplies M_PI on Windows. Existing host scripts
and plugin builds already enumerate node `.cpp` sources; both therefore link
this same implementation. SDKs retain its source and notices. Native test helpers
link the same unit. Windows packages and the application JAR carry the notices.

## Streaming bridge

`LorisStreamImplementation.hpp` follows `Analyzer::analyze`: Kaiser window and
derivative, `ReassignedSpectrum`, `SpectralPeakSelector`, the original
`Analyzer::thinPeaks` and `fixBandwidth`, optional `AssociateBandwidth`, and
`PartialBuilder`. Neither the old NBD/NDD/FCD classifier nor EasyKnack's former
predictive/harmonic tracking participates.

The input Spectrum contains a windowed STFT, not raw audio. The bridge inverse
transforms and removes that analysis window before applying Loris' own Kaiser
window. Its odd window uses samples 1 through M−1, centred at M/2. The zero
endpoint of periodic Hann/Blackman is excluded rather than divided by zero.
This cannot recover information destroyed by preceding spectral effects; those
inputs are analyzed as their reconstructed local sample section.

The source FFT fixes available duration, hop and timestamps. At 90 dB Kaiser
sidelobes the window-width parameter is chosen for M−1 samples; it is independent
of the adjustable minimum frequency separation. Resolution 0 selects 2*rate/M,
and drift 0 selects half the resolution. Crop time is the reciprocal Kaiser
window width. The default bandwidth mode is Loris convergence, tolerance 0.1;
residue association and no-bandwidth modes are also available. These are explicit
EasyKnack streaming defaults, not a claim to reproduce every offline default.

## Causality, state and real-time limits

The unmodified offline Analyzer collects an entire PartialList, constructs
envelopes and optionally corrects phases/frequencies after analysis. There is no
end-of-file in the live graph. The bridge keeps the original PartialBuilder
across frames, retaining only active tracks and their final two breakpoints.
Inactive tracks are reclaimed, independent of program uptime. Separate birth
times preserve age; monotonic IDs and reusable slots preserve group state.
The two final points permit local interpolation/extrapolation. No retrospective
phase correction, global envelope estimation or semantic classification is run.

Detected amplitude includes Loris' bandwidth-enhanced energy. The emitted
sinusoid has amplitude A*sqrt(1−bandwidth); Context Tonality is 1−bandwidth.
Lobe synthesis uses phase at the STFT window start. Subtracting those same
complex lobes leaves an exact residual, so unchanged resynthesis remains an
identity even when the sinusoidal model is imperfect. This does not establish
perceptually correct separation.

Minimum duration is an additional causal admission delay. Capacity caps peaks
after upstream amplitude-ordered thinning. There is no gap-filling prediction or
common-fundamental restriction. Changing analysis settings resets the model;
IDs are not reused. Structural capacity changes prepare new resources.

`LorisWorker.hpp` is a prepared per-analyzer helper with one synchronous
rendezvous, no queue or frame dropping. Loris allocations and deallocations run
on that thread; callback-owned outputs are preallocated. The rendering thread
waits, so the work and scheduling overhead still count against the DSP deadline.
This is not a free asynchronous analysis engine. Many nested instances create
many helpers, and very large capacities/windows can exceed real-time budgets.
Worker failures are logged and leave an empty Partials frame plus the original
residual. No exception escapes the rendering callback.

## Verification

`native/audio-host/tests/LorisAnalysisTest.cpp` compares stationary 440/997 Hz
components against the **unmodified upstream Analyzer** in all three bandwidth
modes, with matching Kaiser length/hop, and offline phase correction disabled.
The known-signal comparison checks frequency, amplitude, stable identity and
range validity. It also tests 300 simultaneous partials and graph capacity
propagation. `PartialsTest.cpp` checks complex reconstruction and IFFT output
across all four supported source windows and both FFT padding factors. Both
instrument callback-thread allocations; worker allocations are expected.

`PartialAnalysisSettingsTest` covers controls, retired-property migration and
copied group bindings. `scripts/partial-reference.py` remains an optional
synthetic/recording comparison with SMS Tools; see
[PARTIAL-REFERENCE.md](PARTIAL-REFERENCE.md). Real recordings have no assumed
ground-truth labels. Numerical agreement on stationary fixtures does not prove
equivalence of offline and causal processing for transients or missing frames.

## Compatibility and versioning

The node identity and Partials resource layout remain intact. The native
parameter contract changes; rebuild Java/host together and invalidate/rebuild
generated plugins and baked artifacts through the SDK cache key. The three
previous native parameter-string contracts are consumed for already exported
plugins; their retired controls are ignored and execution still uses Loris.
Documents lose
retired classifier/harmonic settings, related fundamental connections and copied
bindings with a warning. Retained amplitude floor, ranges, duration and capacity
do not guarantee the same separation result. The existing 0.0.1-SNAPSHOT product
version remains pending the first release baseline; this behavior change is
recorded in release notes. No protocol-wide or document-format bump is needed.

## Measured validation (2026-11-07)

clang 20.1, C++17, -O2, Windows x64: the unmodified-Analyzer comparison reports
maximum frequency difference 3.99e-10 Hz and amplitude difference 2.09e-10 on
the stationary two-tone fixture across all three bandwidth modes. Capacity,
reconstruction and callback-allocation tests pass, including 300 active tracks.
The optional SMS harness completed 48 cases at 2048/8192 samples, including a
private 48 kHz recording; 32 native cases had maximum complex reconstruction
error 4.27e-14. Neither the recording nor its derived audio is distributed.
No perceptual ground truth or perfect noise separation is inferred from this.

The actual null-device host accepts the rebuilt contract, plays a full
FFT → Loris → synthesis/residual sum → IFFT graph, switches all bandwidth modes
live, stops and quits without stderr or connection loss. The full Maven suite
passed (682 tests, 170 native/platform tests skipped); the focused enabled
Partials/Settings suite separately passed 7 tests with no skips. The host build
has only the pre-existing miniaudio enum-comparison warning. Documentation
rendering validates 226 pages and 7623 local links. Installer generation and
interactive listening were not repeated as part of this integration.
