# Partial Group

**Partial Analysis** detects and tracks sinusoidal components. Unlike FFT bins, Partials have tracked IDs and independently editable frequency, amplitude and phase.

A typical chain is `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Math (Add) → IFFT`. Connect the analysis residual spectrum to the second Add input when the non-sinusoidal component should remain.

Inside, **Partial Context** supplies partial values; process them with Math or Formula Group and write them through **Partial Output**. State is separate per ID and resets for a new ID. **Partial Noise** supplies independent random control using stable IDs; `2 * random - 1` gives a symmetric deviation.

The group runs once per analysis frame and partial, not as an audio oscillator bank. It supports up to 64 numeric processors, including flattened ordinary groups and numeric Formula Groups. Buffers and trigger programs do not belong in this context. External numbers are read at the window end from the current publication block; historical asynchronous modulation values are not buffered.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/partial-group.md)
