# Value Input
Supplies an editable numeric value.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Value | Configured limits; Default: 0 | Value |
| Slider type | Buttons, Bar; Default: Buttons | Editor; Inspector |
| Enable minimum | Off / On; Default: Off | Editor; Inspector |
| Minimum | ±∞; Default: 0 | Value; Inspector |
| Enable maximum | Off / On; Default: Off | Editor; Inspector |
| Maximum | ±∞; Default: 1 | Value; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Value Out | Value; Configured limits; Control |

## Behavior
Buttons can have optional bounds. Bar requires both bounds. Changing a bound also clamps Value; an invalid upper bound is raised above the minimum. The output updates once per block, even when editor motion is interpolated.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/value-input.md)
