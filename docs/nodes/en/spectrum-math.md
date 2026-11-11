# Spectrum Math
Applies complex arithmetic to two temporally aligned spectra.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide; Default: Add | Editor |
| A | — | Spectrum; Control |
| B | — | Spectrum; Control |
| Gain A | 0–∞; Default: 1; Factor, % | Editor; Inspector |
| Gain B | 0–∞; Default: 1; Factor, % | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum; Control |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Computes `op(A * Gain A, B * Gain B)` for each complex FFT coefficient. Add and Subtract retain phase relationships. Multiply is complex multiplication, not sample-wise multiplication of the audio signals. Division by zero and nonfinite results produce 0. No automatic level normalization is applied. A disconnected input contributes zero. Both spectra must share FFT size, window, hop, sample rate and frame positions; incompatible batches produce an empty spectrum. For partial reconstruction use Add and both gains at 1. Earlier weighted Spectrum Mix nodes migrate to this node on load, preserving connections and gains.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | A → Spectrum |
| Insertion | A → Spectrum |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Deutsch](../de/spectrum-math.md)
