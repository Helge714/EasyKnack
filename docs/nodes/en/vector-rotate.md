# Vector Rotate
Rotates a vector around an origin.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Vector | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Rotation | ±∞ per component; Default: (0, 0, 0)°; °, rad, turn | Vector; ±∞ per component; Auto |
| Origin | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Vector | Vector; ±∞ per component |

## Behavior
`vout = origin + Rz Ry Rx (v − origin)`. Uses right-handed Euler rotations: X first, then Y, then Z. Angles are periodic, so continuous rotations need not be manually wrapped at 360°. Order matters. Origin and Vector use length components; Rotation uses angles.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Insertion | Vector → Vector |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-rotate.md)
