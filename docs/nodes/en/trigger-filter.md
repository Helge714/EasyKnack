# Trigger Filter
Passes triggers only while a numeric condition is positive.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Condition | 0–1; Default: 1 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Trigger | Trigger |

## Behavior
At each trigger sample, a finite Condition greater than 0 permits the event; otherwise it is discarded. Condition changing without a Trigger produces no output event. Use a [Comparator](comparator.md) for index-dependent selection.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Trigger → Trigger |
| Insertion | Trigger → Trigger |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-filter.md)
