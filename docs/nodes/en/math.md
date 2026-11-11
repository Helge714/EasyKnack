# Math
Applies a selectable mathematical operation.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Operation | Add, Subtract, Multiply, Divide, Power, Remainder, Modulo, Minimum, Maximum, Floor, Ceil, Round, Fract, Sqrt, Root, Exp, Log, Log Base, Absolute, Negate; Default: Multiply | Editor |
| Clamp ±1 | Off / On; Default: Off | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| A | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| B | ±1 000 000; Default: 1 | Value; ±∞; Auto; Binary operation only |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞ |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Binary operations evaluate A and B; unary operations hide B. Operand names and automatic defaults follow the operation. Remainder follows the dividend sign; Modulo uses a floor-based remainder. Fract is `x − floor(x)`. Division by a near-zero divisor and invalid domains return safe values; nonfinite results become zero. Optional Clamp limits the final result to ±1. Each connected input keeps its own rate. The same operations are available inside [Spectral Groups](spectral-group.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | A → Out |
| Insertion | A → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/math.md)
