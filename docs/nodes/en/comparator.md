# Comparator
Converts a numeric comparison into 0 or 1.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Operation | <, <=, >, >=, ==, !=; Default: < | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| A | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| B | ±1 000 000; Default: 0 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; 0–1 |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Outputs 1 when the selected comparison is true, otherwise 0. Equality compares the numeric values directly; it is not a tolerance test. The output is a numeric gate, not a Trigger. Use [Threshold Trigger](threshold-trigger.md) to create events.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | A → Out |
| Insertion | A → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/comparator.md)
