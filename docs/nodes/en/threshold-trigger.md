# Threshold Trigger
Emits triggers when a value crosses a threshold.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Edge | Rising, Falling, Both; Default: Rising | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| In | ±1 000 000; Default: 0 | Value; ±∞; Auto |
| Threshold | ±1 000 000; Default: 0.5 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Trigger | Trigger |

## Behavior
Compares `input > threshold`. Rising fires on false-to-true, Falling on true-to-false, Both on either change. The first value initializes without an event. Audio checks each sample; Control checks block values. Moving Threshold can also cause a crossing.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/threshold-trigger.md)
