# Sine Cloud
Generates a research stimulus of up to ten million equal-amplitude, uniformly or Gaussian-distributed sinusoids with optional random initial phases.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Count | 1–10,000,000; Default: 10,000 | Value; 1–10,000,000, rounded; Control |
| Min Frequency | 0.00001 Hz–half the DSP sample rate; Default: 20 Hz; Hz, kHz | Value; 0.00001 Hz–half the sample rate; Control |
| Max Frequency | 0.00001 Hz–half the DSP sample rate; Default: 20,000 Hz; Hz, kHz | Value; 0.00001 Hz–half the sample rate; Control |
| Seed | 0–16,777,215; Default: 0 | Value; 0–16,777,215, rounded; Control |
| Gaussian | Off, On; Default: Off | Editor |
| Randomize phases | Off, On; Default: On | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±1 |
| Actual count | Value; 0–10,000,000; Control |
| Period | Value; 0 or N / sample rate; Control |

## Behavior

Identical parameters and sample rate produce the same periodic buffer. Every included sinusoid has equal amplitude; the complete table is normalized to peak 1, not RMS. Random phases are drawn once from Seed. Without that option, all sinusoids start at phase 0 and Seed has no effect. Unchanged playback loops exactly, without new random values or suppression of periodicity and beating.

Gaussian changes frequency density, not individual amplitudes: a normal distribution in linear Hz centered on the interval midpoint, standard deviation `(Max-Min)/6`, truncated at ±3σ. Seed changes phases only. Frequencies occupy distinct interior IFFT bins; DC and exact Nyquist are excluded. Insufficient grid positions reduce Actual count. Reversed bounds are sorted; equal bounds produce one tone at the nearest representable bin.

Period reports the complete active buffer duration in seconds, initially 0. The previous table continues during rebuilding; the new period applies when the 20-ms transition starts. It need not be the shortest mathematical sound period. Rapid changes coalesce; failures retain the previous table.

Large tables require substantial construction time and memory: up to 67,108,864 samples (256 MiB) plus 1 GiB of IFFT scratch; at most three tables per node. Narrow frequency intervals may not accommodate the requested count even with a large table.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/sine-cloud.md)
