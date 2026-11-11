# Trigger Counter
Counts incoming triggers and exposes their zero-based index.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Reset | Button | Trigger; Trigger |
| Wrap | Off / On; Default: Off | Editor |
| Count | 1–16 777 216, integer; Default: 16 | Value; 1–16 777 216, rounded; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Index | Value; 0–16 777 215; Audio |
| Trigger | Trigger |

## Behavior
The first trigger outputs Index 0; subsequent triggers increment it. Index is held between events. Wrap cycles through 0…Count−1; without Wrap the counter saturates at 16 777 215. Reset prepares Index 0; a simultaneous Trigger is still counted as the first event. The Trigger output forwards counted events.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Trigger → Trigger |
| Insertion | Trigger → Trigger |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-counter.md)
