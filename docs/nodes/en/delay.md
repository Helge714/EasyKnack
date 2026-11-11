# Delay
Delays a signal and protects feedback cycles.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| In | ±∞; Default: 0 | Audio; ±∞; Audio |
| Time | 0–10 s; Default: 0.25 s; s, ms, smp | Value; 0–10 s; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Audio; ±∞ |

## Behavior
Time is read once per block. Fractional-sample delays use linear interpolation. Zero bypasses outside feedback; inside feedback at least one sample is enforced. The scheduler chooses block-sized or shorter execution chunks according to the loop delay. Time modulation must originate outside that loop. Mute removes the delay boundary; an unsafe resulting connection is ignored rather than evaluated as instantaneous feedback.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/delay.md)
