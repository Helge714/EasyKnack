# Bitcrusher
Reduces amplitude precision and update frequency.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| In | ±1; Default: 0 | Value; ±1; Auto |
| Bits | 1–24, integer; Default: 8 | Value; 1–24, rounded; Auto |
| Downsample | 1–1024, integer; Default: 1 | Value; 1–1024, rounded; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±1 |

## Behavior
Quantizes the bounded input to the selected bit depth and holds a sample for Downsample evaluations. Downsample 1 updates every evaluation. There is no anti-aliasing lowpass before this intentional sample-rate reduction.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bitcrusher.md)
