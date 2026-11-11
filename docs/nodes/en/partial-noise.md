# Partial Noise
Supplies reproducible random values per tracked partial and analysis frame.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Seed | 0–2 147 483 647, integer; Default: 0 | Value; 0–2 147 483 647; Auto |
| Vary by partial | Off / On; Default: On | Editor |
| Vary by frame | Off / On; Default: On | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Random | Value; 0–1, excluding 1; Audio (partial/frame) |

## Behavior
Available only inside a Partial Group. Uniform values in [0, 1) are keyed by Seed, stable partial ID and FFT-frame position. Vary by partial separates tracks; when disabled, all partials in the same frame share one value. Vary by frame produces new values for every FFT window; when disabled, a track retains its value throughout its lifetime. With both options disabled, only Seed determines the value. Amplitude ordering, evaluation order and channel do not change the assignment. For symmetric octave modulation use `f_new = f * pow(2, strength * (2 * random - 1))`. This node does not require the Partial Context Seed output.

A connected Seed is sampled once per evaluated partial, rounded down and limited to 0–2 147 483 647. Non-finite values use the manual Seed. Without a connection the integer editor value is retained exactly. Connect a context index or a group input to vary the seed per instance.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Partial Group](partial-group.md) · [Partial Context](partial-context.md)
