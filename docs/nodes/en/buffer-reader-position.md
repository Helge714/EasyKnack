# Buffer Reader (Position)
Reads a buffer at an externally supplied position.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Wrap | Off / On; Default: On | Editor |
| Buffer | — | Buffer resource; Buffer resource; Control |
| Position | 0–1; Default: 0 | Value; ±∞; Auto |
| Offset | 0–1; Default: 0 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Audio; ±∞ |
| Distance | Value; 0–1; Audio |
| Read position | Value; 0–1; Audio |

## Behavior
Position is an absolute fraction of the buffer; Offset is subtracted from it. Connect another reader’s Read position and use Offset = 0.5 for a synchronized second head. This reader has no internal playback clock.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/buffer-reader-position.md)
