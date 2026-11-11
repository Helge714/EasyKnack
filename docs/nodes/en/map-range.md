# Map Range
Maps one numeric interval linearly to another.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| Clamp ±1 | Off / On; Default: Off | Editor |
| Value | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| From Min | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| From Max | ±1 000 000; Default: 1 | Value; ±∞; Auto |
| To Min | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| To Max | ±1 000 000; Default: 1 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞ |

## Behavior
`t = (x − From Min) / (From Max − From Min)`; `y = To Min + t × (To Max − To Min)`. Clamp limits t to 0–1, not the final value to ±1 despite the control label. Without Clamp, values outside the source range are extrapolated. A degenerate source interval returns To Min. Reversed intervals reverse the mapping.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Value → Out |
| Insertion | Value → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/map-range.md)
