# Partial Group
Processes each detected partial once per analysis frame.
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
| Partials | Partials; Control |

## Behavior

[Guide](../../user/en/partial-group.md)
Default routing: `Partial Analysis → Partial Group → Partials to Spectrum → Spectrum Math (Add) → IFFT`; connect the analysis residual to the second Math input. Inside, use `Partial Context → Math / Formula Group → Partial Output`. Frequency, amplitude and phase remain independent values. State is isolated by ID and reset for new IDs. Up to 64 numeric processors; ordinary nested groups and numeric Formula Groups are flattened. No audio oscillator bank, buffers or trigger programs. External numeric values are sampled at window end from the current publication block; asynchronous historical modulation values are not buffered.

Use [Partial Noise](partial-noise.md) for independent random modulation per track. It incorporates stable IDs automatically; map its output with `2 * random - 1` for symmetric deviations.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Partials → Partials |
| Insertion | Partials → Partials |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Deutsch](../de/partial-group.md)
