# Signals and processing

A connection has a **data type**, **rate**, **unit** and, where applicable, **channels**. These are independent. Audio and numeric values can use the same math nodes. Buffer, Wavetable, Spectrum and Partials are resources requiring suitable readers or processors.

**Audio** evaluates per sample; **Control** once per block. **Auto** selects the required precision. Converting a Control signal afterwards cannot recover missing intermediate values. Stateful formulas need Audio precision when their state must advance per sample.

A **gate** holds a number, usually 0 or 1. A **trigger** is an individual event and can occur again while a gate stays 1. MIDI messages become numbers or triggers only through suitable MIDI nodes.

## Units

Choose units in supported editors' or ports' context menus. Changing a manual value's unit preserves its physical quantity. Connections instead carry numbers without automatic unit matching: `1` from a seconds output means `1 ms` at a milliseconds input. Match units or use Unit Converter. Group boundaries additionally convert into the units expected internally.

Useful relationships: `1 kHz = 1000 Hz`, `1 s = 1000 ms`, `1 turn = 360° = 2π rad`, `1 oct = 12 st = 1200 cent`, amplitude `dB = 20 * log10(factor)`. Widths in bins and octaves are different interpretations; conversion needs bin spacing and center frequency.

## Mute and feedback

Mute follows the node: effects may bypass, generators become silent, resources may stop appearing. It is not a universal zero value. Groups use their configured bypass routing. Mute does not change Windows device mute.

Feedback requires a protecting delay and supported processors. Very short delays increase work. Bypassing the only delay can disable the return connection with a warning.

Editor bounds do not automatically constrain connected signals. In short references, ±∞ means arbitrary finite values, not acceptance of Infinity or NaN. An omitted insertion or sample-feedback routing row means that feature is unsupported.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/signals.md)
