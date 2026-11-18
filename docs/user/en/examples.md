# Examples

Open the numbered projects in **Examples**, starting with **01 Basics**.
Choose your output device in Audio Output if necessary and start playback.
Save variations under Projects; installed examples are templates.
All examples use built-in nodes and require no audio files or plugins.
Read the layout from left to right; modulation and feedback paths sit below.

## 01 Basics

- **01 First tone:** Oscillator → Gain → Audio Output. A quiet 220 Hz sine.
  Change Frequency, then Gain. A connection carries the signal between nodes.
- **02 Filtered noise:** White noise through a lowpass. Move Cutoff between 200 and
  8000 Hz and listen to the change in timbre. Leave Resonance unchanged initially.
- **03 Tremolo:** A slow oscillator modulates the amplitude of an audible tone.
  Multiplication by 0.5 and addition of 0.5 map −1…1 to 0…1.
  Change the lower oscillator's frequency between 1 and 8 Hz.
- **04 Live buffer - speed:** A sawtooth feeds a one-second Recording Buffer and is
  read at Speed 0.75. After a short fill time, a lower tone is audible.
  Try Speed 1, 0.5 and −1. Reset places the reader relative to the current write head.
  There is no automatic crossfade; crossing the write head can cause clicks.
- **05 Live buffer - position:** A Speed Reader supplies its actual Read position to
  a Position Reader using the same Buffer. Only the Position Reader is audible.
  Offset 0 follows exactly; then try Offset 0.5. The Value Viewer shows the circulating
  Write position. Position is an absolute buffer coordinate, not Distance.
- **06 Group with controls:** An ordinary group contains an oscillator and filter.
  Change Frequency and Cutoff outside, then double-click the group and trace their
  internal inputs. This group runs once and produces one audio stream.

## 02 MIDI

First click the keyboard inside MIDI Input. Play with the mouse or its displayed
computer-key mappings. Alternatively select a connected MIDI device.
These examples are intentionally silent without notes. Gate controls holding,
Trigger starts the envelope and Velocity controls its level.

- **01 Mono instrument:** A sawtooth with Attack/Decay/Release and velocity.
  Hold a note, then release it; the sound fades over the Release time.
- **02 Poly instrument:** Eight independent voices with the same signal path.
  Play a chord and release its notes separately. Voice Merge sums voices after
  their envelopes; the lower output gain leaves room for chords.

## 03 Instance Groups

- **01 Harmonic bank:** Eight instances generate harmonics of a 220 Hz fundamental.
  Inside, Index + 1 gives the harmonic number; a Formula group weights each with
  1 / harmonic number. Change Count between 1 and 16 and open the group.
  Instance Merge uses Mean, so Count also changes the level.
- **02 Odd harmonics:** The same bank with even harmonics muted.
  Open its inner Formula group: Index 0 denotes the first harmonic, so even
  indices correspond to odd harmonic numbers. Compare its hollow sound with 01.
  Bake is optional for both examples and is not required.

## 04 Spectral Groups

- **01 FFT round trip:** Noise → synchronous FFT → Spectral Group → IFFT.
  The group passes Real and Imaginary unchanged. Open it and follow
  Bin Context → Bin Output. The 1024-sample window introduces a short latency.
- **02 Bin lowpass:** Inside, every bin above 1200 Hz receives Magnitude 0;
  phase is preserved. Change the cutoff in the inner Formula group.
  Evaluation happens once per bin per FFT window, rather than per audio sample.
  A sharp bin boundary can cause ringing in time.

## 05 Formula Groups

Double-click the Formula group. Use **Apply** after editing;
invalid code leaves the last valid formula active.

- **01 Logarithmic mapping:** Value Input 0…1 maps to 110…440 Hz.
  The midpoint is 220 Hz: equal control distances produce equal frequency ratios.
  Move the control and inspect the short formula `110 * pow(4, signal)`.
- **02 Function waveshaper:** A user-defined function softly compresses a sine.
  `final` protects its parameter; `const DRIVE` defines the fixed drive factor.
  Compare DRIVE 1 and 4. This formula runs at audio rate.

## 06 Feedback

Each return path explicitly contains a Delay. A loop without a delay cannot
execute as audio feedback. The saved return gains are below 1;
start with the stored settings.

- **01 Echo:** A Clock triggers a short noise burst every two seconds.
  Delay 0.3 s and feedback 0.5 produce audible decaying repeats.
  Change Time and the factor in the lower Math node. The return signal is
  filtered and attenuated before it is added again.
- **02 Resonator:** A similar burst excites a short, filtered loop.
  Its delay of about 6.8 ms produces a pitched tail; the actual pitch also depends
  on the filter. Shorter Time raises the pitch.
  The feedback factor 0.92 extends the tail compared with the echo.

On development startup, the former generated collection is backed up under
`.previous-examples` and hidden from the menu. Other user files are preserved.
