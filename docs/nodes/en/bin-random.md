# Bin Random
Supplies deterministic random numbers per bin and frame.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Seed | 0–2 147 483 647, integer; Default: 0 | Value; 0–2 147 483 647; Auto |
| Vary by bin | Off / On; Default: On | Editor |
| Vary by frame | Off / On; Default: Off | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Random | Value; 0–1, excluding 1; Audio (bin/frame) |

## Behavior
Uniform values depend on Seed and the enabled bin/frame keys. Bin only gives a fixed random value per frequency. Frame only gives one shared random value per window. Both varies per bin and window; neither gives one constant random value. Equal keys also give equal values across channels. Frame means FFT window, not audio block. Only valid in [Spectral Groups](spectral-group.md).

A connected Seed is sampled once per evaluated bin, rounded down and limited to 0–2 147 483 647. Non-finite values use the manual Seed. Without a connection the integer editor value is retained exactly. Connect a context index or a group input to vary the seed per instance.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-random.md)
