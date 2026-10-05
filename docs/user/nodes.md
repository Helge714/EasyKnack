# Node reference

[User guide](README.md)

Use the Add Node search to find a node by the English names below. The palette is
context-sensitive: some nodes are available only inside the appropriate group.
Hover ports and controls for the current range and unit; the Inspector exposes
settings that are not shown on the node itself.

## Signals, rates and units

| Port/rate | Meaning |
|---|---|
| Audio | A numeric value at each sample; also useful for sample-accurate modulation |
| Control | One numeric value per processing block |
| Trigger | Discrete events at sample positions, not a continuous numeric value |
| MIDI | Note/controller messages; convert them with MIDI nodes before numeric processing |
| Buffer / Spectrum / analysis data | References to structured data, not an ordinary numeric signal |

Control ports use diamonds. Resource connections are dashed. Colours distinguish
signal categories; they do not replace the type/rate checks on a connection.
**Auto** on supported numeric processors selects the required Audio/Control rate.
It does not turn Trigger or MIDI into an ordinary numeric signal. A Control input
can remain scalar when another input makes the output run at Audio rate.

Unconnected numeric inputs use their slider/default. Connected inputs use the incoming
value. Unit selection is available through a slider's **Unit** context menu where
supported. Choose compatible source/target units; do not assume all numeric wires
are automatically converted. **Unit Converter** makes an explicit conversion,
including frequency and reciprocal period. Group interfaces handle their declared
internal unit conversions. File-position sliders have duration-relative conversions.

## Inputs and generators

| Node | Purpose and main controls |
|---|---|
| Audio Input | Hardware or supported system/application audio source. Select Source; Copy capture leaves the original output playing. |
| Audio File Player | Stream a mono/stereo file. Play/Pause, Reset, position, Speed and loop bounds control playback. |
| MIDI Input | Select a MIDI device or the computer keyboard. Give the keyboard focus to play instead of invoking editing shortcuts; use its transpose setting as needed. |
| Value Input | Supply a numeric Control value. Configure slider style and optional minimum/maximum in the Inspector. |
| Trigger Input | Manually emit a trigger and distribute it to multiple destinations. |
| Context Input | Supply context values such as channel position; use these to make channel-dependent processing explicit. |
| Oscillator | Waveform, Frequency, Phase and Reset control a waveform generator. Phase offsets the cycle; Reset restarts it. |
| Additive Oscillator | Generate partials of the chosen waveform. Partials, Falloff and Spread shape the result; frequency and phase control the bank. |
| Noise | Generate noise with a configurable seed. Use channel/instance context for independent sequences where needed. |
| Digital Noise | Produce a random cycle-based factor, a trigger and the resulting Frequency. Strength 0 removes variation; Frequency can drive an oscillator directly. |
| Clock | Produce regular triggers; frequency units include Hz and BPM. |
| Envelope | Generate an envelope from gate/trigger control with editable shape/timing. |

Audio File Player accepts common WAV, MP3, FLAC, Ogg, AAC and M4A media through the
bundled decoder. Speed 1 is normal playback; changing it also changes pitch.
Unconnected position controls follow playback. Loop boundaries can click when they
are discontinuous; no automatic crossfade is implied.

## Effects and routing

| Node | Purpose and main controls |
|---|---|
| Filter | Lowpass, Highpass or Bandpass with Cutoff, Resonance (Q), Order and optional Level compensation. |
| Delay | Delay audio; Time supports seconds, milliseconds and samples. Protects feedback cycles. |
| Bitcrusher | Reduce signal precision for a digital degradation effect. |
| Gain | Scale a signal using a connected or manually set gain. |
| Mix | Blend two values with a factor. Choose linear or equal-power mixing; rate can be automatic or explicit. |
| Pan | Position a signal between stereo channels. |
| Channel Separate | Expose left and right for separate processing. |
| Channel Combine | Build stereo from separate channel inputs. |
| Step Switch | Select one of 1–32 inputs. Next advances, Reset returns to the first; numbered buttons select a step. Index is zero-based. |
| Voice Merge | Reduce polyphonic voices with Sum or Mean before common effects. |
| Instance Merge | Combine instance contributions and optional weights with Sum, Mean or Weighted Mean. Used inside instance/spectral processing. |
| Instance Context | Supply Index, Count, normalized Position and Seed for the current instance. |
| Audio Output | Send audio to a selected output device. Polyphonic voices are summed for playback. |

Filter Order is 2, 4, 6 or 8. Compensation is bounded level correction, not a limiter,
LUFS normalization or restoration of removed frequencies. Resonant or feedback
configurations can still require lower gain.

Delay supports ordinary delays up to 10 seconds. Zero is bypass outside feedback;
a feedback loop requires at least one sample. Short delays in feedback require finer
processing and can cost more. Time controls must be supplied from outside the loop.
Warnings identify unsafe connections; errors identify unsupported processing.

A Step Switch with unconnected inputs is a simple sequencer: each slider supplies
one step value. Switching audio is immediate, without a crossfade. Reducing Steps
removes the corresponding trailing inputs and wires; Undo restores them.

## Math and mapping

| Node | Purpose and main controls |
|---|---|
| Math | Arithmetic and other selectable operations; optional clamp to -1…+1. Unary operations need only one input. |
| Map Range | Map a source interval to a destination interval. Check bounds and clamp behaviour. |
| Clamp | Restrict numeric values to a range. |
| Comparator | Compare values and output 0 or 1. Use Threshold Trigger when an event is needed. |
| Curve Mapping | Remap values through an editable curve. |
| Smoothing | Smooth changing numeric values. |
| Sample & Hold | Capture and hold a numeric input on a trigger. |
| Scale Quantizer | Quantize pitch to a chosen musical scale. |
| Unit Converter | Choose a source unit and compatible destination unit; output values use the destination unit. |
| Rate Converter | Preserve samples as Audio or reduce each block to Control. |
| Formula Group | Write typed expressions and if/else logic using group inputs/outputs. See [Formula Groups](groups.md#formula-groups). |

Rate Converter offers First sample, Last sample, Mean, RMS, Minimum, Maximum and
Absolute peak. Mean is signed: a bipolar waveform can average to zero. RMS and
Absolute peak are nonnegative. This conversion is distinct from Trigger conversion.

## MIDI and triggers

| Node | Purpose and main controls |
|---|---|
| MIDI Note | Convert note messages to frequency, gate and note triggers, with voice handling. Gate remains active while the note is held. |
| MIDI CC | Convert MIDI continuous-controller messages to numeric control values. |
| MIDI Transpose | Change the pitch of note messages. |
| MIDI Delay | Delay MIDI messages. |
| Threshold Trigger | Convert numeric threshold crossings to triggers. |
| Trigger to Pulse | Convert a trigger to a numeric pulse/gate with timing controls. |
| Trigger Divider | Pass selected clock divisions. |
| Trigger Counter | Count trigger events and expose the count as a numeric signal. |
| Trigger Filter | Accept/discard triggers according to its control input. |
| Trigger Delay | Delay events; supports protected trigger-feedback routing. |
| Trigger Merge | Combine trigger sources. |
| Trigger Timer | Output elapsed time since the latest trigger and the interval between the last two. |

Trigger Timer's Interval remains zero until two triggers have arrived. Its Smoothing
retains part of the previous interval estimate: 0 adopts the new interval immediately,
1 retains the estimate. Choose seconds, milliseconds or samples for its numeric outputs.
A smoothed interval is not by itself a complete beat detector.

## Buffers

| Node | Purpose and main controls |
|---|---|
| Recording Buffer | Record a rolling audio history. Buffer length changes allocated storage. |
| File Buffer | Load a chosen Start/End segment of a file into RAM. Supplies a Buffer reference, Duration and Samples. |
| Buffer Reader (Speed) | Own a playback cursor with Speed, Offset and Reset; output Audio, Read position and Distance. |
| Buffer Reader (Position) | Read at a supplied normalized position, with Offset. The input position determines timing. |

Use File Buffer when you want independent readers rather than a complete player.
At 48 kHz, stereo 32-bit float samples require about 384,000 bytes per second
(about 110 MiB for five minutes). Memory reports selected PCM storage, not compressed
file size or the maximum memory used while replacing a buffer. Samples counts sample
frames in the selected range. Decoded disk caches can still contain the complete file.

Both readers offer Wrap. With Wrap off, reaching a boundary holds the last valid
sample; it does not emit silence. For synchronized readers, connect the Speed reader's
Read position to Position readers using the same Buffer. Reset the leader. Apply offsets
and explicit windows/crossfades to create multiple read heads; independent Speed readers
are not a synchronization mechanism.

## Spectrum and analysis nodes

| Node | Purpose and main controls |
|---|---|
| FFT | Convert audio windows into complex spectra. Size, window, overlap, channel and synchronous mode determine the analysis. |
| IFFT | Reconstruct audio from spectra with overlap-add. |
| Spectrum Shift | Shift by bins; this is not a general musical pitch/formant shifter. |
| Spectrum Blur | Spread spectral content with a kernel; choose width/distribution, phase behaviour and optional power normalization. |
| Spectrum Prune | Keep the strongest bins and set others to zero. |
| Bin Context | Access the current bin inside a Spectral Group. |
| Bin Reader | Read another bin from its connected spectrum. |
| Bin Output | Write the resulting complex spectrum. |
| Bin Random | Supply random values with per-bin/per-frame variation. |
| Bin Discard | Skip a bin when its condition is nonzero; connect a magnitude comparison to discard quiet bins before other work. |
| Bin Sample & Hold | Capture per-bin values on triggers. |
| Bin Smoothing | Blend incoming values into stored per-bin values. |
| Oscilloscope | Plot audio over time with channel and trigger controls. |
| Level Detector | Measure Peak and windowed RMS, optionally with Attack/Release smoothing. |
| Autocorrelation | Derive correlation from the connected FFT result. |
| Spectrum Statistics | Derive measurements from a spectrum. |
| Analysis Viewer | Display spectra or correlation; enlarge, freeze, inspect and export. |
| Pitch Detection / Pitch Hold / Pitch Monitor | Estimate pitch, retain an estimate or inspect pitch behaviour. |

See [spectrum and analysis](analysis.md) for complete workflows and measurement limits.
