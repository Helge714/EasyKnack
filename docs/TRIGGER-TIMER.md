> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Trigger Timer

The node is available under Modulation. Both numeric outputs use Audio rate:

- Elapsed is zero until the first trigger, then increases once per sample. Every
  trigger resets it to zero at that sample. It is never smoothed.
- Interval stays zero until two triggers have arrived. It then holds their
  measured separation until the next trigger.

Smoothing accepts Audio or Control and is sampled at each trigger. The update is
`previous * smoothing + measured * (1 - smoothing)`. Zero disables smoothing;
0.9 retains 90% of the old estimate; 1 holds the estimate. The first complete
interval is always adopted directly, even when Smoothing is 1. External values
are clamped to 0–1; non-finite values use the slider setting.

Unit selects seconds (default), milliseconds or samples for both outputs. The
choice changes the numeric signal values, and the output labels show the unit.
Changing it does not reset timing or the smoothing history. When connecting to a
time slider, choose the matching input unit there. Ordinary graph rebuilds retain
the timer state. Trigger Timer supports sample-feedback processing and independent
MIDI voices.

For a tempo estimate, use `60 / Interval` with the timer set to seconds and wait
for the first complete interval. Smoothing reduces jitter; it does not identify
missed beats, false triggers or musical subdivisions.
