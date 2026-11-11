# Vector Distance
Calculates Euclidean distance between two vectors.
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
| Distance | Value; 0–∞ |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

`distance = length(A − B)`. Returns one distance per channel, with rate following the inputs. Quantity selects the unit representation, not a different distance metric.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/vector-distance.md)
