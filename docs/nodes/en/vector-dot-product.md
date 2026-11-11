# Vector Dot Product
Calculates the scalar product of two vectors.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| A | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vector; ±∞ per component; Auto |
| B | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vector; ±∞ per component; Auto |
| Quantity | Length, Angle, Scale, Unitless; Default: Length | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Dot Product | Value; ±∞ |

## Behavior
`dot = Ax Bx + Ay By + Az Bz`. For normalized vectors this is the cosine of their angle. The output is a raw number; multiplying length components mathematically gives squared-length units, not another position vector.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-dot-product.md)
