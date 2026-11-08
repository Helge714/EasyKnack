# Holding values in a Spectral Group

## Continuous update: Bin Memory

**Bin Memory** updates each bin once per FFT frame:
`stored = stored * (1 - Update) + input * Update`.
The first frame initializes the stored value, including when Update is zero.
Afterwards, 0 holds and 1 replaces; intermediate values gradually follow the input.
Update accepts a Control or Audio connection and is clamped to 0–1. External
Audio values are taken at the window end, including for delayed asynchronous
frames. The amount is per frame, so smaller hop sizes make the response faster.
Memory is separate per bin/channel and survives compatible rebuilds and export.
Like Bin Sample & Hold, it operates on numeric values, not phase-aware audio.

## Triggered capture: Bin Sample & Hold

Add **Bin Sample & Hold** inside a Spectral Group. It stores one value per bin
and channel when **Capture** fires. Before the first capture it passes values
through. Capture again to replace the stored values.

To hold a magnitude envelope:

1. Connect **Bin Context / Magnitude** to **Bin Sample & Hold / Value**.
2. Set **Bin Output / Components** to **Magnitude / phase**.
3. Connect the held value to **Bin Output / Magnitude** and the live
   **Bin Context / Phase** to **Bin Output / Phase**.
4. Press **Capture**. Alternatively, expose Capture as a Trigger input in Group IO
   and connect an external Trigger Input or Clock.

Use a Mix node between the live and held magnitudes for a manual crossfade.
Capture uses the first completed FFT frame after the trigger. All bins see the
same capture; multiple triggers before that frame coalesce into one capture.
Short external triggers are retained across audio blocks, including while
asynchronous FFT results are delayed (within the existing frame-history capacity).

This holds numeric values, not an independent sound. Holding only magnitude keeps
the incoming phase. The group still needs incoming FFT frames. Holding phase as
a constant is not phase continuation and can produce IFFT artifacts.

Captured values survive compatible graph rebuilds. A new FFT source identity or
FFT size clears them because the bins may represent different frequencies.
Muting the node bypasses it. Automatic empty-bin skipping is disabled for groups
with held values; an explicit Bin Discard still intentionally prevents evaluation
of discarded bins. These stateful frame programs run serially across bins so
worker scheduling cannot change which stored values are used. Bake/export uses
the same capture behaviour.
