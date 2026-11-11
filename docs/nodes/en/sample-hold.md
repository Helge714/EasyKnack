# Sample & Hold
Captures and holds a value on a Trigger.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| In | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| Trigger | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞ |

## Behavior
Each Trigger captures the input at its sample position; the result is held until another trigger. Initial held value is 0. Auto chooses Audio when Trigger is connected so within-block events remain visible. Forced Control reports the block-start value and later captures affect following output. Manual capture occurs at the next block.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/sample-hold.md)
