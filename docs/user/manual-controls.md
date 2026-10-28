# Manual control interpolation

Continuous sliders use adaptive linear transitions for mouse and MIDI changes.
Choose **Settings → Document settings** to enable or disable interpolation and set
its minimum and maximum transition time. The defaults are **5 ms** and **50 ms**.
These settings are saved in the document.

Each new target starts from the value currently being rendered. The transition
time follows the interval between changes, bounded by the configured limits.
The first transition uses 20 ms within those limits. A long pause therefore never
causes an excessively slow response. This reduces stepping; it cannot predict
future controller movement or guarantee a perfectly constant motion.

**Configure MIDI driver** offers **Document default**, **Adaptive linear** with
custom limits, or **Off** for continuous sliders. This overrides the document
policy for that driver. Choice editors, buttons, seeds, counts, buffer sizes and
other discrete settings switch directly. Slider tooltips identify continuous
and discrete controls.

A connected signal always takes priority and is never interpolated by this
feature. The assignment remains saved; MIDI resumes with a new message after
disconnecting the signal. Control-rate processing still evaluates once per block.
Use an audio-rate path when you need a separate interpolated value at each sample.

MIDI-driven parameter changes do not add Undo entries. Driver assignments and
mapping edits remain normal document edits. The displayed slider value is the
target; the audio host renders the transition independently of GUI repainting.
