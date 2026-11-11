# Digital Noise
Generates cycle-based random frequency modulation.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Frequency | 1e-5–20 000 Hz; Default: 440 Hz; Hz, kHz | Value; 0–20 000 Hz; Auto |
| Strength | 0–1; Default: 0 | Value; 0–1; Auto |
| Distribution | Uniform, Gaussian; Default: Uniform | Editor |
| Seed | −1–16 777 215; Default: −1 | Value; −1–16 777 215, rounded; Control |

## Outputs
| Output | Signal |
| --- | --- |
| Factor | Value; 2^(−s)–2^s; Audio |
| Trigger | Trigger |
| Frequency | Value; f × Factor; Audio |

## Behavior
Factor is held for a self-modulated cycle; Frequency outputs `f × Factor`. Strength `s` bounds octave deviation to `±s`. Gaussian uses standard deviation `s/3`, truncated at `±s`. Drawing compensates for cycle duration so time-weighted octave deviations are symmetric. This does not preserve the arithmetic mean in Hz. Strength 0 gives Factor 1 and passes Frequency unchanged. Trigger marks a new draw. Seed follows [Noise](noise.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/digital-noise.md)
