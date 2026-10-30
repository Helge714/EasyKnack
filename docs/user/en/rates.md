# Rates

Rate determines **when** values are evaluated.
[Data type](data-types.md) determines **what** a connection carries.

## Audio, Control and Auto

| Rate | Meaning | Numeric port appearance |
| --- | --- | --- |
| Audio | One value per sample | Circle |
| Control | One value per audio block, held throughout that block | Diamond |
| Auto | Select Audio or Control according to the node's rules | Resolved rate's shape |

The current DSP configuration uses 48,000 samples per second and blocks of up to
256 samples. A full block lasts approximately 5.33 ms.

Auto is not an additional processing rate. In adaptive numeric nodes, an Audio
input usually results in an Audio output; Control-only inputs allow Control.
Inputs retain their individual rates: a Control value need not be recalculated
for every sample just because another input carries Audio. Fixed rates and
special policies are described in each node reference.

An Audio-rate value need not be audible audio. Frequency, position, gate and
other control quantities can also change sample by sample.

## Rate conversion

A Control value at an Audio input is held for the block. This does not create
interpolated intermediate values.

A Control input normally reads an Audio signal at block start, unless the node
defines another measurement or reduction method. Brief changes within the block
can be missed. **Rate Converter** offers first/last sample, mean, RMS, minimum,
maximum and absolute peak reductions.

The mean of a symmetric alternating signal can be near zero. RMS or absolute
peak is more suitable for measuring its level.

## Trigger and MIDI

**Trigger** denotes individual events that initiate an action. Its port uses a
double diamond. “Sample-accurate” describes event timing resolution, not another
rate. A trigger is not a held number. Use the appropriate trigger converters
when converting between these representations.

MIDI carries timed messages and is a separate data type. **MIDI Note** can derive
Audio-rate frequency and gate values, alongside separate triggers. Event-based
storage does not make its numeric outputs Control signals.

## Resources and smoothing

A resource at Control rate can contain an entire buffer or spectrum. Control
does not describe how many samples it contains. FFT window size, hop size and
update mode determine spectral frame timing independently of the audio block size.

Smoothing is a separate function. Rate conversion does not smooth automatically.
[Manual and MIDI control interpolation](../manual-controls.md) applies to editor
changes; connected signals need a suitable smoothing node when required. A
subsequent Control path can reduce a smoothed trajectory back to block values.

[Fundamentals](index.md) · [Deutsch](../de/rates.md)
