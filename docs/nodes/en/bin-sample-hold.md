# Bin Sample & Hold
Captures values separately for each bin and channel.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Value | ±∞; Default: 0 | Value; ±∞; Auto |
| Capture | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Value | Value; ±∞; Audio (bin/frame) |

## Behavior
Before capture, the incoming values pass through. Capture stores the next complete FFT frame's value independently per bin/channel, then holds it until the next capture. Holding magnitude alone still uses a live phase if phase is wired directly; this is not an autonomous freeze of the complete complex spectrum. Only valid in [Spectral Groups](spectral-group.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Value → Value |
| Insertion | Value → Value |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-sample-hold.md)
