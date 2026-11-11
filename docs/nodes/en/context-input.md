# Context Input
Supplies channel, timing and block context values.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| — | — | — |

## Outputs
| Output | Signal |
| --- | --- |
| Channel | Value; ±1; Control |
| Sample Rate | Value; 48 000 Hz; Control; Mono |
| Block Size | Value; 1–256 smp; Control; Mono |
| Time | Value; 0–∞ s; Audio; Mono |

## Behavior
Channel is −1 for left and +1 for right. Sample Rate is the host DSP rate; Block Size is the actual number of frames per channel, not total routing latency. Time advances per rendered sample while this node is active and survives graph edits. Use Channel to create explicitly different channel paths.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/context-input.md)
