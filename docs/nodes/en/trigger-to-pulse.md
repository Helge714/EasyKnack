# Trigger to Pulse
Converts trigger events into numeric sample impulses.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | — | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Pulse | Value; 0–∞; Audio |

## Behavior
The output is zero except at event samples. Each event adds 1 at its sample; coincident events can therefore give values greater than 1. These numeric impulses can be processed by Math, but do not remain Trigger messages.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-to-pulse.md)
