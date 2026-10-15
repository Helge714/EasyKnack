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

**Copy captured curve** copies a 64-point approximation for editing. Add a
Waveform node and choose **Paste captured curve (64 points)** in its curve menu.
The captured resource itself retains its full table; the editable approximation
can lose fine detail. Copy/paste is local to the current application session.

Captured samples are stored with the node when its view receives the capture.
Keep the capture node visible until the Copy button becomes available before
saving. Reopening the document restores the captured sound without another FFT.

Waveform ports now carry resources, not numeric blocks. Old custom plugins that
sent raw sample blocks into these ports must adopt the Waveform resource contract
and be rebuilt. Built-in unconnected waveform choices are unchanged.
