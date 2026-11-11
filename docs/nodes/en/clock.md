# Clock
Generates regularly spaced triggers.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Frequency | 0.001–20 000 Hz; Default: 1 Hz; Hz, BPM | Value; 0–20 000 Hz; Auto |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| Sync | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Trigger |

## Behavior
The interval is `1/f`; 60 BPM equals 1 Hz. Sync restarts timing and emits a trigger at the synchronization sample. Connected frequency modulation follows the configured rate; zero frequency stops periodic events.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/clock.md)
