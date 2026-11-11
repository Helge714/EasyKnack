# Trigger Divider
Passes every nth input trigger.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Division | 1–1024, integer; Default: 2 | Value; 1–1024, rounded; Control |
| Reset | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Trigger | Trigger |

## Behavior
Division n emits on the nth event, then restarts the counter. Reset clears the counter; a simultaneous input is counted after Reset. Division 1 passes every event. Division changes do not themselves emit a trigger.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Trigger → Trigger |
| Insertion | Trigger → Trigger |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-divider.md)
