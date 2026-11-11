# Noise
Generates seeded noise with a selectable spectral color.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Seed | −1–16 777 215; Default: −1 | Value; −1–16 777 215, rounded; Control |
| Noise | White, Pink, Brown, Blue; Default: White | Editor |
| Rate | Audio, Control; Default: Audio | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Out | Audio; ±1; Audio / Control |

## Behavior
White is spectrally flat in power per Hz; Pink emphasizes low frequencies, Brown integrates a white sequence, and Blue emphasizes changes. Seed −1 uses the logical node's automatic seed. Channel/instance copies share it; drive Seed from context to obtain independent sequences. Changing Seed restarts the sequence. Control produces one held random value per block.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/noise.md)
