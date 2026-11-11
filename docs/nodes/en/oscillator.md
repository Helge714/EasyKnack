# Oscillator
Generates a periodic waveform.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Waveform | Sine, Triangle, Sawtooth, Square; Default: Sine | Wavetable resource; Control |
| Frequency | 1e-5–20 000 Hz; Default: 440 Hz; Hz, kHz | Value; 0–20 000 Hz; Auto |
| Phase | ±∞ turn; Default: 0 turn; turn, °, rad | Value; ±∞ turn; Auto |
| Reset | Button | Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±1 |

## Behavior
The running phase advances by `f / fs` turns per sample. Phase adds an offset without restarting the cycle; Reset restarts before the corresponding sample. With zero frequency the current phase is held, which can produce a constant output. The selected or connected Wavetable is read with frequency-dependent band limiting. Phase wraps modulo one turn. Manual Reset is delivered at the next block start.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Sample feedback | Supported |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/oscillator.md)
