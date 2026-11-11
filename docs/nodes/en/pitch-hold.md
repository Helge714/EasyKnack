# Pitch Hold
Experimental: enable **Document settings → Enable experimental features** to add this node or open dependent examples from the Examples menu. Pitch detection/acceptance is not yet reliable in all tested cases. Existing projects keep their nodes when the setting is off.

Holds the last sufficiently confident frequency.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Frequency | 0–20000 Hz; Default: 440 Hz; Hz, kHz | Value; ≥ 0 Hz; Auto |
| Confidence | 0–1; Default: 1 | Value; ±∞; Auto |
| Minimum confidence | 0–1; Default: 0.8 | Value; ±∞; Auto |
| Rate | Auto / Audio / Control; Default: Auto | Editor; Inspector |
| Initial frequency | 1–20000 Hz; Default: 440 Hz; Hz, kHz | Editor; Inspector |

## Outputs
| Output | Signal |
| --- | --- |
| Frequency [Hz] | Value; > 0 Hz |
| Valid | Value; 0–1 |

## Behavior
Accepts positive finite Frequency when Confidence meets Minimum confidence. Otherwise the previous accepted value is held; before the first accepted value it uses Initial frequency. Valid describes the current input, even while output is held. No automatic fade or smoothing is applied.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Frequency → Frequency [Hz] |
| Insertion | Frequency → Frequency [Hz] |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/pitch-hold.md)
