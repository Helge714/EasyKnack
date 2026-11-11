# Buffer Context
Reads the size and write position of a Buffer resource.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Buffer | — | Buffer resource; Buffer resource; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Size | Value; 0–+∞ smp; Control |
| Write position | Value; 0–1; Audio |

## Behavior

[Guide](../../user/en/buffers.md)
Size is the allocated number of samples per channel, including unwritten recording capacity. For File Buffer it is the loaded start/end selection. The default unit is smp; seconds and milliseconds are available through the output unit menu.

Write position is the normalized next-write location. Recording buffers advance sample by sample and wrap to 0, matching the Buffer Readers. File buffers report 1 (end of file). Empty or unconnected resources produce 0 at both outputs.

Old Recording Buffer Write position connections are automatically moved onto Buffer Context when loading, including exposed group outputs. Loading a migrated file displays a notice; saving retains the new routing.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/buffer-context.md)
