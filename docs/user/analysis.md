# Spectrum processing and analysis

[User guide](README.md)

## Inspect audio

Connect audio directly to **Oscilloscope** for a time plot, or **Level Detector**
for Peak/RMS values. Neither requires FFT. Use Left, Right or Mix where offered;
Mix averages left and right, so opposite-phase signals can cancel.

For a spectrum, connect `audio → FFT → Analysis Viewer`. FFT has its own window
storage; no Recording Buffer is required. Connect **Autocorrelation** after FFT
when you need correlation data, then view that result in Analysis Viewer.
Autocorrelation results are not spectra and cannot be reconstructed by IFFT.

## Configure FFT

Window size determines frequency spacing: `sample rate / FFT size`. Larger windows
separate closer frequencies but cover more time and add reconstruction latency.
Zero padding gives denser plotted bins, not additional information in the recording.
Window functions reduce spectral leakage and change measured magnitudes. Raw FFT
coefficients are not automatically amplitudes in the range 0–1 or dB values.

**Overlap** controls how often a new frame is produced. The effective hop is rounded
to samples and shown in the tooltip. More overlap increases processing work.
**Synchronous** is the default for reconstruction. Asynchronous FFT may drop or deliver
frames late; IFFT accepts it with a warning, but missing contributions can produce
silence, clicks or level changes. Use synchronous FFT for a continuous FFT-to-IFFT chain.

**All** preserves channels and voices. Left, Right and Mix select a channel and sum
voices. DC removal intentionally changes the signal. For a transparent reconstruction
start with `audio → FFT → IFFT → Audio Output` and avoid signal-altering options.
FFT/IFFT adds a window's latency in addition to device latency.

## Process bins

A Spectral Group receives a spectrum and provides per-bin processing. **Bin Context**
exposes the current bin's coefficient, magnitude, phase, frequency and index.
**Bin Reader** reads another bin from an explicitly connected spectrum.
**Bin Output** accepts real/imaginary or magnitude/phase values to build the result.
For spectral gain, multiply Magnitude by a factor and keep Phase unchanged.

Frame-derived values update once per FFT frame and bin. Audio generators inside the
group run continuously with separate per-bin state. Use **Instance Merge** to sum
that generated audio; use **Bin Output** for frame-domain spectrum reconstruction.
An oscillator bank using magnitudes is not equivalent to IFFT: phase and windowing
matter. The Add menu only offers nodes supported in the current group context.

Expose numeric or Trigger inputs through the group interface to supply outside
values/events to frame processing. Numeric values are held across a frame's bins. **Bin Random** controls whether random
values vary between bins and frames. **Bin Sample & Hold** captures on a trigger;
**Bin Smoothing** blends new values into stored per-bin values.

Outside the group, **Spectrum Blur** spreads energy across neighbouring frequencies.
Octaves gives a logarithmic width: radius 1 around 220 Hz spans 110–440 Hz. Gaussian,
Box and Maximum use different kernels. Normalize power is optional and does not
promise unchanged peaks or perceived loudness after IFFT. Noise phase behaviour
changes the reconstructed sound; compare an analysis after IFFT when judging it.
**Spectrum Prune** retains the strongest bins according to its amount setting.

## Freeze, zoom and measure

Double-click a plot to open its enlarged view. Freeze recording captures the displayed
frame; it does not stop audio. Resume returns to live data. The frozen frame is temporary,
not saved in the document.

FFT plots offer linear or logarithmic frequency axes with matching grids. A logarithmic
axis cannot show 0 Hz; DC is reported separately. Hover shows the actual nearest bin
and its frequency. FFT resolution still depends on the source window, not the plot size.

In the enlarged view:

- Click to set measurement cursors A and B; Shift+click explicitly places B.
- Drag a cursor to move it. Freeze first for stable measurements.
- Use the wheel to zoom horizontally, Shift+wheel vertically, and middle-drag to pan.
- Use the context menu to reset the view or remove either cursor separately.
- Escape returns to the graph; inspection cursors and zoom survive that view switch.

## Export measurements

The enlarged view's Export menu offers CSV and images. The data snapshot is captured
before the file dialog; live updates cannot change it while you choose settings.

CSV offers comma-separated values with decimal points, or German Excel formatting
with semicolons and decimal commas. It exports actual values, not pixel-reduced plot
lines. FFT rows include bin, frequency and value; time plots and correlation use their
corresponding time/lag columns. Viewer mode determines the exported quantity; there
is no implicit conversion to dB or power density.

Image export offers the visible section or complete analysis, PNG or JPG, output
size and JPG quality. It includes axes and measurement cursors. Larger images cannot
increase measurement resolution. Export timestamps describe UI reception in UTC,
not sample-accurate acquisition time. Keep FFT window, sample rate and display mode
with your results when comparing measurements.
