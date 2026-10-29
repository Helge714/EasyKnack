# Complex Oscillator: partial stretching

**Stretch** is the dimensionless exponent p in `frequency(n) = fundamental * n^p`,
with partial numbers starting at 1. Its range is 0–4:

- 1: ordinary harmonic partials; this is the default.
- Between 0 and 1: compress the partial spacing.
- Above 1: progressively stretch the higher partials.
- 0: all partials coincide with the fundamental.

The fundamental stays fixed. At 200 Hz with p = 2, the first partials are
200, 800, 1800 and 3200 Hz. For nonharmonic frequency ratios, try a noninteger
exponent such as 1.4. An integer exponent such as 2 still selects integer
multiples of the fundamental, so it does not by itself guarantee inharmonicity.
Partials above Nyquist are omitted as before. Stretch accepts Audio or Control
modulation; it is no longer a pitch offset measured in cents or octaves.

Older scalar settings are converted using `p = clamp(1 + cents / 1200, 0, 4)`.
This matches the former second partial where possible, rather than preserving
the complete old sound. Port IDs and connections remain intact, but existing
modulation signals and inherited group-editor ranges/units must be reviewed.
The previous native cent-parameter contract is still accepted with the same
scalar conversion. Rebake or re-export old generated groups/plugins to expose
the new Stretch control;
the native parameter contract now explicitly identifies the power model.
