# Pitch Monitor
Displays detected and target pitches while forwarding the target.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Pitch | Readout | Editor |
| Detected [Hz] | Hz | Value; ≥ 0 Hz; Auto |
| Target [Hz] | Hz | Value; ≥ 0 Hz; Auto |
| Confidence | — | Value; 0–1; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Target [Hz] | Value; ≥ 0 Hz |

## Behavior
The output is Target unchanged. The readout compares detected pitch, target and confidence; it performs no pitch detection or quantization. Place it in the target path to keep the monitor part of the active graph.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Target [Hz] → Target [Hz] |
| Insertion | Target [Hz] → Target [Hz] |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/pitch-monitor.md)
