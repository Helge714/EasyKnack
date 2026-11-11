# Pan
Distributes an audio signal between left and right.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Audio In | — | Audio; ±∞; Audio |
| Pan | ±1; Default: 0 | Value; ±1; Audio |

## Outputs
| Output | Signal |
| --- | --- |
| Stereo Out | Audio; ±∞; Stereo |

## Behavior
For `a = (pan+1)π/4`, left gain is `√2 cos(a)` and right gain `√2 sin(a)`. Center leaves both channel gains at 1; an extreme boosts the selected side by √2 and silences the other. A mono source is panned; stereo channels are scaled independently. Before Voice Merge this acts per voice, after it on the combined signal.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Audio In → Stereo Out |
| Insertion | Audio In → Stereo Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/pan.md)
