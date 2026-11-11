# Audio Input
Captures audio from a selected source.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Source | Available capture sources; Default: System mix | Editor |
| Mode | Copy; Default: Copy | Editor; System mix or application |
| Channels | Mono / Stereo; Default: Stereo | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±1 |

## Behavior
Capture can use a device, the system mix or an application. Copy captures the signal without suppressing the original playback. For exclusive rerouting, select a virtual routing device and route the source application to that device in Windows. Mono combines channels; Stereo preserves left and right.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/audio-input.md)
