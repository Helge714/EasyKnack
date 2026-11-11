# Hilbert
Produces a delayed direct signal and its Hilbert quadrature component.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Audio |
| Filter length | 61, 129, 257, 513, 1025, 2049; Default: 257 | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Direct | Audio; ±∞ |
| Quadrature | Audio; ±∞ |

## Behavior
A Hamming-windowed FIR with selectable length N approximates a −90° phase shift for positive frequencies. Both outputs have a group delay of `(N - 1) / 2` samples (2.67 ms for N = 257 at 48 kHz). Start-up history is zero; channel and voice states are independent.

The approximation loses accuracy near DC and Nyquist; Quadrature rejects DC and the Nyquist component. For N = 257 at 48 kHz, leave approximately 400 Hz of transition bandwidth at both ends for accurate quadrature; these limits scale with sample rate. Longer filters narrow the transition regions but increase CPU cost and latency. Changing length prepares a new empty filter outside the audio thread. Length has no signal input. Outputs are not amplitude-limited.

For an upward frequency shift, compute `Direct * cos(phase) - Quadrature * sin(phase)` with a shared carrier phase. Addition selects the downward shift. Mute forwards the input to Direct without delay and silences Quadrature.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Audio In → Direct |
| Insertion | Audio In → Direct |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/hilbert.md)
