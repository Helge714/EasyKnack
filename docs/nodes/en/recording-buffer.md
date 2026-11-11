# Recording Buffer
Records audio into a circular buffer.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Audio |
| Size | 256 smp–30 s; Default: 2 s; s, ms, smp | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Buffer | Buffer resource; Recorded samples; Control |

## Behavior

[Guide](../../user/en/buffers.md)
Size sets the stored duration at the DSP sample rate. Resizing allocates an empty buffer and resets readers. Readers see a sample-accurate write head, including earlier samples in the current block but never future samples. Read the resource with [Buffer Reader (Speed)](buffer-reader-speed.md) or [Buffer Reader (Position)](buffer-reader-position.md).

Use [Buffer Context](buffer-context.md) for Size and sample-accurate Write position.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/recording-buffer.md)
