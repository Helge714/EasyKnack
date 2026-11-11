# Partial Context
Provides the current partial values inside a Partial Group.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Partials | — | Partials; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Frequency | Value; 0–Nyquist Hz; Audio |
| Amplitude | Value; 0–∞; Audio |
| Phase | Value; ±∞ turn; Audio |
| ID | Value; 1–∞; Audio |
| Age | Value; 0–∞ s; Audio |
| Index | Value; 0–32767; Audio |
| Frame time | Value; 0–∞ s; Audio |
| Seed | Value; 0–16 777 215; Audio |
| Tonality | Value; 0–1; Audio |

## Behavior
Values describe the current partial in the current frame. Phase uses turns; Index is the current amplitude ordering, ID is the tracked identity. Age and frame time use seconds. Partial Group state follows the ID.

ID remains the track identifier. Seed is a derived 24-bit value exactly representable as float for ordinary Noise seed inputs; it is not a unique identifier. Partial Noise uses the full track ID directly and needs no such connection.

Tonality is Loris’ tonal energy fraction `1 − bandwidth`, from 0 (entirely noisy) to 1 (entirely sinusoidal). It is not a classification probability. With Bandwidth model None it is 1.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Deutsch](../de/partial-context.md)
