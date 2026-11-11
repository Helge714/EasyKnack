# Trigger Delay
Delays triggers and can protect trigger feedback loops.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Time | 1 smp–10 s; Default: 0.25 s; s, ms, smp | Value; 1 smp–10 s; Control |
| Reset | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Trigger | Trigger |

## Behavior
Time is rounded to samples and limited to at least one sample. Events keep their scheduled due times when later Time values change. Reset clears pending events. Several events due at the same sample collapse to one trigger. The delay is an execution boundary for feedback; control its time from outside the loop.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Trigger → Trigger |
| Insertion | Trigger → Trigger |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-delay.md)
