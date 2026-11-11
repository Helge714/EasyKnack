# Trigger Merge
Combines several trigger inputs into one stream.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Inputs | 2–16, integer; Default: 2 | Value |
| Input 1…N | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Trigger | Trigger |

## Behavior
Events at different samples retain their positions. Events arriving at the same sample collapse to one output trigger. Reducing Inputs removes trailing ports and their connections; Undo can restore the change.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Input 1 → Trigger |
| Insertion | Input 1 → Trigger |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-merge.md)
