# Spectral features and numeric inspection

[User guide](README.md) · [Analysis](analysis.md)

Connect **FFT → Spectral Features** to measure the shape of a spectrum. Connect
its outputs to **Value Viewer** to inspect numbers or their history. No second
FFT is needed. Both synchronous and background FFTs are supported.

## Spectral Features

The node has eight Control outputs. Each incoming spectrum is evaluated once;
the latest result is held until another frame arrives. If multiple synchronous
frames arrive within a block, Flux includes each successive comparison and the
outputs hold the last frame's descriptors. Background FFTs may skip frames: Flux
then compares the available frames, rather than inventing missing observations.

Choose **Power** (default) or **Magnitude** weighting. With bin frequency `f[k]`,
raw complex coefficient `X[k]` and analysis-window sum `W`, the one-sided weights
are `w[k] = 2 * |X[k]/W|²` for Power and `2 * |X[k]/W|` for Magnitude. DC and
Nyquist use a multiplier of 1. Use the same weighting, window, FFT resolution and
hop when comparing measurements. This energy convention differs from simply
squaring a doubled one-sided amplitude at every bin.

Define `p[k] = w[k] / sum(w)`, `mu = sum(p[k] * f[k])`, and
`m[r] = sum(p[k] * (f[k] - mu)^r)` over DC through Nyquist:

| Output | Definition | Unit |
|---|---|---|
| Centroid | `mu` | Hz |
| Spread | `sqrt(m[2])` | Hz |
| Skewness | `m[3] / m[2]^(3/2)` | dimensionless |
| Kurtosis | `m[4] / m[2]²` (ordinary; Gaussian = 3) | dimensionless |
| Flux | Euclidean distance from the previous spectral weights | dimensionless when normalized |
| Roll-off | First bin frequency whose cumulative weight reaches the selected fraction | Hz |
| Slope | Linear regression slope of `p[k]` against `f[k]` | 1/Hz |
| Flatness | Geometric mean of weights divided by their arithmetic mean | 0–1 |

In the inspector, **Frequency scale** selects the coordinates for Centroid,
Spread, Skewness and Kurtosis. **Linear** is the default and uses the definitions
above. **Logarithmic** uses `x[k] = log2(f[k] / 1 Hz)` for positive-frequency bins;
DC is excluded and the remaining weights are normalized again for these four
moments. Centroid is `2^mean(x)` Hz, Spread is the standard deviation of `x` in
octaves (also selectable as semitones or cents). Skewness and Kurtosis use the
central moments of `x` and remain dimensionless. A DC-only spectrum has zero for
all four moments.

For equal weights at 220 and 880 Hz, Linear gives Centroid 550 Hz and Spread
330 Hz; Logarithmic gives Centroid 440 Hz and Spread 1 octave. Transposing the
whole distribution shifts the logarithmic Centroid but preserves its Spread,
Skewness and Kurtosis. No resampling into equal-octave bands or extra frequency
weighting is performed. This is a coordinate change for moments, not a change
to the spectrum.

Flux, Roll-off, Slope and Flatness retain their original definitions, including
DC. Slope always uses linear Hz. Changing scale resets the Flux comparison and
re-evaluates the current frame. Switching Spread from Hz to octaves changes the
meaning of values on existing downstream connections; review those connections
when changing scale. Existing documents remain Linear unless explicitly changed.

**Normalize flux** compares unit-sum weights, measuring changes of spectral
shape independently of overall level. Disable it to compare the unnormalized
weights, including changes of level. Flux is a difference per observed frame,
not a derivative per second. Its first frame is zero; changing source, weighting
or FFT configuration resets its reference.

Roll-off defaults to 0.85. With Power weighting, this is the frequency below
which 85% of the measured one-sided energy lies. Magnitude weighting instead
measures an amplitude-weight fraction. Centroid is not a pitch detector.

Silence returns zero for shape descriptors. Zero-spread Skewness and Kurtosis
also return zero; this convention avoids undefined numeric outputs. A transition
to silence may still produce Flux. Flatness uses a relative floor of `1e-12` of
the mean weight to handle empty bins, and returns zero for silence. A perfectly
flat discrete distribution has Flatness 1; an individual white-noise frame need
not reach that value.

## Value Viewer

Set **Inputs** to add up to eight numeric inputs. In Properties, give each trace
a name and display unit. Units are labels: they do not convert the incoming
number. Use Unit Converter when a conversion is required. Choose Left, Right or
Mix for stereo inputs; Mix is the arithmetic mean of both channels.

- **History / Values** switches between independent time graphs and current
  numeric readouts. Trace colors come from the selected application theme.
- **Time window** selects 0.1–600 seconds of rolling history.
- **Freeze** freezes the displayed snapshot while audio and live capture continue.
  Resume returns to the current live history.
- **Enlarge** opens a larger inspection window. Close it or press Escape to return.
- Wheel zooms the history; middle-button drag pans. Click places a measurement
  cursor and freezes the display. Shift-click places cursor B. Drag a nearby
  cursor to move it; the context menu removes either cursor separately.
- The graph context menu switches between separate graphs and an overlay.
  Overlay uses one shared numeric scale; use it for commensurate quantities with
  the same unit. Separate graphs have independent automatic vertical scales.

By default, each 10 ms interval retains its minimum, maximum and last value.
Audio peaks therefore survive screen reduction even when they occur between UI
updates. Control values are held over their processing block. The curve shows
the last value and an envelope shows the minimum/maximum.

**Record every sample** in Properties retains individual samples instead. History
is bounded to one million input records across all inputs, so this mode can show
a shorter duration than requested. Live telemetry is a bounded display transport,
not an audio-file recorder: prolonged polling stalls may leave gaps, especially
with sample recording and many inputs. Gaps are not interpolated. Use an audio
recording node for guaranteed complete audio capture.

In the inspector, each input has one row with its name, display unit and up/down
buttons. The small number identifies its fixed input socket. Reordering changes
the numeric rows, graph/legend order and exports; the sockets and their connections
stay in place. Each trace keeps its input colour. Names, units and order are saved
in the document and can be undone. Units are labels only, not conversions.

Reordering also works while frozen: it rearranges the same captured samples
without resuming acquisition or discarding history. The enlarged view follows
the same order.

## Export

**Export** captures an immutable snapshot and offers PNG, JPG, standard CSV and
CSV for German Excel. Choose the current visible interval or the full retained
history; set image dimensions and JPG compression as required.

The Values view exports a numeric table (one row in CSV). History exports a time
column followed by each input's last value, minimum and maximum. Headers include
names and units; timestamps are seconds on the viewer's sample clock. Snapshot
UTC time and Freeze state are included. CSV for German Excel uses a UTF-8 BOM,
semicolons and decimal commas. CSV retains captured numeric data independently
of the image's pixel reduction.
