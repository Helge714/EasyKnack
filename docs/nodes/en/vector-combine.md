# Vector Combine
Combines X, Y and Z into a vector.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| X | ±∞; Default: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Value; ±∞; Auto |
| Y | ±∞; Default: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Value; ±∞; Auto |
| Z | ±∞; Default: 0 m; m, km, cm, mm; °, rad, turn; ×, % | Value; ±∞; Auto |
| Quantity | Length, Angle, Scale, Unitless; Default: Length | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Vector | Vector; ±∞ per component |

## Behavior
The output vector is `(X,Y,Z)`. Each numeric input can independently carry Audio or Control. Quantity selects length, angle, scale or unitless component editors. To preserve a smooth changing position, retain Audio through the downstream vector path.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-combine.md)
