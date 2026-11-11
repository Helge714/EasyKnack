# Gain
Scales an audio signal by an amplitude factor.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Gain | 0–1.995262 ×; Default: 0.251189 ×; ×, %, dB | Value; 0–1e6 ×; Auto |
| Audio In | — | Audio; ±∞; Audio |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±∞ |

## Behavior
`y = x × gain`, with `gain = 10^(dB/20)`. Manual −12 dB corresponds to about 0.251189×. Values at or below −160 dB mute the signal. Connected gain is limited to 120 dB; manual changes are smoothed. Gain does not limit the audio output.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Audio In → Audio Out |
| Insertion | Audio In → Audio Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/gain.md)
