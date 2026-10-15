# Custom waveforms

## Draw one cycle

1. Add **Generators → Waveform**.
2. Edit the curve: double-click to add a point, drag to move it, and right-click
   to select a linear, stepped or Bézier segment. The horizontal range is one
   cycle (phase 0–1); the vertical range represents amplitude −1 to +1.
3. The first and last points share their amplitude. Use the context menu to
   start from a sine, triangle, sawtooth or square wave.
4. Connect the dark-blue **Waveform** resource output to **Waveform** on an
   Oscillator or Complex Oscillator. Set the playback frequency on the oscillator.

The editor enlarges vertically with its node and becomes a compact preview when
hidden. Curve edits are saved in the document and support undo. Oscillators use
precomputed band-limited tables appropriate to the playback frequency. Existing
oscillator shape choices remain available when their Waveform port is unconnected.

## Capture a tonal sound

1. Connect the sound to **FFT**. Choose the desired channel and a window long
   enough to resolve its harmonics; several periods of the fundamental are needed.
2. Connect **Spectrum** to **Spectrum → Spectrum to Waveform**.
3. Set **Fundamental** to the source's fundamental frequency, or connect a pitch
   estimate. This identifies the harmonic spacing; it does not set playback pitch.
4. Press **Capture**, or connect a trigger. A capture waits for an available FFT
   snapshot and keeps its harmonic amplitudes and relative phases. It is prepared
   in the background; the previous waveform continues playing until it is ready.
5. Connect the captured **Waveform** to an oscillator. **Normalize** sets the
   captured cycle's peak to one; disable it to retain the estimated amplitudes.

A single periodic waveform describes a stable tonal sound. Noisy, polyphonic and
changing sounds can only be approximated. Capturing again replaces the held cycle.

**Copy captured resource** copies all 2,048 samples without curve fitting. Add a
Waveform node and choose **Paste wavetable resource** in its graph context menu.
Copy/paste is local to the current application session and creates an independent
snapshot. Samples are stored losslessly in the document.

## Draw or inspect samples

The Waveform graph context menu switches between **Curve mode** and **Resource
mode**. Resource mode draws straight segments through the actual table samples;
drag the mouse to draw new sample values. One pen stroke is one document edit.
Curve-to-resource conversion samples the curve at 2,048 positions. Switching back
to curve mode approximates the table with 64 points and may lose detail and clip
values outside −1 to +1. Undo restores the previous representation.

Add **Analysis → Wavetable Viewer** and connect a Waveform resource to inspect its
actual samples. Hover for sample index and value. Right-click to copy the complete
resource, then paste into a Waveform node. The viewer has no audio output and does
not fit a Bézier curve. Oscillator playback still uses band-limited derived tables.

## Reduce background analysis work

Disable **Synchronous** on FFT. In its inspector, set **Minimum frame interval**
(e.g. 100 ms). Background requests are separated by at least this interval and by
the overlap-derived hop, whichever is longer. Zero preserves the overlap-derived
cadence. This does not change window length or frequency resolution. A busy worker
can still deliver fewer frames; sparse asynchronous frames are intended for
analysis and capture, not continuous IFFT resynthesis.

Captured samples are stored with the node when its view receives the capture.
Keep the capture node visible until the Copy button becomes available before
saving. Reopening the document restores the captured sound without another FFT.

Waveform ports now carry resources, not numeric blocks. Old custom plugins that
sent raw sample blocks into these ports must adopt the Waveform resource contract
and be rebuilt. Built-in unconnected waveform choices are unchanged.
