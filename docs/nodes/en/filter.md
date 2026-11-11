# Filter
Filters audio with a resonant lowpass, highpass or bandpass.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Lowpass, Highpass, Bandpass; Default: Lowpass | Editor |
| Order | 2, 4, 6, 8; Default: 2 | Editor |
| Level compensation | Off / On; Default: Off | Editor |
| Audio In | — | Audio; ±∞; Audio |
| Cutoff | 5–20 000 Hz; Default: 1000 Hz; Hz, kHz | Value; 5 Hz–0.45 × fs; Audio |
| Resonance | 0.1–30 Q; Default: 0.707 Q; Q | Value; 0.1–30 Q; Audio |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±∞ |

## Behavior
Cascades second-order sections to obtain the selected order. At Q = 0.707, lowpass/highpass sections follow a Butterworth arrangement; changing Q scales their resonances. Cutoff and resonance are smoothed. Level compensation compares input/output power over 100 ms and smooths gain over 5 ms, independently per channel, bounded to 0.25–4×. It adds no lookahead latency, is not a limiter and cannot recover removed spectral content. In a feedback loop, compensation changes the loop gain.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Audio In → Audio Out |
| Insertion | Audio In → Audio Out |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/filter.md)
