# Curve Mapping
Maps values through an editable curve.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Curve | Curve editor; Default: Identity | Editor |
| Outside range | Clamp, Linear extension; Default: Clamp | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| Value | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| Input min | ±1 000 000; Default: 0 | Value |
| Input max | ±1 000 000; Default: 1 | Value |
| Output min | ±1 000 000; Default: 0 | Value |
| Output max | ±1 000 000; Default: 1 | Value |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±1e12 |

## Behavior
Normalizes the input using Input min/max, samples the curve, then scales to Output min/max. Clamp holds curve endpoints outside the input domain; Linear extension continues their boundary slopes. A zero-width input domain returns Output min. Supports Bézier control points and direct use in Spectral Groups.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Value → Out |
| Insertion | Value → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/curve-mapping.md)
