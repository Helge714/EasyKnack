# Filter

Choose Lowpass, Highpass or Bandpass, then set Cutoff and Resonance (Q).

**Order** selects 2, 4, 6 or 8 using cascaded second-order sections. Higher orders
give steeper slopes and require more processing. At the default Q of 0.707,
lowpass/highpass section Q values follow a Butterworth arrangement. Changing Q
scales the section resonances together. Bandpass uses the same sections; its
centre gain depends on Q. Order 2 retains the original filter behaviour.

**Level compensation** compares input and output power using 100 ms exponential
averages, with 5 ms gain smoothing. Gain is bounded to 0.25–4 (about +/-12 dB).
This reduces overall level loss, but cannot recover removed frequencies or
fully compensate extreme attenuation. It also reduces excessive resonant gain.
It is not a peak limiter or perceptual loudness normalization. No additional
latency is introduced. Channels are measured independently. Compensation inside
a feedback loop changes that loop's gain.

Existing documents default to order 2 and compensation off. Filter order, stage
state and compensation are also supported in exports and sample feedback.
