# Bin Smoothing
Blends new bin values into per-bin memory.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Value | ±∞; Default: 0 | Value; ±∞; Auto |
| Update | 0–1; Default: 1 | Value; 0–1; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Value | Value; ±∞; Audio (bin/frame) |

## Behavior
Each FFT frame computes `held = (1−u) × held + u × value`. The first frame initializes directly. Update 0 holds and 1 replaces. Smaller hops update more frequently, so the same factor has a different time response at another hop size. Audio inputs are sampled at the window end. Only valid in [Spectral Groups](spectral-group.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Value → Value |
| Insertion | Value → Value |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-smoothing.md)
