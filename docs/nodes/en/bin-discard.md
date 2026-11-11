# Bin Discard
Skips a bin before its remaining processing.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Discard | Off / On; Default: Off | Value; 0 keeps; nonzero discards; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
In a [Spectral Group](spectral-group.md), the condition is evaluated before that bin's other work for each frame. A discarded bin is zero in every Spectrum output and contributes no generated audio. The condition must depend only on frame/bin values, not generated temporal audio. This is computation pruning, not an amplitude threshold by itself; use a Comparator to define the threshold.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-discard.md)
