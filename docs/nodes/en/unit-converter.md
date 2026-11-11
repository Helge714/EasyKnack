# Unit Converter
Converts a number between compatible units.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| From | Supported units; Default: Factor | Editor |
| To | Compatible target units; Default: Percent | Editor |
| Value | ±∞; Default: 0; Selected source unit | Value; ±∞; Auto |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±1e30 |

## Behavior
Converts through the units' shared base quantity. Time and frequency additionally use reciprocal period conversion `T = 1/f`; zero maps to zero rather than infinity. Target choices follow From. This explicitly changes numbers; ordinary wires do not match units automatically. See [Units](../../user/en/units.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Insertion | Value → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/unit-converter.md)
