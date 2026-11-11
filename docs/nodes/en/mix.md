# Mix
Crossfades between two numeric signals.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Linear, Equal Power; Default: Linear | Editor |
| Factor | 0–1 ×; Default: 0.5 ×; ×, % | Value; 0–1 ×; Auto |
| A | ±∞; Default: 0 | Value; ±∞; Auto |
| B | ±∞; Default: 0 | Value; ±∞; Auto |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞ |

## Behavior
Connecting a new wire to occupied A or B moves the previous wire to the other input when it is free and compatible. Otherwise the previous wire is replaced.

Linear: `y = (1−t)A + tB`. Equal Power: `y = cos(πt/2)A + sin(πt/2)B`, with t clamped to 0–1. Equal Power preserves expected power for uncorrelated signals; identical inputs can gain about 3 dB at the midpoint. Linear preserves identical-input level.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | A → Out |
| Insertion | A → Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/mix.md)
