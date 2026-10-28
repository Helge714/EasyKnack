# Manual control interpolation

Continuous sliders can use a buffered timeline or adaptive linear ramps for mouse
and MIDI changes. Choose **Settings → Document settings** for the document defaults.
The options are saved with the document. **Configure MIDI driver** offers
**Document default**, **Custom interpolation** or **Off** for an individual driver.
Discrete controls such as choices, counts, seeds and buffer sizes switch directly.
Slider tooltips identify continuous and discrete controls.

## Buffered interpolation

**Buffered interpolation** is enabled by default with a **50 ms** delay. The host
replays timestamped control points on a delayed timeline and interpolates linearly
between known neighbors. This replaces adaptive ramps: their duration is not added
to the buffer delay. The displayed slider still shows the current target.

Give the buffer some margin beyond the usual MIDI event spacing, especially when
rounding is enabled. A buffer cannot reconstruct movement for which the next
point has not arrived. A late point starts a new transition without jumping the
current position. Extremely dense streams use bounded storage; if it fills, only
future points are coarsened while the playback head is retained.

**Round movement edges** is enabled by default at **5 ms**. It gently changes
velocity at the beginning and end of a movement while preserving target positions.
The interior stays linear; this is not a continuous target-following filter.
Short segments reduce the rounding duration automatically. With sufficient buffer
reserve, rounding fits inside the delayed timeline without another fixed delay.
If a successor arrives after an endpoint's rounding has started, that endpoint
finishes smoothly and the next segment restarts smoothly.

Buffering and rounding can be enabled independently. A zero buffer duration uses
the adaptive path. Changing these timing options resets the old queued movement
to the current target.

## Adaptive interpolation

With buffering off, each target starts a ramp from the value currently being
rendered. **Initial / restart transition** (Min, default **5 ms**) applies to the
first change or a gap greater than **Inactivity threshold** (Max, default **50 ms**).
Otherwise the duration is exactly the interval since the preceding target change,
even below Min or exactly equal to Max. A new target interrupts the running ramp;
a completed ramp holds its target.

Without lookahead, the end of an ongoing movement cannot be known in advance.
Rounding therefore applies to initial/restart ramps; ongoing adaptive ramps retain
their linear timing. Use buffering to round both ends of a continuous movement.

A connected signal always takes priority and bypasses this feature. The driver
assignment remains saved; MIDI resumes with a new message after disconnection.
Control-rate processing still evaluates once per block. An audio-rate path is
needed for a separate interpolated value at each sample. For a MIDI-controlled
position through **Vector Combine**, select **Audio** in its Inspector rate setting.

MIDI-driven values do not add Undo entries. Assignments and mapping edits remain
normal document edits. The audio host renders transitions independently of GUI
repainting.
