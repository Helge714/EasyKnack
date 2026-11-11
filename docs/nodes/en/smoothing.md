# Smoothing
Smooths rising and falling values exponentially.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Rate | Auto, Audio, Control; Default: Audio | Editor; Inspector |
| In | ±∞; Default: 0 | Value; ±∞; Auto |
| Rise | 0–60 s; Default: 0.03 s; s, ms, smp | Value; 0–60 s; Auto |
| Fall | 0–60 s; Default: 0.03 s; s, ms, smp | Value; 0–60 s; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Value; ±∞ |

## Behavior
For time constant τ, `y += (1 − exp(−dt/τ)) × (x − y)`, using Rise when increasing and Fall when decreasing. One τ reaches about 63% of a step, three τ about 95%. Zero follows immediately. The first valid value initializes directly. Audio output smooths Control steps samplewise; Control output advances once per block. This is not a fixed-duration linear ramp.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | In → Out |
| Insertion | In → Out |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/smoothing.md)
