> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# FFT and spectrum processing

For capturing individual bin values, see [Holding values in a Spectral Group](SPECTRAL-HOLD.md).

The palette contains one **FFT**, under **Spectral**. Its **Spectrum** output feeds
IFFT, Spectrum Shift, spectral groups, Autocorrelation, Spectrum Statistics and the
Analysis Viewer. Consumers reuse the existing complex coefficients rather than
calculating another forward FFT. Correlation remains a distinct analysis result;
it is not a reconstructable spectrum.

**Synchronous** is enabled by default. Every window is calculated in the audio
path; FFT to IFFT latency is one window (also with 2x zero padding). The overlap
slider is rounded to sample positions; synchronous operation retains the stream
engine's minimum hop of 64 samples. The tooltip shows the effective hop.

Disable **Synchronous** for background computation. The audio thread never waits
for this worker. Bounded queues can drop windows; completed windows retain their
original sample positions. IFFT still accepts the connection and shows a warning,
also through Spectrum Shift and groups. Remaining overlapping contributions can
still produce audio; without them the output is silent. Late frames cannot repair
already emitted audio and are never replayed as new sound. Gaps, clicks or level
changes are possible. The warning does not mute the graph or force the checkbox.

**Channel: All** preserves separate stereo channels and MIDI voices. Left, Right
and Mix select a channel (Mix = (L+R)/2) and sum voices, matching the former analysis
FFT. New FFTs default to All. Windowing, DC removal and padding remain available;
DC removal intentionally changes the reconstructed signal.

Loading older documents preserves their timing: old analysis FFTs stay asynchronous
with their channel selection, and FFT Stream nodes become synchronous FFTs with
All selected. Node IDs, wire IDs and placement are retained, including exposed
spectrum ports in nested groups. The old native stream processor remains available
for previously exported source plugins but is hidden from the node palette.


## Spectral groups

The normal Add menu offers **New Spectral Group**. Inside a spectral group it
only offers supported bin operations. Bin Context and Bin Read are only offered
there; Bin Output can be re-added when missing.

The group defines the per-bin evaluation scope. Group boundary ports transport
whole spectra; the numeric graph feeding Bin Output runs once per bin. Disconnected
numeric branches are not evaluated. Bin Context exposes the current bin, whereas
Bin Read looks up an arbitrary index in the Spectrum connected to its input.
Connect the group Spectrum input to each Bin Read that needs it. An unconnected
Bin Read returns zero. Older documents gain their previously implicit connections
when loaded, including through nested groups.

A spectral group has one Spectrum input and output and supports up to 16 numeric
Audio/Control inputs. Multiple spectrum sources remain future work (see TODO.md).


Stateless numeric nodes such as Math, Comparator, Map Range, Clamp, Curve and Mix
reuse their native sample evaluation with a one-value context per bin. Comparator
produces 0 or 1, for example to mask bins using their frequency or magnitude.
`BasicNode.supportsSpectralBins()` explicitly opts a node into this context; the
palette and compiler both use it. The native spectral processor factory must also
support the implementation. Sample-feedback support alone is insufficient:
oscillators, delays, filters and temporal hysteresis must not carry state from
one frequency bin to the next. Temporal slider smoothing is likewise inappropriate
across bins. Such nodes require separately defined spectral semantics before use.


## External frame values and randomization

Add a numeric input in **Group-IO**, expose its connection point and connect it to
an internal numeric input as usual. Outside the group, connect an Audio or Control
source. The value at the last sample of each FFT window is held for every bin in
that window. Control sources use the value of that sample's audio block. Units
continue to be converted by the group boundary adapters. An unconnected public
input retains its normal internal editor/default behavior.

The same rule applies to asynchronous FFTs: a preallocated history stores 131072
samples per used numeric input (about 2.73 seconds at 48 kHz, 512 KiB per input and
processing instance). Windows older than this history, preceding connection/start,
or belonging to a reset timeline are omitted, never assigned a newer value.
IFFT then uses any remaining overlapping windows, or silence. Trigger inputs
capture events in each frame's hop interval. MIDI resources and additional
Spectrum inputs are not supported by this boundary yet.

**Formula** can process each bin using arithmetic, conditions and independent
persistent state. See [Spectral formulas](SPECTRAL-FORMULA.md) for the execution
context and a complete Gaussian band-filter example.

**Bin Random** produces independent uniform values in [0, 1). **Fixed per bin**
keeps the same pattern across windows; **Per frame** changes it with the FFT window's
sample position. A **Seed** makes both modes reproducible; equal seed/bin/frame
also gives equal values across channels and group instances. Use distinct seeds
for different patterns and Math/Map Range for another range. This differs from
external Noise: an external source is held once per window, while Bin Random
varies across bins within that window. Per-frame random changes can sound noisy.
