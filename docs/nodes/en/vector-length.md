# Vector Length
Calculates Euclidean vector length.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Vector | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vector; ±∞ per component; Auto |
| Quantity | Length, Angle, Scale, Unitless; Default: Length | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Length | Value; 0–∞ |

## Behavior
`length = sqrt(X² + Y² + Z²)`. The output uses the chosen quantity's unit representation. Angle components are treated as numeric coordinates, not shortest wrapped angular distances.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-length.md)
