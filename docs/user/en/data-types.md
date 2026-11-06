# Data types

Data type describes a connection's contents. It is distinct from
[rate](rates.md), [unit](units.md) and color.

## Signals and messages

| Data type | Contents |
| --- | --- |
| <a id="value"></a>Value | Numbers used for calculations and control, such as frequency or mix factor |
| <a id="audio"></a>Audio | Numeric samples used for sound processing |
| <a id="trigger"></a>Trigger | Individual events without a permanently held numeric value |
| <a id="midi"></a>MIDI | Messages such as Note On, Note Off, Control Change and Pitch Bend |
| <a id="vector"></a>Vector | Three related components, such as X, Y and Z |

Audio and Value use compatible numeric signals. A Math node can therefore
process audio. A different color alone implies neither a different numeric range
nor automatic conversion.

A **gate** is a held value, usually 0 or 1. A **trigger** is an event. A new
trigger can occur while a gate already remains at 1. Use suitable MIDI nodes to
convert MIDI messages into numeric values or triggers; a MIDI port is not a
frequency or gate input.

## Resources

Resource connections reference related data rather than supplying a single
scalar value.

| Resource | Contents | Typical use |
| --- | --- | --- |
| <a id="wavetable"></a>Wavetable | One periodic waveform represented by samples | Oscillator waveform input |
| <a id="buffer"></a>Buffer | Stored audio samples with length and write position or file end | Buffer Reader |
| <a id="spectrum"></a>Spectrum | Complex FFT bins with frequency, window and timing information | Spectral processing and IFFT |
| <a id="partials"></a>Partials | Tracked sinusoidal components with frequency, amplitude, phase, identity and frame timing | Partial Group and spectral reconstruction |
| <a id="analysis"></a>Analysis | An analysis result, such as a spectrum or autocorrelation | Viewers and feature extraction |

Spectrum can connect to suitable Analysis inputs. Conversely, an arbitrary
Analysis result is not automatically a reconstructable Spectrum. Read or process
a resource with an appropriate node. Forwarding a resource does not imply copying
its entire contents at every connection.

## Channels

Channels are independent of data type and rate.

| Reference entry | Meaning |
| --- | --- |
| Mono | A fixed single signal trajectory |
| Stereo | A fixed pair of left and right trajectories |
| Left / Right | An explicitly assigned channel side |
| Auto | Channel layout follows the inputs and node behavior |
| — | Channels do not apply |

A stereo signal does not necessarily need two visible wires. Use the relevant
Channel nodes to separate or combine channels. Polyphonic voices and Instance
Groups are additional processing dimensions; they are not equivalent to left
and right channels.

[Fundamentals](index.md) · [Deutsch](../de/data-types.md)
