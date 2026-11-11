# Step Switch
Selects one input by trigger or numeric index.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Data type | Value, Vector, Wavetable, Buffer, Spectrum, Analysis; Default: Value | Editor; Inspector |
| Steps | 1–32; Default: 4 | Value |
| Selection | Trigger, Index; Default: Trigger | Editor; Inspector |
| Next | Button | Trigger; Trigger; Trigger mode |
| Reset | Button | Trigger; Trigger; Trigger mode |
| Selected index | 0–31, integer; Default: 0 | Value; 0–Steps−1, rounded down; Auto; Index mode |
| Input 1…N | ±∞ for Value; no resource editor; Default: 0 for Value | Selected type; Selected data type; Auto |
| Step name 1…N | Text; Default: Input 1…N | Editor; Inspector |
| Input order | Name, Value, Move up / down; Default: Original slot order | Editor; Inspector |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector; Value/Vector |

## Outputs
| Output | Signal |
| --- | --- |
| Value / Selected | Selected type; Selected input |
| Index | Value; 0–Steps−1 |

## Behavior
Next advances with wrap; Reset selects 0 and wins over simultaneous Next. Index mode floors and clamps its input; numbered selection buttons are disabled. Resource/vector modes replace the numeric step/output types with the selected type; resources have no fallback sliders. Resource selection is held per block and later triggers affect the next block; Index then reports the chosen block resource at Control rate. Vector contents can retain Audio rate. Switching spectra is hard, with no crossfade. Input labels can be renamed; names and selected step are saved. Inside Instance Groups the visible selection represents the first nested instance, manual selection affects all, and connected selectors remain independent. All connected sources still compute.

The inspector shows one connected control group per input, containing its editable name, numeric fallback value where applicable, and Move up/down buttons. Each input retains its stable index, value, name and connections; Next and Index continue to address those indices. Use Reset name in the node context menu to restore its default title; Undo restores the custom title.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Input 1 → Value / Selected |
| Insertion | Input 1 → Value / Selected |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/step-switch.md)
