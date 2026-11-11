# Trigger Timer
Measures elapsed time and intervals between triggers.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Trigger | Button | Trigger; Trigger |
| Smoothing | 0–1; Default: 0 | Value; 0–1; Auto |
| Unit | s, ms, smp; Default: s | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Elapsed | Value; 0–∞, selected unit; Audio |
| Interval | Value; 0–∞, selected unit; Audio |

## Behavior
Elapsed starts at 0 on the first Trigger and resets at each subsequent Trigger. Interval remains 0 until two triggers have arrived, then holds their distance. Smoothing s retains the previous interval: `I = s × Iold + (1−s) × Inew`; the first measured interval initializes directly. Both outputs share the selected unit.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/trigger-timer.md)
