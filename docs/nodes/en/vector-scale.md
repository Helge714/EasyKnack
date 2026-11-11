# Vector Scale
Scales each vector axis around an origin.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Vector | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Scale | ±∞ per component; Default: (1, 1, 1) ×; ×, % | Vector; ±∞ per component; Auto |
| Origin | ±∞ per component; Default: (0, 0, 0) m; m, km, cm, mm | Vector; ±∞ per component; Auto |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Vector | Vector; ±∞ per component |

## Behavior
`vout = origin + scale ⊙ (v − origin)`, where ⊙ denotes componentwise multiplication. Scale 1 preserves an axis, 0 collapses it to the origin, negative values mirror it. This node performs no rotation.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Insertion | Vector → Vector |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-scale.md)
