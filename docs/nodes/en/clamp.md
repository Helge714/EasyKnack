# Clamp
Limits, folds or wraps values around ±1.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Clip, Foldback, Wrap, Soft Clip; Default: Clip | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| In | ±1 000 000; Default: 0 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±1 |

## Behavior
Clip limits to ±1. Foldback reflects excess values at the boundaries. Wrap repeats an interval of width 2. Soft Clip computes `tanh(x)`, approaching ±1 continuously.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/clamp.md)
