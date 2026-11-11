# Buffer Reader (Speed)
Reads a buffer with an independent playback clock.
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
| Speed | ±8; Default: 1 | Value; ±8; Auto |
| Offset | 0–1; Default: 0 | Value; 0–1; Auto |
| Reset | Button | Trigger; Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Audio; ±∞ |
| Distance | Value; 0–1; Audio |
| Read position | Value; 0–1; Audio |

## Behavior
Speed 1 plays normally, 0 holds and negative values reverse playback. Offset subtracts a fraction of the buffer length from the cursor. Reset places a recording-buffer cursor at the current write position, or a file-buffer cursor at the file beginning, before applying Offset. Read position can drive a [Position reader](buffer-reader-position.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/buffer-reader-speed.md)
