# Vector Math
Performs arithmetic with vector-valued results.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide, Scale, Normalize, Negate, Absolute, Minimum, Maximum, Cross; Default: Add | Editor |
| A | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vector; ±∞ per component; Auto |
| B | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm; °, rad, turn; ×, % | Vector; ±∞ per component; Auto; Binary operation |
| Factor | ±∞; Default: 1 | Value; ±∞; Auto; Scale operation |
| Quantity | Length, Angle, Scale, Unitless; Default: Length | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Vector | Vector; ±∞ per component |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Add/Subtract/Multiply/Divide/Minimum/Maximum work componentwise. Division by a zero component returns zero there. Scale multiplies A by Factor. Normalize gives `A/length(A)` or the zero vector for zero length. Cross computes the right-handed cross product. Negate and Absolute act per component. Scalar results use [Vector Length](vector-length.md), [Vector Distance](vector-distance.md) or [Vector Dot Product](vector-dot-product.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Insertion | A → Vector |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-math.md)
